package io.github.nie1ix.movewand.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import io.github.nie1ix.movewand.network.ClearSelectionPayload;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.transform.RelativeMove;

public final class MoveWandControlsScreen extends Screen {
    private final Screen parent;

    public MoveWandControlsScreen(Screen parent) {
        super(Component.translatable("screen.movewand.controls"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int top = height / 2 - 48;
        addRenderableWidget(button(Component.literal("↑"), centerX - 20, top, 40, () -> move(RelativeMove.FORWARD)));
        addRenderableWidget(button(Component.literal("←"), centerX - 64, top + 24, 40, () -> move(RelativeMove.LEFT)));
        addRenderableWidget(button(Component.literal("↓"), centerX - 20, top + 24, 40, () -> move(RelativeMove.BACKWARD)));
        addRenderableWidget(button(Component.literal("→"), centerX + 24, top + 24, 40, () -> move(RelativeMove.RIGHT)));
        addRenderableWidget(button(Component.translatable("screen.movewand.move_up"), centerX - 108, top, 40, () -> move(RelativeMove.UP)));
        addRenderableWidget(button(Component.translatable("screen.movewand.move_down"), centerX - 108, top + 24, 40, () -> move(RelativeMove.DOWN)));
        addRenderableWidget(button(Component.literal("⟲"), centerX + 68, top, 40, TransformPreview::rotateCounterClockwise));
        addRenderableWidget(button(Component.literal("⟳"), centerX + 68, top + 24, 40, TransformPreview::rotateClockwise));
        addRenderableWidget(button(Component.translatable("screen.movewand.apply"), centerX - 62, top + 64, 60, TransformPreview::apply));
        addRenderableWidget(button(Component.translatable("screen.movewand.cancel"), centerX + 2, top + 64, 60, TransformPreview::cancel));
        addRenderableWidget(button(Component.translatable("screen.movewand.clear_selection"), centerX - 62, top + 88, 124, this::clearSelection));
        addRenderableWidget(button(Component.translatable("gui.done"), centerX - 62, top + 112, 124, this::onClose));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 78, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private Button button(Component label, int x, int y, int buttonWidth, Runnable action) {
        return Button.builder(label, ignored -> action.run()).bounds(x, y, buttonWidth, 20).build();
    }

    private void move(RelativeMove move) {
        if (minecraft.player != null && minecraft.player.getMainHandItem().is(ModItems.MOVE_WAND)) {
            TransformPreview.add(move, minecraft.player.getDirection());
        }
    }

    private void clearSelection() {
        ClientPlayNetworking.send(new ClearSelectionPayload());
        ClientSelectionHandler.clear();
    }
}
