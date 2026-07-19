package io.github.nie1ix.movewand.selection;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectionEditorTest {
    @Test
    void twoCornersCreateAnInclusiveBoxSelection() {
        SelectionEditor editor = new SelectionEditor();

        editor.selectBoxCorner(new BlockPos(3, 2, 1));
        editor.selectBoxCorner(new BlockPos(2, 3, 2));

        assertEquals(
            Set.of(
                new BlockPos(2, 2, 1), new BlockPos(2, 2, 2),
                new BlockPos(2, 3, 1), new BlockPos(2, 3, 2),
                new BlockPos(3, 2, 1), new BlockPos(3, 2, 2),
                new BlockPos(3, 3, 1), new BlockPos(3, 3, 2)
            ),
            editor.selection().orElseThrow().positions()
        );
    }

    @Test
    void usesTheFirstBoxCornerAsThePivot() {
        SelectionEditor editor = new SelectionEditor();
        BlockPos firstCorner = new BlockPos(3, 2, 1);

        editor.selectBoxCorner(firstCorner);
        editor.selectBoxCorner(new BlockPos(2, 3, 2));

        assertEquals(firstCorner, editor.selection().orElseThrow().pivot());
    }

    @Test
    void togglingBlockAddsThenRemovesIt() {
        SelectionEditor editor = new SelectionEditor();
        BlockPos position = new BlockPos(1, 2, 3);

        editor.toggleBlock(position);
        assertTrue(editor.selection().isPresent());

        editor.toggleBlock(position);
        assertFalse(editor.selection().isPresent());
    }

    @Test
    void togglingPairedBlocksAddsThenRemovesTheWholeGroup() {
        SelectionEditor editor = new SelectionEditor();
        BlockPos clickedBlock = new BlockPos(1, 2, 3);
        Set<BlockPos> pairedBlocks = Set.of(clickedBlock, new BlockPos(1, 3, 3));

        editor.toggleBlocks(pairedBlocks, clickedBlock);

        assertEquals(pairedBlocks, editor.selection().orElseThrow().positions());

        editor.toggleBlocks(pairedBlocks, clickedBlock);

        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void togglingBlocksCancelsAnUnfinishedBoxSelection() {
        SelectionEditor editor = new SelectionEditor();
        BlockPos individualBlock = new BlockPos(1, 2, 3);

        editor.selectBoxCorner(BlockPos.ZERO);
        editor.toggleBlocks(Set.of(individualBlock), individualBlock);

        assertTrue(editor.pendingBoxCorner().isEmpty());
        assertEquals(Set.of(individualBlock), editor.selection().orElseThrow().positions());
    }

    @Test
    void usesTheFirstIndividuallyAddedBlockAsThePivot() {
        SelectionEditor editor = new SelectionEditor();
        BlockPos firstBlock = new BlockPos(1, 2, 3);

        editor.toggleBlock(firstBlock);
        editor.toggleBlock(new BlockPos(4, 5, 6));

        assertEquals(firstBlock, editor.selection().orElseThrow().pivot());
    }

    @Test
    void clearRemovesAStartedBoxSelection() {
        SelectionEditor editor = new SelectionEditor();
        editor.selectBoxCorner(BlockPos.ZERO);

        editor.clear();

        assertTrue(editor.pendingBoxCorner().isEmpty());
        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void replacesSelectionAndClearsPendingBoxCorner() {
        SelectionEditor editor = new SelectionEditor();
        editor.selectBoxCorner(BlockPos.ZERO);

        assertTrue(editor.replace(BlockSelection.of(Set.of(new BlockPos(2, 3, 4)))));

        assertEquals(Set.of(new BlockPos(2, 3, 4)), editor.selection().orElseThrow().positions());
        assertTrue(editor.pendingBoxCorner().isEmpty());
    }

    @Test
    void rejectsBoxLargerThanTheSelectionLimitBeforeMaterializingIt() {
        SelectionEditor editor = new SelectionEditor(1);

        assertTrue(editor.selectBoxCorner(BlockPos.ZERO));
        assertFalse(editor.selectBoxCorner(new BlockPos(1, 0, 0)));

        assertTrue(editor.selection().isEmpty());
        assertTrue(editor.pendingBoxCorner().isEmpty());
    }

    @Test
    void rejectsAnOverflowingBoxVolumeBeforeMaterializingIt() {
        SelectionEditor editor = new SelectionEditor(512);

        editor.selectBoxCorner(new BlockPos(Integer.MIN_VALUE, 0, Integer.MIN_VALUE));

        assertFalse(editor.selectBoxCorner(new BlockPos(Integer.MAX_VALUE, 1, Integer.MAX_VALUE)));
    }
}
