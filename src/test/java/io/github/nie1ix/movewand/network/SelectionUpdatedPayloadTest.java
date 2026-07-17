package io.github.nie1ix.movewand.network;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectionUpdatedPayloadTest {
    @Test
    void representsAnUnfinishedBoxSelection() {
        BlockPos firstCorner = new BlockPos(1, 2, 3);

        SelectionUpdatedPayload payload = new SelectionUpdatedPayload(Set.of(), null, firstCorner);

        assertTrue(payload.positions().isEmpty());
        assertEquals(firstCorner, payload.pendingBoxCorner());
    }
}
