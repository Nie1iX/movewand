package io.github.nie1ix.movewand.ae2.neoforge;

import io.github.nie1ix.movewand.ae2.AE2MoveIntegration;
import io.github.nie1ix.movewand.move.engine.MoveHooks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("movewand_ae2")
public final class MoveWandAE2NeoForge {
    public MoveWandAE2NeoForge(IEventBus modEventBus) {
        MoveHooks.register(new AE2MoveIntegration());
    }
}
