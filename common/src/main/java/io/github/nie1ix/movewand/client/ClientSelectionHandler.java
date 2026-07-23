package io.github.nie1ix.movewand.client;

import io.github.nie1ix.movewand.network.SelectionUpdatedPayload;
import io.github.nie1ix.movewand.selection.BlockSelection;
import net.minecraft.core.BlockPos;

import java.util.Optional;

public final class ClientSelectionHandler {
    private static BlockSelection selection;
    private static BlockPos pendingBoxCorner;

    private ClientSelectionHandler() {
    }

    public static Optional<BlockSelection> selection() {
        return Optional.ofNullable(selection);
    }

    public static Optional<BlockPos> pendingBoxCorner() {
        return Optional.ofNullable(pendingBoxCorner);
    }

    public static void replace(SelectionUpdatedPayload payload, Runnable cancelPreview) {
        selection = payload.positions().isEmpty() ? null : new BlockSelection(payload.positions(), payload.pivot());
        pendingBoxCorner = payload.pendingBoxCorner();
        cancelPreview.run();
    }

    public static void reset(Runnable cancelPreview) {
        selection = null;
        pendingBoxCorner = null;
        cancelPreview.run();
    }
}
