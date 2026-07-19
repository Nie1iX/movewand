package io.github.nie1ix.movewand.selection;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockSelectionTest {
    @Test
    void removesDuplicatePositions() {
        BlockPos block = new BlockPos(1, 2, 3);

        BlockSelection selection = BlockSelection.of(List.of(block, block));

        assertEquals(1, selection.positions().size());
    }

    @Test
    void rejectsEmptySelection() {
        assertThrows(IllegalArgumentException.class, () -> BlockSelection.of(List.of()));
    }
}
