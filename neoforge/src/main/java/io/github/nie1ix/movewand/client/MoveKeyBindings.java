package io.github.nie1ix.movewand.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.nie1ix.movewand.MoveWand;
import io.github.nie1ix.movewand.network.ClearSelectionPayload;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.transform.RelativeMove;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MoveKeyBindings {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            ResourceLocation.fromNamespaceAndPath(MoveWand.MOD_ID, "main")
    );
    private static final KeyMapping FORWARD = key("key.movewand.move_forward", GLFW.GLFW_KEY_UP);
    private static final KeyMapping BACKWARD = key("key.movewand.move_backward", GLFW.GLFW_KEY_DOWN);
    private static final KeyMapping LEFT = key("key.movewand.move_left", GLFW.GLFW_KEY_LEFT);
    private static final KeyMapping RIGHT = key("key.movewand.move_right", GLFW.GLFW_KEY_RIGHT);
    private static final KeyMapping UP = key("key.movewand.move_up", GLFW.GLFW_KEY_R);
    private static final KeyMapping DOWN = key("key.movewand.move_down", GLFW.GLFW_KEY_V);
    private static final KeyMapping ROTATE_CLOCKWISE = key("key.movewand.rotate_clockwise", GLFW.GLFW_KEY_RIGHT_BRACKET);
    private static final KeyMapping ROTATE_COUNTER_CLOCKWISE = key("key.movewand.rotate_counter_clockwise", GLFW.GLFW_KEY_LEFT_BRACKET);
    private static final KeyMapping APPLY = key("key.movewand.apply", GLFW.GLFW_KEY_ENTER);
    private static final KeyMapping CANCEL = key("key.movewand.cancel", GLFW.GLFW_KEY_BACKSPACE);
    private static final KeyMapping CLEAR_SELECTION = key("key.movewand.clear_selection", GLFW.GLFW_KEY_DELETE);

    private MoveKeyBindings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        all().forEach(event::register);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !client.player.getMainHandItem().is(ModItems.moveWand())) {
            discardPendingClicks();
            return;
        }

        while (CANCEL.consumeClick()) {
            TransformPreview.cancel();
        }

        addWhenPressed(FORWARD, RelativeMove.FORWARD, client.player.getDirection());
        addWhenPressed(BACKWARD, RelativeMove.BACKWARD, client.player.getDirection());
        addWhenPressed(LEFT, RelativeMove.LEFT, client.player.getDirection());
        addWhenPressed(RIGHT, RelativeMove.RIGHT, client.player.getDirection());
        addWhenPressed(UP, RelativeMove.UP, client.player.getDirection());
        addWhenPressed(DOWN, RelativeMove.DOWN, client.player.getDirection());
        while (ROTATE_CLOCKWISE.consumeClick()) {
            TransformPreview.rotateClockwise();
        }
        while (ROTATE_COUNTER_CLOCKWISE.consumeClick()) {
            TransformPreview.rotateCounterClockwise();
        }
        while (APPLY.consumeClick()) {
            TransformPreview.apply();
        }
        while (CLEAR_SELECTION.consumeClick()) {
            ClientPacketDistributor.sendToServer(new ClearSelectionPayload());
        }
    }

    public static List<KeyMapping> all() {
        return List.of(
                FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN,
                ROTATE_CLOCKWISE, ROTATE_COUNTER_CLOCKWISE,
                APPLY, CANCEL, CLEAR_SELECTION
        );
    }

    private static KeyMapping key(String translationKey, int keyCode) {
        return new KeyMapping(translationKey, InputConstants.Type.KEYSYM, keyCode, CATEGORY);
    }

    private static void addWhenPressed(KeyMapping key, RelativeMove move, Direction horizontalView) {
        while (key.consumeClick()) {
            TransformPreview.add(move, horizontalView);
        }
    }

    private static void discardPendingClicks() {
        for (KeyMapping binding : all()) {
            while (binding.consumeClick()) {
                // Ignore MoveWand input while the player is not holding the wand.
            }
        }
    }
}
