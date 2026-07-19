package io.github.nie1ix.movewand.move.vanilla;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VanillaMoveHookTest {
    @Test
    void expandsPairedBlocksAndRotatesVanillaState() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BlockPos lower = BlockPos.ZERO;
        BlockPos upper = lower.above();
        Map<BlockPos, BlockState> states = Map.of(
                lower, Blocks.OAK_DOOR.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER),
                upper, Blocks.OAK_DOOR.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
        );
        BlockState furnace = Blocks.FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);

        VanillaMoveHook hook = new VanillaMoveHook();

        assertEquals(Set.of(lower, upper), hook.expandSelection(Set.of(lower), states::get));
        assertEquals(
                Direction.EAST,
                hook.transformBlockState(furnace, 1).getValue(BlockStateProperties.HORIZONTAL_FACING)
        );
    }
}
