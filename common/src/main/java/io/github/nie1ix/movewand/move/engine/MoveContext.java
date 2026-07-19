package io.github.nie1ix.movewand.move.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

public record MoveContext(
        ServerLevel level,
        Map<BlockPos, BlockPos> destinations,
        Map<BlockPos, CompoundTag> blockEntityData,
        int clockwiseTurns
) {
    public MoveContext {
        destinations = Map.copyOf(destinations);
        blockEntityData = Map.copyOf(blockEntityData);
    }
}
