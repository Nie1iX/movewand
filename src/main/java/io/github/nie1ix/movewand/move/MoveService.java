package io.github.nie1ix.movewand.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.github.nie1ix.movewand.network.SelectionUpdatedPayload;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import io.github.nie1ix.movewand.transform.SelectionTransform;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MoveService {
    public static final int MAX_OFFSET_DISTANCE = 16;

    // Optional compatibility tags. Referencing an absent namespace does not add a mod dependency.
    private static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported")
    );
    private static final TagKey<Block> CREATE_NON_MOVABLE = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("create", "non_movable")
    );
    private static final TagKey<Block> FORGE_RELOCATION_NOT_SUPPORTED = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("forge", "relocation_not_supported")
    );

    private MoveService() {
    }

    public static void move(ServerPlayer player, int x, int y, int z, int clockwiseTurns) {
        if (!player.getMainHandItem().is(ModItems.MOVE_WAND)) {
            return;
        }

        int turns = Math.floorMod(clockwiseTurns, 4);
        if (!hasValidOffset(x, y, z) && turns == 0) {
            player.displayClientMessage(Component.translatable("message.movewand.move.no_change"), true);
            return;
        }
        if (!new BlockPos(x, y, z).equals(BlockPos.ZERO) && !hasValidOffset(x, y, z)) {
            player.displayClientMessage(Component.translatable("message.movewand.move.too_far"), true);
            return;
        }

        Optional<BlockSelection> selected = ServerSelectionManager.selection(player);
        if (selected.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.movewand.selection.empty"), true);
            return;
        }

        ServerLevel level = player.serverLevel();
        Set<BlockPos> positions = selected.get().positions().stream()
                .filter(position -> !level.getBlockState(position).isAir())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (positions.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.movewand.selection.empty"), true);
            return;
        }

        BlockPos pivot = positions.contains(selected.get().pivot()) ? selected.get().pivot() : positions.iterator().next();
        BlockSelection source = BlockSelection.of(positions, pivot);
        Map<BlockPos, BlockPos> destinations = SelectionTransform.transformMap(source, new BlockPos(x, y, z), turns);

        if (source.positions().stream().anyMatch(position -> {
            BlockState state = level.getBlockState(position);
            return state.is(Blocks.BEDROCK)
                    || state.is(RELOCATION_NOT_SUPPORTED)
                    || state.is(CREATE_NON_MOVABLE)
                    || state.is(FORGE_RELOCATION_NOT_SUPPORTED);
        })) {
            player.displayClientMessage(Component.translatable("message.movewand.move.unmovable"), true);
            return;
        }

        if (destinations.values().stream().anyMatch(position -> !level.hasChunkAt(position))) {
            player.displayClientMessage(Component.translatable("message.movewand.move.unloaded"), true);
            return;
        }

        MoveValidation validation = MoveValidator.validate(source, Set.copyOf(destinations.values()),
                position -> level.getBlockState(position).isAir());
        if (validation != MoveValidation.VALID) {
            player.displayClientMessage(Component.translatable("message.movewand.move.blocked"), true);
            return;
        }

        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        Map<BlockPos, CompoundTag> blockEntityData = new LinkedHashMap<>();
        for (BlockPos position : source.positions()) {
            BlockState state = level.getBlockState(position);
            if (state.hasBlockEntity()) {
                BlockEntity blockEntity = level.getBlockEntity(position);
                if (blockEntity == null) {
                    player.displayClientMessage(Component.translatable("message.movewand.move.block_entity"), true);
                    return;
                }
                // Preserve inventory and other persistent BlockEntity state before replacing the block.
                blockEntityData.put(position, blockEntity.saveWithoutMetadata(level.registryAccess()));
            }
            states.put(position, rotate(state, turns));
        }

        for (BlockPos position : source.positions()) {
            level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (Map.Entry<BlockPos, BlockState> entry : states.entrySet()) {
            level.setBlock(destinations.get(entry.getKey()), entry.getValue(), Block.UPDATE_CLIENTS);
        }
        for (Map.Entry<BlockPos, CompoundTag> entry : blockEntityData.entrySet()) {
            BlockEntity blockEntity = level.getBlockEntity(destinations.get(entry.getKey()));
            if (blockEntity != null) {
                blockEntity.loadWithComponents(
                        relocatedBlockEntityData(entry.getValue(), destinations.get(entry.getKey())),
                        level.registryAccess()
                );
                blockEntity.setChanged();
            }
        }
        for (BlockPos position : source.positions()) {
            level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
        }
        for (BlockPos position : destinations.values()) {
            level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
        }

        BlockSelection updatedSelection = BlockSelection.of(destinations.values(), destinations.get(source.pivot()));
        ServerSelectionManager.replace(player, updatedSelection);
        ServerPlayNetworking.send(player, new SelectionUpdatedPayload(updatedSelection.positions(), updatedSelection.pivot()));
        player.displayClientMessage(Component.translatable("message.movewand.move.success"), true);
    }

    public static boolean hasValidOffset(int x, int y, int z) {
        long squaredDistance = (long) x * x + (long) y * y + (long) z * z;
        return squaredDistance > 0 && squaredDistance <= (long) MAX_OFFSET_DISTANCE * MAX_OFFSET_DISTANCE;
    }

    static CompoundTag relocatedBlockEntityData(CompoundTag snapshot, BlockPos destination) {
        // BlockEntity NBT can include its position; copy first so the original snapshot stays reusable.
        CompoundTag relocated = snapshot.copy();
        relocated.putInt("x", destination.getX());
        relocated.putInt("y", destination.getY());
        relocated.putInt("z", destination.getZ());
        return relocated;
    }

    private static BlockState rotate(BlockState state, int clockwiseTurns) {
        BlockState rotated = state;
        for (int turn = 0; turn < clockwiseTurns; turn++) {
            rotated = rotated.rotate(Rotation.CLOCKWISE_90);
        }
        return rotated;
    }
}
