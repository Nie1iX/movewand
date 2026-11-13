package io.github.nie1ix.movewand.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import io.github.nie1ix.movewand.MoveWand;
import io.github.nie1ix.movewand.item.MoveWandItem;

public final class ModItems {
    public static final Item MOVE_WAND = register("move_wand");

    private ModItems() {
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept((ItemLike) MOVE_WAND));
    }

    public static Item moveWand() {
        return MOVE_WAND;
    }

    private static Item register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(MoveWand.MOD_ID, path);
        Item item = new MoveWandItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }
}
