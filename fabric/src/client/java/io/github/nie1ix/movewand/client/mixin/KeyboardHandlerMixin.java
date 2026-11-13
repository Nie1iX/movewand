package io.github.nie1ix.movewand.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.client.TransformPreview;

@Mixin(KeyboardHandler.class)
final class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void cancelPreviewBeforeOpeningThePauseMenu(long window, int action, KeyEvent event, CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.key() == InputConstants.KEY_ESCAPE
                && action == InputConstants.PRESS
                && minecraft.gui.screen() == null
                && minecraft.player != null
                && minecraft.player.getMainHandItem().is(ModItems.moveWand())
                && TransformPreview.isActive()) {
            TransformPreview.cancel();
            callback.cancel();
        }
    }
}
