package io.github.nie1ix.movewand.registry;

import io.github.nie1ix.movewand.MoveWand;
import io.github.nie1ix.movewand.item.MoveWandItem;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(MoveWand.MOD_ID)
public final class NeoForgeModItemsGameTest {
    @GameTest(template = "empty")
    public static void registersMoveWand(GameTestHelper context) {
        context.assertTrue(ModItems.moveWand() instanceof MoveWandItem, "MoveWand must be registered as MoveWandItem");
        context.succeed();
    }
}
