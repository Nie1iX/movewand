package io.github.nie1ix.movewand.move.engine;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveHooksTest {
    @Test
    void appliesSelectionAndStateHooksInOrder() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BlockPos first = BlockPos.ZERO;
        BlockPos second = first.east();
        MoveHook addPosition = new MoveHook() {
            @Override
            public Set<BlockPos> expandSelection(net.minecraft.server.level.ServerLevel level, Set<BlockPos> positions) {
                return Set.of(first, second);
            }
        };
        MoveHook transformState = new MoveHook() {
            @Override
            public BlockState transformBlockState(BlockState state, int clockwiseTurns) {
                assertEquals(1, clockwiseTurns);
                return Blocks.GOLD_BLOCK.defaultBlockState();
            }
        };

        assertEquals(Set.of(first, second), MoveHooks.expandSelection(null, Set.of(first), List.of(addPosition)));
        assertEquals(
                Blocks.GOLD_BLOCK,
                MoveHooks.transformBlockState(Blocks.STONE.defaultBlockState(), 1, List.of(transformState)).getBlock()
        );
    }
}
