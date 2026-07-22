package io.github.nie1ix.movewand.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreviewRendererTest {
    @Test
    void limitsGhostAlphaWithoutIncreasingIt() {
        assertEquals(96, PreviewRenderer.ghostAlpha(255));
        assertEquals(48, PreviewRenderer.ghostAlpha(48));
    }

}
