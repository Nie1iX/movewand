package io.github.nie1ix.movewand;

import io.github.nie1ix.movewand.client.MoveWandClient;
import io.github.nie1ix.movewand.interaction.NeoForgeBlockInteraction;
import io.github.nie1ix.movewand.network.MoveWandNetworking;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MoveWand.MOD_ID)
public final class MoveWand {
    public static final String MOD_ID = "movewand";

    public MoveWand(IEventBus modEventBus, Dist dist) {
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(MoveWandNetworking::registerPayloads);
        NeoForge.EVENT_BUS.addListener(ServerSelectionManager::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(NeoForgeBlockInteraction::onRightClickBlock);
        if (dist == Dist.CLIENT) {
            MoveWandClient.initialize(modEventBus);
        }
    }
}
