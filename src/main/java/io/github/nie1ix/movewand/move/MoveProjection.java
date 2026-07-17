package io.github.nie1ix.movewand.move;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class MoveProjection {
    private MoveProjection() {
    }

    public static Map<BlockPos, BlockState> blockStatesAfterMove(
            Map<BlockPos, BlockState> sourceStates,
            Map<BlockPos, BlockPos> destinations
    ) {
        Map<BlockPos, BlockState> projected = new HashMap<>();
        for (BlockPos source : sourceStates.keySet()) {
            projected.put(source, Blocks.AIR.defaultBlockState());
        }
        for (Map.Entry<BlockPos, BlockState> entry : sourceStates.entrySet()) {
            projected.put(destinations.get(entry.getKey()), entry.getValue());
        }
        return Map.copyOf(projected);
    }

    public static LevelReader levelAfterMove(
            LevelReader level,
            Map<BlockPos, BlockState> sourceStates,
            Map<BlockPos, BlockPos> destinations
    ) {
        return new ProjectedLevelReader(level, blockStatesAfterMove(sourceStates, destinations));
    }
}
