package io.github.nie1ix.movewand.item;

import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveWandItemTest {
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
