package io.github.nie1ix.movewand.client.mixin;

import io.github.nie1ix.movewand.client.PreviewRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
final class LevelRendererMixin {
    @Inject(
            method = "method_62214",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V", ordinal = 0)
    )
    private void renderMoveWandPreview(CallbackInfo callback) {
        PreviewRenderer.render();
    }
}
