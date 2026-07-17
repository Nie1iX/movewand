package io.github.nie1ix.movewand.client;

import net.minecraft.core.BlockPos;
import io.github.nie1ix.movewand.network.SelectionUpdatedPayload;
import io.github.nie1ix.movewand.selection.BlockSelection;

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

    public static void replace(SelectionUpdatedPayload payload) {
        selection = payload.positions().isEmpty() ? null : new BlockSelection(payload.positions(), payload.pivot());
        pendingBoxCorner = payload.pendingBoxCorner();
        TransformPreview.cancel();
    }

    public static void reset() {
        selection = null;
        pendingBoxCorner = null;
        TransformPreview.cancel();
    }
}
