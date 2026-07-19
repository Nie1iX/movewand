package io.github.nie1ix.movewand.transform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import io.github.nie1ix.movewand.selection.BlockSelection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectionTransformTest {
    @Test
    void translatesEverySelectedBlock() {
        BlockSelection selection = BlockSelection.of(List.of(new BlockPos(0, 4, 0), new BlockPos(1, 4, 0)));

        Set<BlockPos> result = SelectionTransform.translate(selection, Direction.EAST);

        assertEquals(Set.of(new BlockPos(1, 4, 0), new BlockPos(2, 4, 0)), result);
    }

    @Test
    void mapsEachSourcePositionToItsTranslatedTarget() {
        BlockPos first = new BlockPos(0, 4, 0);
        BlockPos second = new BlockPos(1, 4, 0);
        BlockSelection selection = BlockSelection.of(List.of(first, second));

        assertEquals(
            Map.of(first, new BlockPos(0, 5, 0), second, new BlockPos(1, 5, 0)),
            SelectionTransform.translationMap(selection, Direction.UP)
        );
    }

    @Test
    void translatesEverySelectedBlockByAnOffset() {
        BlockSelection selection = BlockSelection.of(List.of(new BlockPos(0, 4, 0), new BlockPos(1, 4, 0)));

        Set<BlockPos> result = SelectionTransform.translate(selection, new BlockPos(-2, 3, 5));

        assertEquals(Set.of(new BlockPos(-2, 7, 5), new BlockPos(-1, 7, 5)), result);
    }

    @Test
    void rotatesEverySelectedBlockClockwiseAroundPivot() {
        BlockSelection selection = BlockSelection.of(List.of(new BlockPos(1, 4, 0), new BlockPos(1, 4, 1)));

        Set<BlockPos> result = SelectionTransform.rotateY(selection, BlockPos.ZERO, RotationDirection.CLOCKWISE);

        assertEquals(Set.of(new BlockPos(0, 4, 1), new BlockPos(-1, 4, 1)), result);
    }

    @Test
    void rotatesAroundTheSelectionPivot() {
        BlockPos pivot = new BlockPos(1, 4, 0);
        BlockSelection selection = BlockSelection.of(List.of(pivot, new BlockPos(2, 4, 0)), pivot);

        Set<BlockPos> result = SelectionTransform.rotateY(selection, RotationDirection.CLOCKWISE);

        assertEquals(Set.of(pivot, new BlockPos(1, 4, 1)), result);
    }

    @Test
    void combinesClockwiseRotationAndTranslation() {
        BlockPos pivot = new BlockPos(1, 4, 0);
        BlockPos second = new BlockPos(2, 4, 0);
        BlockSelection selection = BlockSelection.of(List.of(pivot, second), pivot);

        assertEquals(
                Map.of(pivot, new BlockPos(2, 4, 3), second, new BlockPos(2, 4, 4)),
                SelectionTransform.transformMap(selection, new BlockPos(1, 0, 3), 1)
        );
    }

    @Test
    void clockwiseAndCounterClockwiseRotationRestoreSelection() {
        BlockSelection selection = BlockSelection.of(List.of(new BlockPos(1, 4, 0), new BlockPos(0, 4, 1)));
        Set<BlockPos> clockwise = SelectionTransform.rotateY(selection, BlockPos.ZERO, RotationDirection.CLOCKWISE);

        Set<BlockPos> restored = SelectionTransform.rotateY(BlockSelection.of(clockwise), BlockPos.ZERO, RotationDirection.COUNTER_CLOCKWISE);

        assertEquals(selection.positions(), restored);
    }
}
