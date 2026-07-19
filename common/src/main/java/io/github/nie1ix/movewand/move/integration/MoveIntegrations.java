package io.github.nie1ix.movewand.move.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MoveIntegrations {
    private static final CopyOnWriteArrayList<MoveIntegration> INTEGRATIONS = new CopyOnWriteArrayList<>();

    private MoveIntegrations() {
    }

    public static void register(MoveIntegration integration) {
        INTEGRATIONS.addIfAbsent(integration);
    }

    public static List<MoveIntegration> all() {
        return List.copyOf(INTEGRATIONS);
    }

    public static Set<BlockPos> expandSelection(ServerLevel level, Set<BlockPos> positions) {
        return expandSelection(level, positions, all());
    }

    public static BlockState transformBlockState(BlockState state, int clockwiseTurns) {
        return transformBlockState(state, clockwiseTurns, all());
    }

    static Set<BlockPos> expandSelection(
            ServerLevel level,
            Set<BlockPos> positions,
            List<MoveIntegration> integrations
    ) {
        Set<BlockPos> expanded = new LinkedHashSet<>(positions);
        for (MoveIntegration integration : integrations) {
            expanded = new LinkedHashSet<>(integration.expandSelection(level, expanded));
        }
        return expanded;
    }

    static BlockState transformBlockState(
            BlockState state,
            int clockwiseTurns,
            List<MoveIntegration> integrations
    ) {
        BlockState transformed = state;
        for (MoveIntegration integration : integrations) {
            transformed = integration.transformBlockState(transformed, clockwiseTurns);
        }
        return transformed;
    }
}
