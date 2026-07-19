package io.github.nie1ix.movewand.transform;

import net.minecraft.core.Direction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockStateTransformTest {
    @Test
    void rotatesBlockStateClockwiseAroundY() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BlockState northFacingFurnace = Blocks.FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);

        BlockState rotated = BlockStateTransform.rotateY(northFacingFurnace, 1);

        assertEquals(Direction.EAST, rotated.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
