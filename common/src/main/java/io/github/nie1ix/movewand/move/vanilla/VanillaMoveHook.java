package io.github.nie1ix.movewand.move.vanilla;

import io.github.nie1ix.movewand.move.engine.MoveHook;
import io.github.nie1ix.movewand.selection.StructureSelection;
import io.github.nie1ix.movewand.transform.BlockStateTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Function;

public final class VanillaMoveHook implements MoveHook {
    @Override
    public Set<BlockPos> expandSelection(ServerLevel level, Set<BlockPos> positions) {
        return expandSelection(positions, level::getBlockState);
    }

    @Override
    public BlockState transformBlockState(BlockState state, int clockwiseTurns) {
        return BlockStateTransform.rotateY(state, clockwiseTurns);
    }

    Set<BlockPos> expandSelection(Set<BlockPos> positions, Function<BlockPos, BlockState> stateAt) {
        return StructureSelection.expandPairedBlocks(positions, stateAt);
    }
}
