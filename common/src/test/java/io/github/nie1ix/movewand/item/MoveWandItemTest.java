package io.github.nie1ix.movewand.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveWandItemTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void capturesMainHandBlockUse() {
        assertTrue(MoveWandItem.capturesBlockUse(InteractionHand.MAIN_HAND, true));
    }

    @Test
    void doesNotCaptureOffHandOrOtherItems() {
        assertFalse(MoveWandItem.capturesBlockUse(InteractionHand.OFF_HAND, true));
        assertFalse(MoveWandItem.capturesBlockUse(InteractionHand.MAIN_HAND, false));
    }
}
