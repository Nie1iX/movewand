package io.github.nie1ix.movewand.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.github.nie1ix.movewand.MoveWand;

public record ClearSelectionPayload() implements CustomPacketPayload {
    public static final Type<ClearSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MoveWand.MOD_ID, "clear_selection")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearSelectionPayload> CODEC = StreamCodec.unit(new ClearSelectionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
