package io.github.nie1ix.movewand.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import io.github.nie1ix.movewand.network.SelectionUpdatedPayload;

public final class MoveWandClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MoveKeyBindings.initialize();
        ClientPlayNetworking.registerGlobalReceiver(SelectionUpdatedPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientSelectionHandler.replace(payload, TransformPreview::cancel))
        );
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientSelectionHandler.reset(TransformPreview::cancel));
    }
}
