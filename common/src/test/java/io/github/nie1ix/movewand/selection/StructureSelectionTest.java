package io.github.nie1ix.movewand.selection;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class StructureSelectionTest {
    @Test
    void expandsDoorSelectionToItsOtherHalf() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BlockPos lower = new BlockPos(2, 4, 6);
        BlockPos upper = lower.above();
        BlockState lowerState = Blocks.OAK_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upperState = Blocks.OAK_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.UPPER);

        Set<BlockPos> expanded = StructureSelection.expandPairedBlocks(Set.of(lower), Map.of(lower, lowerState, upper, upperState)::get);

        assertEquals(Set.of(lower, upper), expanded);
    }

    @Test
    void expandsBedSelectionToItsOtherPart() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BlockPos foot = new BlockPos(2, 4, 6);
        BlockPos head = foot.north();
        BlockState footState = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.FOOT);
        BlockState headState = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.HEAD);

        Set<BlockPos> expanded = StructureSelection.expandPairedBlocks(Set.of(foot), Map.of(foot, footState, head, headState)::get);

        assertEquals(Set.of(foot, head), expanded);
    }

    @Test
    void preservesPositionOrderWhenNoPairsAreAdded() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        List<BlockPos> positions = List.of(
                new BlockPos(4, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(3, 0, 0),
                new BlockPos(0, 0, 0),
                new BlockPos(2, 0, 0)
        );

        Set<BlockPos> expanded = StructureSelection.expandPairedBlocks(
                new LinkedHashSet<>(positions),
                ignored -> Blocks.AIR.defaultBlockState()
        );

        assertIterableEquals(positions, expanded);
    }
}
