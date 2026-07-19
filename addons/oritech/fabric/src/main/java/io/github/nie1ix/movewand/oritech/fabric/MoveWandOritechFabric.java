package io.github.nie1ix.movewand.oritech.fabric;

import io.github.nie1ix.movewand.move.integration.MoveIntegrations;
import io.github.nie1ix.movewand.oritech.OritechMoveIntegration;
import net.fabricmc.api.ModInitializer;

public final class MoveWandOritechFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MoveIntegrations.register(new OritechMoveIntegration());
    }
}
