package io.github.nie1ix.movewand.oritech.neoforge;

import io.github.nie1ix.movewand.move.integration.MoveIntegrations;
import io.github.nie1ix.movewand.oritech.OritechMoveIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("movewand_oritech")
public final class MoveWandOritechNeoForge {
    public MoveWandOritechNeoForge(IEventBus modEventBus) {
        MoveIntegrations.register(new OritechMoveIntegration());
    }
}
