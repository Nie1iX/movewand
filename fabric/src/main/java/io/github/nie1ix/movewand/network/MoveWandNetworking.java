package io.github.nie1ix.movewand.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.github.nie1ix.movewand.move.MoveService;

public final class MoveWandNetworking {
    private MoveWandNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(MoveRequestPayload.TYPE, MoveRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClearSelectionPayload.TYPE, ClearSelectionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SelectionUpdatedPayload.TYPE, SelectionUpdatedPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MoveRequestPayload.TYPE, (payload, context) ->
                MoveService.move(context.player(), payload.x(), payload.y(), payload.z(), payload.clockwiseTurns())
        );
        ServerPlayNetworking.registerGlobalReceiver(ClearSelectionPayload.TYPE, (payload, context) -> {
            if (context.player().getMainHandItem().is(io.github.nie1ix.movewand.registry.ModItems.moveWand())) {
                io.github.nie1ix.movewand.selection.ServerSelectionManager.clear(context.player());
            }
        });
    }
}
