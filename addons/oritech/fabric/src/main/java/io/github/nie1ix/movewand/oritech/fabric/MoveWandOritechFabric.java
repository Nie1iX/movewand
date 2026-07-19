package io.github.nie1ix.movewand.oritech.fabric;

import io.github.nie1ix.movewand.move.engine.MoveHooks;
import io.github.nie1ix.movewand.oritech.OritechMoveIntegration;
import net.fabricmc.api.ModInitializer;

public final class MoveWandOritechFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MoveHooks.register(new OritechMoveIntegration());
    }
}
