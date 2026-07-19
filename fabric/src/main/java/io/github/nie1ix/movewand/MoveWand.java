package io.github.nie1ix.movewand;

import net.fabricmc.api.ModInitializer;
import io.github.nie1ix.movewand.interaction.FabricBlockInteraction;
import io.github.nie1ix.movewand.network.MoveWandNetworking;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;

public final class MoveWand implements ModInitializer {
    public static final String MOD_ID = "movewand";

    @Override
    public void onInitialize() {
        ModItems.initialize();
        ServerSelectionManager.initialize();
        MoveWandNetworking.initialize();
        FabricBlockInteraction.initialize();
    }
}
