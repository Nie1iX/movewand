package io.github.nie1ix.movewand.client;

import io.github.nie1ix.movewand.MoveWand;
import io.github.nie1ix.movewand.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class MoveWandClient {
    private MoveWandClient() {
    }

    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(MoveKeyBindings::register);
        NeoForge.EVENT_BUS.addListener(MoveKeyBindings::onClientTick);
        NeoForge.EVENT_BUS.addListener(PreviewRenderer::render);
        NeoForge.EVENT_BUS.addListener(MoveWandClient::onScreenOpening);
        NeoForge.EVENT_BUS.addListener(MoveWandClient::onPlayerLoggingOut);
    }

    private static void onScreenOpening(ScreenEvent.Opening event) {
        Minecraft client = Minecraft.getInstance();
        if (event.getCurrentScreen() == null
                && event.getNewScreen() instanceof PauseScreen
                && client.player != null
                && client.player.getMainHandItem().is(ModItems.moveWand())
                && TransformPreview.isActive()) {
            TransformPreview.cancel();
            event.setNewScreen(null);
        }
    }

    private static void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSelectionHandler.reset();
    }
}
