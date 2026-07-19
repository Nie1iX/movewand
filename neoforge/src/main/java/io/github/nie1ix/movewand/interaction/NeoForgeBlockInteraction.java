package io.github.nie1ix.movewand.interaction;

import io.github.nie1ix.movewand.item.MoveWandItem;
import io.github.nie1ix.movewand.registry.ModItems;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class NeoForgeBlockInteraction {
    private NeoForgeBlockInteraction() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (MoveWandItem.capturesBlockUse(event.getHand(), event.getItemStack().is(ModItems.moveWand()))) {
            event.setUseBlock(TriState.FALSE);
        }
    }
}
