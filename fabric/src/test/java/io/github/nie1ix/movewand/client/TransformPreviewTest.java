package io.github.nie1ix.movewand.client;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformPreviewTest {
    @Test
    void acceptsZeroOffsetForRotationOnlyPreview() {
        assertTrue(TransformPreview.isOffsetWithinRange(BlockPos.ZERO));
    }

    @Test
    void rejectsOffsetsBeyondTheMovementLimit() {
        assertFalse(TransformPreview.isOffsetWithinRange(new BlockPos(0, 0, 17)));
    }

}
