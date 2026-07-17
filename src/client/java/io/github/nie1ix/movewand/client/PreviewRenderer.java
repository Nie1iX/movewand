package io.github.nie1ix.movewand.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.StructureSelection;
import io.github.nie1ix.movewand.move.MoveProjection;
import io.github.nie1ix.movewand.transform.BlockStateTransform;
import io.github.nie1ix.movewand.transform.SelectionTransform;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PreviewRenderer {
    private PreviewRenderer() {
    }

    public static void initialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.world() == null || context.matrixStack() == null || context.consumers() == null) {
                return;
            }

            Vec3 camera = context.camera().getPosition();
            ClientSelectionHandler.pendingBoxCorner().ifPresent(corner -> renderPendingBoxCorner(context, camera, corner));
            ClientSelectionHandler.selection().ifPresent(selection -> {
                BlockSelection expandedSelection = BlockSelection.of(
                        StructureSelection.expandPairedBlocks(selection.positions(), context.world()::getBlockState),
                        selection.pivot()
                );
                if (TransformPreview.isActive()) {
                    renderPreview(context, expandedSelection);
                } else {
                    renderSelection(context, expandedSelection);
                }
            });
        });
    }

    private static void renderPreview(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context, BlockSelection selection) {
        BlockPos offset = TransformPreview.offset();
        Map<BlockPos, BlockPos> targets = SelectionTransform.transformMap(selection, offset, TransformPreview.clockwiseTurns());
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        for (BlockPos source : targets.keySet()) {
            states.put(source, BlockStateTransform.rotateY(
                    context.world().getBlockState(source),
                    TransformPreview.clockwiseTurns()
            ));
        }
        LevelReader projectedLevel = MoveProjection.levelAfterMove(context.world(), states, targets);
        boolean withinRange = TransformPreview.isOffsetWithinRange(offset);
        Vec3 camera = context.camera().getPosition();

        for (Map.Entry<BlockPos, BlockPos> entry : targets.entrySet()) {
            BlockPos target = entry.getValue();
            BlockState state = states.get(entry.getKey());
            boolean emptyOrOverlapping = selection.positions().contains(target) || context.world().getBlockState(target).isAir();
            boolean valid = withinRange && emptyOrOverlapping && state.canSurvive(projectedLevel, target);
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

    private static void renderPendingBoxCorner(
            net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context,
            Vec3 camera,
            BlockPos corner
    ) {
        AABB box = new AABB(corner).move(-camera.x, -camera.y, -camera.z).inflate(0.004);
        LevelRenderer.renderLineBox(
                context.matrixStack(),
                context.consumers().getBuffer(RenderType.lines()),
                box,
                1.0f,
                0.75f,
                0.1f,
                1.0f
        );
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
