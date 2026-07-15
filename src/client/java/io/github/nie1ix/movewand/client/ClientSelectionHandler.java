package io.github.nie1ix.movewand.client;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.SelectionEditor;

import java.util.Optional;

public final class ClientSelectionHandler {
    private static final SelectionEditor EDITOR = new SelectionEditor();

    private ClientSelectionHandler() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND || !player.getItemInHand(hand).is(ModItems.MOVE_WAND)) {
                return InteractionResult.PASS;
            }

            if (player.isShiftKeyDown()) {
                EDITOR.toggleBlock(hitResult.getBlockPos());
            } else {
                EDITOR.selectBoxCorner(hitResult.getBlockPos());
            }
            TransformPreview.cancel();
            return InteractionResult.PASS;
        });
    }

    public static Optional<io.github.nie1ix.movewand.selection.BlockSelection> selection() {
        return EDITOR.selection();
    }

    public static void replace(io.github.nie1ix.movewand.selection.BlockSelection selection) {
        EDITOR.replace(selection);
        TransformPreview.cancel();
    }

    public static void clear() {
        EDITOR.clear();
        TransformPreview.cancel();
    }
}
