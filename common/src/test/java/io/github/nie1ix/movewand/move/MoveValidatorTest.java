package io.github.nie1ix.movewand.move;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import io.github.nie1ix.movewand.selection.BlockSelection;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveValidatorTest {
    @Test
    void identifiesUnmovableBlocks() {
        assertTrue(MoveValidator.isUnmovable(Blocks.BEDROCK.defaultBlockState()));
        assertFalse(MoveValidator.isUnmovable(Blocks.STONE.defaultBlockState()));
    }

    @Test
    void allowsTargetPositionsVacatedByTheSameMove() {
        BlockSelection source = BlockSelection.of(List.of(new BlockPos(0, 0, 0), new BlockPos(1, 0, 0)));
        Set<BlockPos> targets = Set.of(new BlockPos(1, 0, 0), new BlockPos(2, 0, 0));

        assertEquals(MoveValidation.VALID, MoveValidator.validate(source, targets, position -> position.equals(new BlockPos(2, 0, 0))));
    }

    @Test
    void rejectsAOccupiedTargetOutsideTheSelection() {
        BlockSelection source = BlockSelection.of(List.of(BlockPos.ZERO));
        Set<BlockPos> targets = Set.of(new BlockPos(1, 0, 0));

        assertEquals(MoveValidation.TARGET_OCCUPIED, MoveValidator.validate(source, targets, ignored -> false));
    }

    @Test
    void rejectsSelectionsLargerThanTheConfiguredLimit() {
        BlockSelection source = BlockSelection.of(List.of(BlockPos.ZERO, new BlockPos(1, 0, 0)));

        assertEquals(MoveValidation.SELECTION_TOO_LARGE, MoveValidator.validate(source, source.positions(), ignored -> true, 1));
    }
}
