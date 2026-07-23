package io.github.nie1ix.movewand.network;

import io.github.nie1ix.movewand.client.ClientSelectionHandler;
import io.github.nie1ix.movewand.client.TransformPreview;
import io.github.nie1ix.movewand.move.MoveService;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MoveWandNetworking {
    private MoveWandNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(MoveRequestPayload.TYPE, MoveRequestPayload.CODEC, (payload, context) ->
                MoveService.move((ServerPlayer) context.player(), payload.x(), payload.y(), payload.z(), payload.clockwiseTurns())
        );
        registrar.playToServer(ClearSelectionPayload.TYPE, ClearSelectionPayload.CODEC, (payload, context) -> {
            if (context.player().getMainHandItem().is(ModItems.moveWand())) {
                ServerSelectionManager.clear((ServerPlayer) context.player());
            }
        });
        registrar.playToClient(SelectionUpdatedPayload.TYPE, SelectionUpdatedPayload.CODEC, (payload, context) ->
                ClientSelectionHandler.replace(payload, TransformPreview::cancel)
        );
    }
}
