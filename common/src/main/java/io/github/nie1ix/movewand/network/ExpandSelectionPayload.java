package io.github.nie1ix.movewand.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.github.nie1ix.movewand.MoveWand;

public record ExpandSelectionPayload(int relativeMoveId, boolean grow) implements CustomPacketPayload {
    public static final Type<ExpandSelectionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MoveWand.MOD_ID, "expand_selection")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ExpandSelectionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ExpandSelectionPayload::relativeMoveId,
            ByteBufCodecs.BOOL,
            ExpandSelectionPayload::grow,
            ExpandSelectionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
