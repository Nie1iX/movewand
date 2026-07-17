package io.github.nie1ix.movewand.selection;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerSelectionManagerTest {
    @Test
    void clearsTheActionbarWhenNoBlocksRemainSelected() {
        assertEquals(Component.empty(), ServerSelectionManager.selectionSizeMessage(0));
    }
}
