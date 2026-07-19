package io.github.nie1ix.movewand.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import io.github.nie1ix.movewand.MoveWand;
import io.github.nie1ix.movewand.item.MoveWandItem;

public final class ModItems {
    public static final Item MOVE_WAND = register("move_wand", new MoveWandItem(new Item.Properties()));

    private ModItems() {
    }

    public static void initialize() {
    }

    public static Item moveWand() {
        return MOVE_WAND;
    }

    private static Item register(String path, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MoveWand.MOD_ID, path), item);
    }
}
