package io.github.nie1ix.movewand.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.network.ClearSelectionPayload;
import io.github.nie1ix.movewand.network.ExpandSelectionPayload;
import io.github.nie1ix.movewand.transform.RelativeMove;

import java.util.List;

public final class MoveKeyBindings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("movewand", "movewand"));
    private static final KeyMapping FORWARD = register("key.movewand.move_forward", InputConstants.KEY_UP);
    private static final KeyMapping BACKWARD = register("key.movewand.move_backward", InputConstants.KEY_DOWN);
    private static final KeyMapping LEFT = register("key.movewand.move_left", InputConstants.KEY_LEFT);
    private static final KeyMapping RIGHT = register("key.movewand.move_right", InputConstants.KEY_RIGHT);
    private static final KeyMapping ROTATE_CLOCKWISE = register("key.movewand.rotate_clockwise", InputConstants.KEY_RBRACKET);
    private static final KeyMapping ROTATE_COUNTER_CLOCKWISE = register("key.movewand.rotate_counter_clockwise", InputConstants.KEY_LBRACKET);
    private static final KeyMapping APPLY = register("key.movewand.apply", InputConstants.KEY_RETURN);
    private static final KeyMapping CANCEL = register("key.movewand.cancel", InputConstants.KEY_BACKSPACE);
    private static final KeyMapping CLEAR_SELECTION = register("key.movewand.clear_selection", InputConstants.KEY_DELETE);

    private MoveKeyBindings() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
                ClientPlayNetworking.send(new ClearSelectionPayload());
            }
        });
    }

    public static List<KeyMapping> all() {
        return List.of(
                FORWARD, BACKWARD, LEFT, RIGHT,
                ROTATE_CLOCKWISE, ROTATE_COUNTER_CLOCKWISE,
                APPLY, CANCEL, CLEAR_SELECTION
        );
    }

    private static KeyMapping register(String translationKey, int keyCode) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translationKey,
                InputConstants.Type.KEYBOARD,
                keyCode,
                CATEGORY
        ));
    }

    private static void addWhenPressed(KeyMapping key, RelativeMove baseMove, net.minecraft.core.Direction horizontalView) {
        while (key.consumeClick()) {
            RelativeMove move = effectiveMove(baseMove);
            if (TransformPreview.isActive()) {
                TransformPreview.add(move, horizontalView);
            } else if (isLeftShiftDown()) {
                ClientPlayNetworking.send(new ExpandSelectionPayload(move.id(), true));
            } else if (isCtrlDown()) {
                ClientPlayNetworking.send(new ExpandSelectionPayload(move.id(), false));
            } else {
                TransformPreview.add(move, horizontalView);
            }
        }
    }

    // Right Shift reinterprets forward/backward as up/down, so height is always "Shift + arrow"
    // without taking the Left-Shift-expands-the-selection shortcut away from forward/backward.
    private static RelativeMove effectiveMove(RelativeMove baseMove) {
        if (isRightShiftDown()) {
            if (baseMove == RelativeMove.FORWARD) {
                return RelativeMove.UP;
            }
            if (baseMove == RelativeMove.BACKWARD) {
                return RelativeMove.DOWN;
            }
        }
        return baseMove;
    }

    private static boolean isLeftShiftDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT);
    }

    private static boolean isRightShiftDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }

    private static boolean isCtrlDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
    }

    private static void discardPendingClicks() {
        for (KeyMapping binding : all()) {
            while (binding.consumeClick()) {
                // Ignore MoveWand input while the player is not holding the wand.
            }
        }
    }
}
