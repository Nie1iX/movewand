package io.github.nie1ix.movewand.interaction;

import io.github.nie1ix.movewand.item.MoveWandItem;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class FabricBlockInteraction {
    private FabricBlockInteraction() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!MoveWandItem.capturesBlockUse(hand, player.getItemInHand(hand).is(ModItems.moveWand()))) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                ServerSelectionManager.select(serverPlayer, hitResult.getBlockPos(), player.isShiftKeyDown());
            }
            return InteractionResult.SUCCESS;
        });
    }
}
