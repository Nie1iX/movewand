package io.github.nie1ix.movewand.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import io.github.nie1ix.movewand.move.MoveService;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.transform.SelectionTransform;

import java.util.Set;

public final class PreviewRenderer {
    private PreviewRenderer() {
    }

    public static void initialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.world() == null || context.matrixStack() == null || context.consumers() == null) {
                return;
            }

            ClientSelectionHandler.selection().ifPresent(selection -> {
                if (TransformPreview.isActive()) {
                    renderPreview(context, selection);
                } else {
                    renderSelection(context, selection);
                }
            });
        });
    }

    private static void renderPreview(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context, BlockSelection selection) {
        BlockPos offset = TransformPreview.offset();
        Set<BlockPos> targets = Set.copyOf(SelectionTransform.transformMap(selection, offset, TransformPreview.clockwiseTurns()).values());
        boolean withinRange = MoveService.hasValidOffset(offset.getX(), offset.getY(), offset.getZ());
        Vec3 camera = context.camera().getPosition();

        for (BlockPos target : targets) {
            boolean emptyOrOverlapping = selection.positions().contains(target) || context.world().getBlockState(target).isAir();
            boolean valid = withinRange && emptyOrOverlapping;
            float red = valid ? 0.1f : 1.0f;
            float green = valid ? 0.45f : 0.1f;
            float blue = valid ? 1.0f : 0.1f;
            AABB box = new AABB(target).move(-camera.x, -camera.y, -camera.z).inflate(0.002);
            LevelRenderer.renderLineBox(
                    context.matrixStack(),
                    context.consumers().getBuffer(RenderType.lines()),
                    box,
                    red,
                    green,
                    blue,
                    0.8f
            );
        }
    }

    private static void renderSelection(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context, BlockSelection selection) {
        Vec3 camera = context.camera().getPosition();
        for (BlockPos position : selection.positions()) {
            AABB box = new AABB(position).move(-camera.x, -camera.y, -camera.z).inflate(0.002);
            LevelRenderer.renderLineBox(
                    context.matrixStack(),
                    context.consumers().getBuffer(RenderType.lines()),
                    box,
                    0.2f,
                    0.8f,
                    1.0f,
                    0.45f
            );
        }
    }
}
