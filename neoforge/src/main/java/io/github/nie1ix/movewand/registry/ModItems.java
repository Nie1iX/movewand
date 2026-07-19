package io.github.nie1ix.movewand.registry;

import io.github.nie1ix.movewand.MoveWand;
import io.github.nie1ix.movewand.item.MoveWandItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MoveWand.MOD_ID);
    public static final DeferredItem<MoveWandItem> MOVE_WAND = ITEMS.registerItem("move_wand", MoveWandItem::new);

    private ModItems() {
    }

    public static Item moveWand() {
        return MOVE_WAND.get();
    }
}
