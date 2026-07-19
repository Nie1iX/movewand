package io.github.nie1ix.movewand.move;

import io.github.nie1ix.movewand.move.engine.MoveContext;
import io.github.nie1ix.movewand.move.engine.MoveHook;
import io.github.nie1ix.movewand.move.engine.MoveHooks;
import io.github.nie1ix.movewand.move.vanilla.VanillaMoveRules;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import io.github.nie1ix.movewand.transform.SelectionTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MoveEngine {
    private MoveEngine() {
    }

    public static void move(ServerPlayer player, BlockSelection selected, int x, int y, int z, int turns) {
        ServerLevel level = player.serverLevel();
        Set<BlockPos> selectedPositions = selected.positions().stream()
                .filter(position -> !level.getBlockState(position).isAir())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        List<MoveHook> hooks = MoveHooks.all();
        Set<BlockPos> positions = MoveHooks.expandSelection(level, selectedPositions, hooks).stream()
                .filter(position -> !level.getBlockState(position).isAir())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (positions.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.movewand.selection.empty"), true);
            return;
        }
        if (VanillaMoveRules.selectsOneHalfOfDoubleChest(positions, level)) {
            player.displayClientMessage(Component.translatable("message.movewand.move.double_chest"), true);
            return;
        }

        BlockPos pivot = positions.contains(selected.pivot()) ? selected.pivot() : positions.iterator().next();
        BlockSelection source = BlockSelection.of(positions, pivot);
        Map<BlockPos, BlockPos> destinations = SelectionTransform.transformMap(source, new BlockPos(x, y, z), turns);

        BlockState unmovableState = source.positions().stream()
                .map(level::getBlockState)
                .filter(MoveValidator::isUnmovable)
                .findFirst()
                .orElse(null);
        if (unmovableState != null) {
            displayUnmovableBlock(player, unmovableState);
            return;
        }

        if (destinations.values().stream().anyMatch(position -> !level.hasChunkAt(position))) {
            player.displayClientMessage(Component.translatable("message.movewand.move.unloaded"), true);
            return;
        }

        MoveValidation validation = MoveValidator.validate(source, Set.copyOf(destinations.values()), position -> {
            BlockState state = level.getBlockState(position);
            return state.isAir() || (!state.getFluidState().isEmpty() && !state.getFluidState().isSource());
        });
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
                CompoundTag data = blockEntity.saveWithoutMetadata(level.registryAccess());
                if (data.contains("Lock") && !data.getString("Lock").isEmpty()) {
                    displayUnmovableBlock(player, state);
                    return;
                }
                blockEntityData.put(position, data);
            }
            states.put(position, MoveHooks.transformBlockState(state, turns, hooks));
        }

        MoveContext moveContext = new MoveContext(level, destinations, blockEntityData, turns);
        for (MoveHook hook : hooks) {
            hook.transformBlockEntityData(moveContext);
        }

        LevelReader projectedLevel = MoveProjection.levelAfterMove(level, states, destinations);
        if (states.entrySet().stream().anyMatch(entry -> !entry.getValue().canSurvive(projectedLevel, destinations.get(entry.getKey())))) {
            player.displayClientMessage(Component.translatable("message.movewand.move.unsurvivable"), true);
            return;
        }

        List<HangingEntity> hangingEntities = VanillaMoveRules.selectedHangingEntities(level, source.positions());

        for (BlockPos position : blockEntityData.keySet()) {
            level.removeBlockEntity(position);
        }
        for (BlockPos position : sourceClearOrder(level, states)) {
            level.setBlock(position, Blocks.AIR.defaultBlockState(), sourceClearFlags());
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
        VanillaMoveRules.relocateHangingEntities(hangingEntities, destinations, turns);
        updateBoundaryShapes(level, source.positions(), destinations.values());
        for (BlockPos position : source.positions()) {
            level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
        }
        for (BlockPos position : destinations.values()) {
            level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
        }
        for (MoveHook hook : hooks) {
            hook.afterMove(moveContext);
        }

        BlockSelection updatedSelection = BlockSelection.of(destinations.values(), destinations.get(source.pivot()));
        ServerSelectionManager.replace(player, updatedSelection);
        ServerSelectionManager.sendSelectionUpdate(player);
        player.displayClientMessage(Component.translatable("message.movewand.move.success"), true);
    }

    public static int sourceClearFlags() {
        return Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    }

    public static CompoundTag relocatedBlockEntityData(CompoundTag snapshot, BlockPos destination) {
        CompoundTag relocated = snapshot.copy();
        relocated.putInt("x", destination.getX());
        relocated.putInt("y", destination.getY());
        relocated.putInt("z", destination.getZ());
        return relocated;
    }

    private static List<BlockPos> sourceClearOrder(ServerLevel level, Map<BlockPos, BlockState> states) {
        Set<BlockPos> remaining = new LinkedHashSet<>(states.keySet());
        Map<BlockPos, BlockState> projectedStates = new HashMap<>(states);
        List<BlockPos> clearOrder = new ArrayList<>(states.size());
        while (!remaining.isEmpty()) {
            BlockPos next = remaining.stream()
                    .filter(position -> canClearWithoutBreakingSelectedNeighbors(
                            level, projectedStates, states, remaining, position))
                    .findFirst()
                    .orElseGet(() -> remaining.iterator().next());
            clearOrder.add(next);
            remaining.remove(next);
            projectedStates.put(next, Blocks.AIR.defaultBlockState());
        }
        return clearOrder;
    }

    private static boolean canClearWithoutBreakingSelectedNeighbors(
            ServerLevel level,
            Map<BlockPos, BlockState> projectedStates,
            Map<BlockPos, BlockState> states,
            Set<BlockPos> remaining,
            BlockPos position
    ) {
        projectedStates.put(position, Blocks.AIR.defaultBlockState());
        LevelReader projectedLevel = new ProjectedLevelReader(level, projectedStates);
        boolean safe = true;
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = position.relative(direction);
            if (remaining.contains(neighbor) && !states.get(neighbor).canSurvive(projectedLevel, neighbor)) {
                safe = false;
                break;
            }
        }
        projectedStates.put(position, states.get(position));
        return safe;
    }

    private static void updateBoundaryShapes(
            ServerLevel level,
            Set<BlockPos> sourcePositions,
            Collection<BlockPos> destinationPositions
    ) {
        Set<BlockPos> structurePositions = new LinkedHashSet<>(sourcePositions);
        structurePositions.addAll(destinationPositions);
        for (BlockPos position : structurePositions) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = position.relative(direction);
                if (structurePositions.contains(neighbor)) {
                    continue;
                }
                BlockState state = level.getBlockState(neighbor);
                Block.updateOrDestroy(state, Block.updateFromNeighbourShapes(state, level, neighbor),
                        level, neighbor, Block.UPDATE_ALL);
            }
        }
    }

    private static void displayUnmovableBlock(ServerPlayer player, BlockState state) {
        player.displayClientMessage(
                Component.translatable("message.movewand.move.unmovable", state.getBlock().getName()),
                true
        );
    }
}
