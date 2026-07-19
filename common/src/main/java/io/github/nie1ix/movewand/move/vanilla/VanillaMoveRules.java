package io.github.nie1ix.movewand.move.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VanillaMoveRules {
    private VanillaMoveRules() {
    }

    public static boolean selectsOneHalfOfDoubleChest(Set<BlockPos> positions, ServerLevel level) {
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

    public static List<HangingEntity> selectedHangingEntities(ServerLevel level, Set<BlockPos> positions) {
        AABB bounds = new AABB(positions.iterator().next());
        for (BlockPos position : positions) {
            bounds = bounds.minmax(new AABB(position));
        }
        return level.getEntitiesOfClass(HangingEntity.class, bounds.inflate(1),
                entity -> positions.contains(hangingEntitySupport(entity)));
    }

    public static void relocateHangingEntities(
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
}
