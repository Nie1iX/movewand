package io.github.nie1ix.movewand.move;

import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class MoveService {
    public static final int MAX_OFFSET_DISTANCE = 16;

    private MoveService() {
    }

    public static void move(ServerPlayer player, int x, int y, int z, int clockwiseTurns) {
        if (!player.getMainHandItem().is(ModItems.moveWand())) {
            return;
        }

        int turns = Math.floorMod(clockwiseTurns, 4);
        if (!hasValidOffset(x, y, z) && turns == 0) {
            player.sendOverlayMessage(Component.translatable("message.movewand.move.no_change"));
            return;
        }
        if (!new BlockPos(x, y, z).equals(BlockPos.ZERO) && !hasValidOffset(x, y, z)) {
            player.sendOverlayMessage(Component.translatable("message.movewand.move.too_far"));
            return;
        }

        Optional<BlockSelection> selected = ServerSelectionManager.selection(player);
        if (selected.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.movewand.selection.empty"));
            return;
        }

        MoveEngine.move(player, selected.get(), x, y, z, turns);
    }

    public static boolean hasValidOffset(int x, int y, int z) {
        long squaredDistance = (long) x * x + (long) y * y + (long) z * z;
        return squaredDistance > 0 && squaredDistance <= (long) MAX_OFFSET_DISTANCE * MAX_OFFSET_DISTANCE;
    }

    static int sourceClearFlags() {
        return MoveEngine.sourceClearFlags();
    }

    static CompoundTag relocatedBlockEntityData(CompoundTag snapshot, BlockPos destination) {
        return MoveEngine.relocatedBlockEntityData(snapshot, destination);
    }
}
