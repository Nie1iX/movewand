package io.github.nie1ix.movewand.client;

import io.github.nie1ix.movewand.network.SelectionUpdatedPayload;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientSelectionHandlerTest {
    @AfterEach
    void resetSelection() {
        ClientSelectionHandler.reset();
    }

    @Test
    void rendersTheServerSnapshotWithoutChangingItsSelection() {
        BlockPos foot = new BlockPos(1, 2, 3);
        BlockPos head = foot.north();
        BlockPos nextBoxCorner = new BlockPos(5, 6, 7);

        ClientSelectionHandler.replace(new SelectionUpdatedPayload(
                Set.of(foot, head), foot, nextBoxCorner
        ));

        assertEquals(Set.of(foot, head), ClientSelectionHandler.selection().orElseThrow().positions());
        assertEquals(nextBoxCorner, ClientSelectionHandler.pendingBoxCorner().orElseThrow());
    }
}
