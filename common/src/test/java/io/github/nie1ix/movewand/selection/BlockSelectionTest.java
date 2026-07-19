package io.github.nie1ix.movewand.selection;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockSelectionTest {
    @Test
    void removesDuplicatePositions() {
        BlockPos block = new BlockPos(1, 2, 3);

        BlockSelection selection = BlockSelection.of(List.of(block, block));

        assertEquals(1, selection.positions().size());
    }

    @Test
    void preservesPositionOrder() {
        List<BlockPos> positions = List.of(
                new BlockPos(4, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(3, 0, 0),
                new BlockPos(0, 0, 0),
                new BlockPos(2, 0, 0)
        );

        assertIterableEquals(positions, BlockSelection.of(positions).positions());
    }

    @Test
    void rejectsEmptySelection() {
        assertThrows(IllegalArgumentException.class, () -> BlockSelection.of(List.of()));
    }
}
