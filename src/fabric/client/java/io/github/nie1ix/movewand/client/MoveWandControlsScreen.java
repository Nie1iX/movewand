package io.github.nie1ix.movewand.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class MoveWandControlsScreen extends Screen {
    private final Screen parent;
    private KeyMapping selectedBinding;

    public MoveWandControlsScreen(Screen parent) {
        super(Component.translatable("screen.movewand.key_bindings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int top = height / 2 - 76;
        for (int index = 0; index < MoveKeyBindings.all().size(); index++) {
            KeyMapping binding = MoveKeyBindings.all().get(index);
            int column = index % 2;
            int row = index / 2;
            addRenderableWidget(Button.builder(bindingLabel(binding), button -> {
                selectedBinding = binding;
                button.setMessage(Component.translatable("screen.movewand.press_key"));
            }).bounds(centerX - 152 + column * 154, top + row * 22, 150, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(centerX - 75, top + 140, 150, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 100, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        minecraft.options.save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedBinding == null) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
            selectedBinding.setKey(InputConstants.getKey(keyCode, scanCode));
            KeyMapping.resetMapping();
        }
        selectedBinding = null;
        init();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedBinding == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        selectedBinding.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
        KeyMapping.resetMapping();
        selectedBinding = null;
        init();
        return true;
    }

    private Component bindingLabel(KeyMapping binding) {
        return Component.translatable(
                "screen.movewand.binding",
                Component.translatable(binding.getName()),
                binding.getTranslatedKeyMessage()
        );
    }
}
