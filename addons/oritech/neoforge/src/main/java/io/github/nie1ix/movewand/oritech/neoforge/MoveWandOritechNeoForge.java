package io.github.nie1ix.movewand.oritech.neoforge;

import io.github.nie1ix.movewand.move.engine.MoveHooks;
import io.github.nie1ix.movewand.oritech.OritechMoveIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("movewand_oritech")
public final class MoveWandOritechNeoForge {
    public MoveWandOritechNeoForge(IEventBus modEventBus) {
        MoveHooks.register(new OritechMoveIntegration());
    }
}
