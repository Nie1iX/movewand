package io.github.nie1ix.movewand.move;

import net.minecraft.core.BlockPos;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.SelectionEditor;

import java.util.Set;
import java.util.function.Predicate;

public final class MoveValidator {
    public static final int MAX_SELECTION_BLOCKS = SelectionEditor.DEFAULT_MAX_POSITIONS;

    private MoveValidator() {
    }

    public static MoveValidation validate(BlockSelection source, Set<BlockPos> targets, Predicate<BlockPos> isAir) {
        return validate(source, targets, isAir, MAX_SELECTION_BLOCKS);
    }

    public static MoveValidation validate(BlockSelection source, Set<BlockPos> targets, Predicate<BlockPos> isAir, int maxSelectionBlocks) {
        if (source.positions().size() > maxSelectionBlocks) {
            return MoveValidation.SELECTION_TOO_LARGE;
        }

        for (BlockPos target : targets) {
            if (!source.positions().contains(target) && !isAir.test(target)) {
                return MoveValidation.TARGET_OCCUPIED;
            }
        }
        return MoveValidation.VALID;
    }
}
