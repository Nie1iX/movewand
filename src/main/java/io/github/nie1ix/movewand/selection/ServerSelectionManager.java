package io.github.nie1ix.movewand.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ServerSelectionManager {
    private static final Map<UUID, SelectionEditor> EDITORS = new HashMap<>();

    private ServerSelectionManager() {
    }

    public static void initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> EDITORS.remove(handler.player.getUUID()));
    }

    public static void select(ServerPlayer player, BlockPos position, boolean individualBlock) {
        SelectionEditor editor = EDITORS.computeIfAbsent(player.getUUID(), ignored -> new SelectionEditor());
        if (individualBlock) {
            editor.toggleBlock(position);
            showSelectionSize(player, editor);
            return;
        }

        if (editor.selectBoxCorner(position)) {
            if (editor.pendingBoxCorner().isPresent()) {
                player.displayClientMessage(Component.translatable("message.movewand.selection.first_corner"), true);
            } else {
                showSelectionSize(player, editor);
            }
        } else {
            player.displayClientMessage(Component.translatable("message.movewand.selection.too_large"), true);
        }
    }

    public static Optional<BlockSelection> selection(ServerPlayer player) {
        return Optional.ofNullable(EDITORS.get(player.getUUID())).flatMap(SelectionEditor::selection);
    }

    public static void replace(ServerPlayer player, BlockSelection selection) {
        EDITORS.computeIfAbsent(player.getUUID(), ignored -> new SelectionEditor()).replace(selection);
    }

    public static void clear(ServerPlayer player) {
        EDITORS.remove(player.getUUID());
    }

    private static void showSelectionSize(ServerPlayer player, SelectionEditor editor) {
        int size = editor.selection().map(selection -> selection.positions().size()).orElse(0);
        player.displayClientMessage(Component.translatable("message.movewand.selection.size", size), true);
    }
}
