package io.github.nie1ix.movewand.registry;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class ModItemsGameTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void exposesTheMoveWandThroughTheLoaderNeutralAccessor(GameTestHelper context) {
        context.assertTrue(ModItems.moveWand() == ModItems.MOVE_WAND, "moveWand must return the registered item");
        context.succeed();
    }
}
