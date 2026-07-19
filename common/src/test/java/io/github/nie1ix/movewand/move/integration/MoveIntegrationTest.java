package io.github.nie1ix.movewand.move.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveIntegrationTest {
    @Test
    void appliesBlockStateTransformationsInRegistrationOrder() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BlockState state = Blocks.STONE.defaultBlockState();
        MoveIntegration first = new MoveIntegration() {
            @Override
            public BlockState transformBlockState(BlockState input, int clockwiseTurns) {
                assertEquals(1, clockwiseTurns);
                return Blocks.GOLD_BLOCK.defaultBlockState();
            }
        };
        MoveIntegration second = new MoveIntegration() {
            @Override
            public BlockState transformBlockState(BlockState input, int clockwiseTurns) {
                assertEquals(Blocks.GOLD_BLOCK, input.getBlock());
                return Blocks.DIAMOND_BLOCK.defaultBlockState();
            }
        };

        BlockState transformed = MoveIntegrations.transformBlockState(state, 1, List.of(first, second));

        assertEquals(Blocks.DIAMOND_BLOCK, transformed.getBlock());
    }

    @Test
    void passesExpandedSelectionToFollowingIntegrations() {
        BlockPos controller = new BlockPos(1, 2, 3);
        BlockPos core = new BlockPos(2, 2, 3);
        MoveIntegration addController = new MoveIntegration() {
            @Override
            public Set<BlockPos> expandSelection(net.minecraft.server.level.ServerLevel level, Set<BlockPos> positions) {
                return Set.of(controller);
            }
        };
        MoveIntegration addCore = new MoveIntegration() {
            @Override
            public Set<BlockPos> expandSelection(net.minecraft.server.level.ServerLevel level, Set<BlockPos> positions) {
                assertEquals(Set.of(controller), positions);
                return Set.of(controller, core);
            }
        };

        Set<BlockPos> expanded = MoveIntegrations.expandSelection(null, Set.of(), List.of(addController, addCore));

        assertEquals(Set.of(controller, core), expanded);
    }
}
