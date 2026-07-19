package io.github.nie1ix.movewand.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.move.integration.MoveContext;
import io.github.nie1ix.movewand.move.integration.MoveIntegration;
import io.github.nie1ix.movewand.move.integration.MoveIntegrations;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import io.github.nie1ix.movewand.selection.StructureSelection;
import io.github.nie1ix.movewand.transform.SelectionTransform;
import io.github.nie1ix.movewand.transform.BlockStateTransform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MoveService {
    public static final int MAX_OFFSET_DISTANCE = 16;

    private MoveService() {
    }

    public static void move(ServerPlayer player, int x, int y, int z, int clockwiseTurns) {
        if (!player.getMainHandItem().is(ModItems.moveWand())) {
            return;
        }

        int turns = Math.floorMod(clockwiseTurns, 4);
        if (!hasValidOffset(x, y, z) && turns == 0) {
            player.sendOverlayMessage(Component.translatable("message.movewand.move.no_change"));
            return;
        }
        if (!new BlockPos(x, y, z).equals(BlockPos.ZERO) && !hasValidOffset(x, y, z)) {
            player.sendOverlayMessage(Component.translatable("message.movewand.move.too_far"));
            return;
        }

        Optional<BlockSelection> selected = ServerSelectionManager.selection(player);
        if (selected.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.movewand.selection.empty"));
            return;
        }

        ServerLevel level = player.level();
        Set<BlockPos> selectedPositions = selected.get().positions().stream()
                .filter(position -> !level.getBlockState(position).isAir())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<BlockPos> positions = StructureSelection.expandPairedBlocks(selectedPositions, level::getBlockState);
        List<MoveIntegration> integrations = MoveIntegrations.all();
        positions = MoveIntegrations.expandSelection(level, positions);
        positions = positions.stream()
                .filter(position -> !level.getBlockState(position).isAir())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (positions.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.movewand.selection.empty"));
            return;
        }
        if (selectsOneHalfOfDoubleChest(positions, level)) {
            player.sendOverlayMessage(Component.translatable("message.movewand.move.double_chest"));
            return;
        }

        BlockPos pivot = positions.contains(selected.get().pivot()) ? selected.get().pivot() : positions.iterator().next();
        BlockSelection source = BlockSelection.of(positions, pivot);
        Map<BlockPos, BlockPos> destinations = SelectionTransform.transformMap(source, new BlockPos(x, y, z), turns);

        Optional<BlockState> unmovableState = source.positions().stream()
                .map(level::getBlockState)
                .filter(MoveValidator::isUnmovable)
                .findFirst();
        if (unmovableState.isPresent()) {
            displayUnmovableBlock(player, unmovableState.get());
            return;
        }

        if (destinations.values().stream().anyMatch(position -> !level.hasChunkAt(position))) {
            player.sendOverlayMessage(Component.translatable("message.movewand.move.unloaded"));
            return;
        }

        MoveValidation validation = MoveValidator.validate(source, Set.copyOf(destinations.values()), position -> {
            BlockState state = level.getBlockState(position);
            return state.isAir() || (!state.getFluidState().isEmpty() && !state.getFluidState().isSource());
        });
        if (validation != MoveValidation.VALID) {
            player.sendOverlayMessage(Component.translatable("message.movewand.move.blocked"));
            return;
        }

        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        Map<BlockPos, CompoundTag> blockEntityData = new LinkedHashMap<>();
        for (BlockPos position : source.positions()) {
            BlockState state = level.getBlockState(position);
            if (state.hasBlockEntity()) {
                BlockEntity blockEntity = level.getBlockEntity(position);
                if (blockEntity == null) {
                    player.sendOverlayMessage(Component.translatable("message.movewand.move.block_entity"));
                    return;
                }
                // Preserve inventory and other persistent BlockEntity state before replacing the block.
                CompoundTag data = blockEntity.saveWithoutMetadata(level.registryAccess());
                if (data.contains("Lock") && !data.getString("Lock").isEmpty()) {
                    displayUnmovableBlock(player, state);
                    return;
                }
                blockEntityData.put(position, data);
            }
            states.put(position, BlockStateTransform.rotateY(state, turns));
        }

        MoveContext moveContext = new MoveContext(level, destinations, blockEntityData, turns);
        for (MoveIntegration integration : integrations) {
            integration.transformBlockEntityData(moveContext);
        }

        LevelReader projectedLevel = MoveProjection.levelAfterMove(level, states, destinations);
        if (states.entrySet().stream().anyMatch(entry -> !entry.getValue().canSurvive(projectedLevel, destinations.get(entry.getKey())))) {
            player.sendOverlayMessage(Component.translatable("message.movewand.move.unsurvivable"));
            return;
        }

        List<HangingEntity> hangingEntities = selectedHangingEntities(level, source.positions());

        // Remove captured BlockEntities first so container blocks cannot drop their inventory on removal.
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
                blockEntity.loadWithComponents(TagValueInput.create(
                        ProblemReporter.DISCARDING,
                        level.registryAccess(),
                        relocatedBlockEntityData(entry.getValue(), destinations.get(entry.getKey()))
                ));
                blockEntity.setChanged();
            }
        }
        relocateHangingEntities(hangingEntities, destinations, turns);
        updateBoundaryShapes(level, source.positions(), destinations.values());
        for (BlockPos position : source.positions()) {
            level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
        }
        for (BlockPos position : destinations.values()) {
            level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
        }
        for (MoveIntegration integration : integrations) {
            integration.afterMove(moveContext);
        }

        BlockSelection updatedSelection = BlockSelection.of(destinations.values(), destinations.get(source.pivot()));
        ServerSelectionManager.replace(player, updatedSelection);
        ServerSelectionManager.sendSelectionUpdate(player);
        player.sendOverlayMessage(Component.translatable("message.movewand.move.success"));
    }

    private static List<HangingEntity> selectedHangingEntities(ServerLevel level, Set<BlockPos> positions) {
        AABB bounds = new AABB(positions.iterator().next());
        for (BlockPos position : positions) {
            bounds = bounds.minmax(new AABB(position));
        }
        return level.getEntitiesOfClass(HangingEntity.class, bounds.inflate(1),
                entity -> positions.contains(hangingEntitySupport(entity)));
    }

    private static void relocateHangingEntities(
            List<HangingEntity> entities,
            Map<BlockPos, BlockPos> destinations,
            int turns
    ) {
        for (HangingEntity entity : entities) {
            BlockPos destination = destinations.get(hangingEntitySupport(entity));
            for (int turn = 0; turn < turns; turn++) {
                entity.rotate(Rotation.CLOCKWISE_90);
            }
            BlockPos hangingPosition = destination.relative(entity.getDirection());
            entity.setPos(hangingPosition.getX(), hangingPosition.getY(), hangingPosition.getZ());
        }
    }

    private static BlockPos hangingEntitySupport(HangingEntity entity) {
        return entity.getPos().relative(entity.getDirection().getOpposite());
    }

    private static void updateBoundaryShapes(ServerLevel level, Set<BlockPos> sourcePositions,
                                             Collection<BlockPos> destinationPositions) {
        Set<BlockPos> structurePositions = new HashSet<>(sourcePositions);
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

    private static void displayUnmovableBlock(ServerPlayer player, BlockState state) {
        player.sendOverlayMessage(Component.translatable("message.movewand.move.unmovable", state.getBlock().getName()));
    }

    public static boolean hasValidOffset(int x, int y, int z) {
        long squaredDistance = (long) x * x + (long) y * y + (long) z * z;
        return squaredDistance > 0 && squaredDistance <= (long) MAX_OFFSET_DISTANCE * MAX_OFFSET_DISTANCE;
    }

    private static boolean selectsOneHalfOfDoubleChest(Set<BlockPos> positions, ServerLevel level) {
        for (BlockPos position : positions) {
            BlockState state = level.getBlockState(position);
            if (!(state.getBlock() instanceof ChestBlock) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                continue;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos otherPosition = position.relative(direction);
                BlockState otherState = level.getBlockState(otherPosition);
                if (otherState.is(state.getBlock())
                        && otherState.getValue(ChestBlock.FACING) == state.getValue(ChestBlock.FACING)
                        && otherState.getValue(ChestBlock.TYPE) != ChestType.SINGLE
                        && otherState.getValue(ChestBlock.TYPE) != state.getValue(ChestBlock.TYPE)
                        && !positions.contains(otherPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    static int sourceClearFlags() {
        return Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    }

    static CompoundTag relocatedBlockEntityData(CompoundTag snapshot, BlockPos destination) {
        // BlockEntity NBT can include its position; copy first so the original snapshot stays reusable.
        CompoundTag relocated = snapshot.copy();
        relocated.putInt("x", destination.getX());
        relocated.putInt("y", destination.getY());
        relocated.putInt("z", destination.getZ());
        return relocated;
    }

}
