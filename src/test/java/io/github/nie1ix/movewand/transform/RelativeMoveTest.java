package io.github.nie1ix.movewand.transform;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RelativeMoveTest {
    @Test
    void rightOfEastIsSouth() {
        assertEquals(Direction.SOUTH, RelativeMove.RIGHT.resolve(Direction.EAST));
    }

    @Test
    void leftOfNorthIsWest() {
        assertEquals(Direction.WEST, RelativeMove.LEFT.resolve(Direction.NORTH));
    }

    @Test
    void forwardPreservesHorizontalView() {
        assertEquals(Direction.WEST, RelativeMove.FORWARD.resolve(Direction.WEST));
    }

    @Test
    void upIgnoresHorizontalView() {
        assertEquals(Direction.UP, RelativeMove.UP.resolve(Direction.SOUTH));
    }

    @Test
    void horizontalMoveRejectsVerticalView() {
        assertThrows(IllegalArgumentException.class, () -> RelativeMove.RIGHT.resolve(Direction.UP));
    }

    @Test
    void resolvesAStableNetworkId() {
        assertEquals(RelativeMove.LEFT, RelativeMove.fromId(RelativeMove.LEFT.id()));
    }

    @Test
    void rejectsAnUnknownNetworkId() {
        assertThrows(IllegalArgumentException.class, () -> RelativeMove.fromId(99));
    }
}
