package io.github.nie1ix.movewand.client;

import net.minecraft.core.BlockPos;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.RenderShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformPreviewTest {
    @Test
    void acceptsZeroOffsetForRotationOnlyPreview() {
        assertTrue(TransformPreview.isOffsetWithinRange(BlockPos.ZERO));
    }

    @Test
    void rejectsOffsetsBeyondTheMovementLimit() {
        assertFalse(TransformPreview.isOffsetWithinRange(new BlockPos(0, 0, 17)));
    }

    @Test
    void rendersSelectionOnlyWhileHoldingTheMoveWand() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        assertTrue(PreviewRenderer.shouldRenderSelection(new ItemStack(Items.STICK), Items.STICK));
        assertFalse(PreviewRenderer.shouldRenderSelection(new ItemStack(Items.DIRT), Items.STICK));
    }

    @Test
    void limitsGhostBlockAlphaWithoutMakingTransparentVerticesOpaque() {
        assertEquals(96, PreviewRenderer.ghostAlpha(255));
        assertEquals(48, PreviewRenderer.ghostAlpha(48));
    }

    @Test
    void preservesTheNativeLayerForEntityRenderedBlocks() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        assertTrue(PreviewRenderer.usesBlockAtlasGhost(RenderShape.MODEL));
        assertFalse(PreviewRenderer.usesBlockAtlasGhost(RenderShape.ENTITYBLOCK_ANIMATED));
    }
}
