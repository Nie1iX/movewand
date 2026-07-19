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

public record SelectionUpdatedPayload(Set<BlockPos> positions, BlockPos pivot, BlockPos pendingBoxCorner)
        implements CustomPacketPayload {
    public static final Type<SelectionUpdatedPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MoveWand.MOD_ID, "selection_updated")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectionUpdatedPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.positions().size());
                for (BlockPos position : payload.positions()) {
                    buffer.writeBlockPos(position);
                }
                buffer.writeBoolean(payload.pivot() != null);
                if (payload.pivot() != null) {
                    buffer.writeBlockPos(payload.pivot());
                }
                buffer.writeBoolean(payload.pendingBoxCorner() != null);
                if (payload.pendingBoxCorner() != null) {
                    buffer.writeBlockPos(payload.pendingBoxCorner());
                }
            },
            buffer -> {
                int size = buffer.readVarInt();
                if (size < 0 || size > SelectionEditor.DEFAULT_MAX_POSITIONS) {
                    throw new IllegalArgumentException("Invalid selection size: " + size);
                }

                Set<BlockPos> positions = new LinkedHashSet<>();
                for (int index = 0; index < size; index++) {
                    positions.add(buffer.readBlockPos());
                }
                BlockPos pivot = buffer.readBoolean() ? buffer.readBlockPos() : null;
                BlockPos pendingBoxCorner = buffer.readBoolean() ? buffer.readBlockPos() : null;
                return new SelectionUpdatedPayload(Set.copyOf(positions), pivot, pendingBoxCorner);
            }
    );

    public SelectionUpdatedPayload {
        positions = Set.copyOf(positions);
        if (positions.size() > SelectionEditor.DEFAULT_MAX_POSITIONS) {
            throw new IllegalArgumentException("Selection size must not exceed " + SelectionEditor.DEFAULT_MAX_POSITIONS);
        }
        if (positions.isEmpty() != (pivot == null)) {
            throw new IllegalArgumentException("Only a non-empty selection may have a pivot");
        }
        if (pivot != null && !positions.contains(pivot)) {
            throw new IllegalArgumentException("Pivot must belong to the selection");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
