package io.github.nie1ix.movewand.move.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public interface MoveIntegration {
    default Set<BlockPos> expandSelection(ServerLevel level, Set<BlockPos> positions) {
        return positions;
    }

    default void transformBlockEntityData(MoveContext context) {
    }

    default void afterMove(MoveContext context) {
    }
}
