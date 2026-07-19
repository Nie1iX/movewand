package io.github.nie1ix.movewand.move.integration;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveIntegrationTest {
    @Test
    void passesExpandedSelectionToFollowingIntegrations() {
        BlockPos controller = new BlockPos(1, 2, 3);
        BlockPos core = new BlockPos(2, 2, 3);
        MoveIntegration addController = new MoveIntegration() {
            @Override
            public Set<BlockPos> expandSelection(net.minecraft.server.level.ServerLevel level, Set<BlockPos> positions) {
                return Set.of(controller);
            }
        };
        MoveIntegration addCore = new MoveIntegration() {
            @Override
            public Set<BlockPos> expandSelection(net.minecraft.server.level.ServerLevel level, Set<BlockPos> positions) {
                assertEquals(Set.of(controller), positions);
                return Set.of(controller, core);
            }
        };

        Set<BlockPos> expanded = MoveIntegrations.expandSelection(null, Set.of(), List.of(addController, addCore));

        assertEquals(Set.of(controller, core), expanded);
    }
}
