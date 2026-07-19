package io.github.nie1ix.movewand.client;

import net.minecraft.world.level.block.RenderShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewRendererTest {
    @Test
    void limitsGhostAlphaWithoutIncreasingIt() {
        assertEquals(96, PreviewRenderer.ghostAlpha(255));
        assertEquals(48, PreviewRenderer.ghostAlpha(48));
    }

    @Test
    void usesBlockAtlasExceptForEntityBlocks() {
        assertTrue(PreviewRenderer.usesBlockAtlasGhost(RenderShape.MODEL));
        assertFalse(PreviewRenderer.usesBlockAtlasGhost(RenderShape.ENTITYBLOCK_ANIMATED));
    }
}
