package io.github.nie1ix.movewand.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.github.nie1ix.movewand.MoveWand;
import io.github.nie1ix.movewand.selection.SelectionEditor;

import java.util.LinkedHashSet;
import java.util.Set;

public record SelectionUpdatedPayload(Set<BlockPos> positions, BlockPos pivot) implements CustomPacketPayload {
    public static final Type<SelectionUpdatedPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MoveWand.MOD_ID, "selection_updated")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectionUpdatedPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.positions().size());
                for (BlockPos position : payload.positions()) {
                    buffer.writeBlockPos(position);
                }
                buffer.writeBlockPos(payload.pivot());
            },
            buffer -> {
                int size = buffer.readVarInt();
                if (size < 1 || size > SelectionEditor.DEFAULT_MAX_POSITIONS) {
                    throw new IllegalArgumentException("Invalid selection size: " + size);
                }

                Set<BlockPos> positions = new LinkedHashSet<>();
                for (int index = 0; index < size; index++) {
                    positions.add(buffer.readBlockPos());
                }
                return new SelectionUpdatedPayload(Set.copyOf(positions), buffer.readBlockPos());
            }
    );

    public SelectionUpdatedPayload {
        positions = Set.copyOf(positions);
        if (positions.isEmpty() || positions.size() > SelectionEditor.DEFAULT_MAX_POSITIONS) {
            throw new IllegalArgumentException("Selection size must be between 1 and " + SelectionEditor.DEFAULT_MAX_POSITIONS);
        }
        if (!positions.contains(pivot)) {
            throw new IllegalArgumentException("Pivot must belong to the selection");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
