package io.github.nie1ix.movewand.move.engine;

import io.github.nie1ix.movewand.move.vanilla.VanillaMoveHook;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MoveHooks {
    private static final MoveHook VANILLA = new VanillaMoveHook();
    private static final CopyOnWriteArrayList<MoveHook> HOOKS = new CopyOnWriteArrayList<>();

    private MoveHooks() {
    }

    public static void register(MoveHook hook) {
        HOOKS.addIfAbsent(hook);
    }

    public static List<MoveHook> all() {
        List<MoveHook> hooks = new ArrayList<>(HOOKS.size() + 1);
        hooks.add(VANILLA);
        hooks.addAll(HOOKS);
        return List.copyOf(hooks);
    }

    public static Set<BlockPos> expandSelection(ServerLevel level, Set<BlockPos> positions) {
        return expandSelection(level, positions, all());
    }

    public static Set<BlockPos> expandSelection(
            ServerLevel level,
            Set<BlockPos> positions,
            List<MoveHook> hooks
    ) {
        Set<BlockPos> expanded = new LinkedHashSet<>(positions);
        for (MoveHook hook : hooks) {
            expanded = new LinkedHashSet<>(hook.expandSelection(level, expanded));
        }
        return expanded;
    }

    public static BlockState transformBlockState(
            BlockState state,
            int clockwiseTurns,
            List<MoveHook> hooks
    ) {
        BlockState transformed = state;
        for (MoveHook hook : hooks) {
            transformed = hook.transformBlockState(transformed, clockwiseTurns);
        }
        return transformed;
    }
}
