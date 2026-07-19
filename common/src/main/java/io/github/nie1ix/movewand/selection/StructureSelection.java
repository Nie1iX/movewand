package io.github.nie1ix.movewand.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public final class StructureSelection {
    private StructureSelection() {
    }

    public static Set<BlockPos> expandPairedBlocks(Set<BlockPos> positions, Function<BlockPos, BlockState> stateAt) {
        Set<BlockPos> expanded = new LinkedHashSet<>(positions);
        for (BlockPos position : positions) {
            BlockState state = stateAt.apply(position);
            if (state.getBlock() instanceof DoorBlock) {
                DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
                BlockState otherState = stateAt.apply(position.relative(half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
                if (otherState.is(state.getBlock()) && otherState.getValue(DoorBlock.HALF) != half) {
                    expanded.add(position.relative(half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
                }
            } else if (state.getBlock() instanceof DoublePlantBlock) {
                DoubleBlockHalf half = state.getValue(DoublePlantBlock.HALF);
                BlockState otherState = stateAt.apply(position.relative(half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
                if (otherState.is(state.getBlock()) && otherState.getValue(DoublePlantBlock.HALF) != half) {
                    expanded.add(position.relative(half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
                }
            } else if (state.getBlock() instanceof BedBlock) {
                BedPart part = state.getValue(BedBlock.PART);
                Direction facing = state.getValue(BedBlock.FACING);
                BlockPos otherPart = position.relative(part == BedPart.FOOT ? facing : facing.getOpposite());
                BlockState otherState = stateAt.apply(otherPart);
                if (otherState.is(state.getBlock())
                        && otherState.getValue(BedBlock.PART) != part
                        && otherState.getValue(BedBlock.FACING) == facing) {
                    expanded.add(otherPart);
                }
            }
        }
        return Collections.unmodifiableSet(expanded);
    }
}
