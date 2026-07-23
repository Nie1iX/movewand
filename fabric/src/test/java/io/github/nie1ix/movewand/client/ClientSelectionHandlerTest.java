package io.github.nie1ix.movewand.client;

import io.github.nie1ix.movewand.network.SelectionUpdatedPayload;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientSelectionHandlerTest {
    @AfterEach
    void resetSelection() {
        ClientSelectionHandler.reset(() -> {
        });
    }

    @Test
    void rendersTheServerSnapshotWithoutChangingItsSelection() {
        BlockPos foot = new BlockPos(1, 2, 3);
        BlockPos head = foot.north();
        BlockPos nextBoxCorner = new BlockPos(5, 6, 7);
        AtomicBoolean previewCancelled = new AtomicBoolean();

        ClientSelectionHandler.replace(new SelectionUpdatedPayload(
                Set.of(foot, head), foot, nextBoxCorner
        ), () -> previewCancelled.set(true));

        assertEquals(Set.of(foot, head), ClientSelectionHandler.selection().orElseThrow().positions());
        assertEquals(nextBoxCorner, ClientSelectionHandler.pendingBoxCorner().orElseThrow());
        assertTrue(previewCancelled.get());
    }
}
