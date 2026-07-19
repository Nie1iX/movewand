package io.github.nie1ix.movewand.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.github.nie1ix.movewand.MoveWand;

public record MoveRequestPayload(int x, int y, int z, int clockwiseTurns) implements CustomPacketPayload {
    public static final Type<MoveRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MoveWand.MOD_ID, "move_request")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MoveRequestPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MoveRequestPayload::x,
            ByteBufCodecs.VAR_INT,
            MoveRequestPayload::y,
            ByteBufCodecs.VAR_INT,
            MoveRequestPayload::z,
            ByteBufCodecs.VAR_INT,
            MoveRequestPayload::clockwiseTurns,
            MoveRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
