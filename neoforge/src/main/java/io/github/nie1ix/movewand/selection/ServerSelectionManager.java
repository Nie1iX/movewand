package io.github.nie1ix.movewand.selection;

import io.github.nie1ix.movewand.network.SelectionUpdatedPayload;
import io.github.nie1ix.movewand.move.engine.MoveHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ServerSelectionManager {
    private static final Map<UUID, SelectionEditor> EDITORS = new HashMap<>();

    private ServerSelectionManager() {
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EDITORS.remove(event.getEntity().getUUID());
    }

    public static void select(ServerPlayer player, BlockPos position, boolean individualBlock) {
        SelectionEditor editor = EDITORS.computeIfAbsent(player.getUUID(), ignored -> new SelectionEditor());
        if (individualBlock) {
            editor.toggleBlocks(
                    MoveHooks.expandSelection(
                            player.level(),
                            StructureSelection.expandPairedBlocks(Set.of(position), player.level()::getBlockState)
                    ),
                    position
            );
            showSelectionSize(player, editor);
            sendSelectionUpdate(player, editor);
            return;
        }

        if (editor.selectBoxCorner(position)) {
            if (editor.pendingBoxCorner().isPresent()) {
                player.sendOverlayMessage(Component.translatable("message.movewand.selection.first_corner"));
            } else if (expandPairedBlocks(player, editor)) {
                showSelectionSize(player, editor);
            } else {
                editor.clear();
                player.sendOverlayMessage(Component.translatable("message.movewand.selection.too_large"));
            }
        } else {
            player.sendOverlayMessage(Component.translatable("message.movewand.selection.too_large"));
        }
        sendSelectionUpdate(player, editor);
    }

    public static Optional<BlockSelection> selection(ServerPlayer player) {
        return Optional.ofNullable(EDITORS.get(player.getUUID())).flatMap(SelectionEditor::selection);
    }

    public static void replace(ServerPlayer player, BlockSelection selection) {
        EDITORS.computeIfAbsent(player.getUUID(), ignored -> new SelectionEditor()).replace(selection);
    }

    public static void clear(ServerPlayer player) {
        EDITORS.remove(player.getUUID());
        sendSelectionUpdate(player, null);
    }

    private static void showSelectionSize(ServerPlayer player, SelectionEditor editor) {
        int size = editor.selection()
                .map(selection -> MoveHooks.expandSelection(
                        player.level(),
                        StructureSelection.expandPairedBlocks(selection.positions(), player.level()::getBlockState)
                ).size())
                .orElse(0);
        player.sendOverlayMessage(selectionSizeMessage(size));
    }

    static Component selectionSizeMessage(int size) {
        return size == 0 ? Component.empty() : Component.translatable("message.movewand.selection.size", size);
    }

    public static void sendSelectionUpdate(ServerPlayer player) {
        sendSelectionUpdate(player, EDITORS.get(player.getUUID()));
    }

    private static boolean expandPairedBlocks(ServerPlayer player, SelectionEditor editor) {
        return editor.selection().map(selection -> editor.replace(BlockSelection.of(
                MoveHooks.expandSelection(
                        player.level(),
                        StructureSelection.expandPairedBlocks(selection.positions(), player.level()::getBlockState)
                ),
                selection.pivot()
        ))).orElse(true);
    }

    private static void sendSelectionUpdate(ServerPlayer player, SelectionEditor editor) {
        BlockSelection selection = editor == null ? null : editor.selection().orElse(null);
        BlockPos pendingBoxCorner = editor == null ? null : editor.pendingBoxCorner().orElse(null);
        PacketDistributor.sendToPlayer(player, new SelectionUpdatedPayload(
                selection == null ? Set.of() : selection.positions(),
                selection == null ? null : selection.pivot(),
                pendingBoxCorner
        ));
    }
}
