package io.github.nie1ix.movewand.move;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveProjectionTest {
    @Test
    void clearsSourceBeforeCheckingTheDestination() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BlockPos source = new BlockPos(0, 2, 0);
        BlockPos destination = source.below();
        BlockState dripstone = Blocks.POINTED_DRIPSTONE.defaultBlockState();

        Map<BlockPos, BlockState> projected = MoveProjection.blockStatesAfterMove(
                Map.of(source, dripstone),
                Map.of(source, destination)
        );

        assertEquals(Blocks.AIR.defaultBlockState(), projected.get(source));
        assertEquals(dripstone, projected.get(destination));
    }
}
