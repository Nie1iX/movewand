package io.github.nie1ix.movewand.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.move.MoveValidator;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.move.MoveProjection;
import io.github.nie1ix.movewand.transform.BlockStateTransform;
import io.github.nie1ix.movewand.transform.SelectionTransform;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PreviewRenderer {
    private static final int GHOST_ALPHA = 96;
    private static final RenderType GHOST_RENDER_TYPE = RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);

    private PreviewRenderer() {
    }

    public static void initialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.world() == null || context.matrixStack() == null || context.consumers() == null) {
                return;
            }
            if (Minecraft.getInstance().player == null
                    || !shouldRenderSelection(Minecraft.getInstance().player.getMainHandItem(), ModItems.moveWand())) {
                return;
            }

            Vec3 camera = context.camera().getPosition();
            ClientSelectionHandler.pendingBoxCorner().ifPresent(corner -> renderPendingBoxCorner(context, camera, corner));
            ClientSelectionHandler.selection().ifPresent(selection -> {
                if (TransformPreview.isActive()) {
                    renderPreview(context, selection);
                } else {
                    renderSelection(context, selection);
                }
            });
        });
    }

    static boolean shouldRenderSelection(ItemStack mainHandItem, Item moveWand) {
        return mainHandItem.is(moveWand);
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
        renderGhostBlocks(context, targets, states, camera);

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

    private static void renderGhostBlocks(
            net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context,
            Map<BlockPos, BlockPos> targets,
            Map<BlockPos, BlockState> states,
            Vec3 camera
    ) {
        VertexConsumer vertices = new AlphaVertexConsumer(context.consumers().getBuffer(GHOST_RENDER_TYPE));
        MultiBufferSource blockAtlasBuffers = ignored -> vertices;
        MultiBufferSource nativeBuffers = renderType -> new AlphaVertexConsumer(context.consumers().getBuffer(renderType));
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        PoseStack matrices = context.matrixStack();

        for (Map.Entry<BlockPos, BlockPos> entry : targets.entrySet()) {
            BlockPos target = entry.getValue();
            matrices.pushPose();
            matrices.translate(target.getX() - camera.x, target.getY() - camera.y, target.getZ() - camera.z);
            blockRenderer.renderSingleBlock(
                    states.get(entry.getKey()),
                    matrices,
                    usesBlockAtlasGhost(states.get(entry.getKey()).getRenderShape()) ? blockAtlasBuffers : nativeBuffers,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY
            );
            matrices.popPose();
        }
    }

    static int ghostAlpha(int alpha) {
        return Math.min(alpha, GHOST_ALPHA);
    }

    static boolean usesBlockAtlasGhost(RenderShape renderShape) {
        return renderShape != RenderShape.ENTITYBLOCK_ANIMATED;
    }

    private static final class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;

        private AlphaVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, ghostAlpha(alpha));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
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
            boolean unmovable = MoveValidator.isUnmovable(context.world().getBlockState(position));
            AABB box = new AABB(position).move(-camera.x, -camera.y, -camera.z).inflate(0.002);
            LevelRenderer.renderLineBox(
                    context.matrixStack(),
                    context.consumers().getBuffer(RenderType.lines()),
                    box,
                    unmovable ? 1.0f : 0.2f,
                    unmovable ? 0.25f : 0.8f,
                    unmovable ? 0.05f : 1.0f,
                    unmovable ? 0.8f : 0.45f
            );
        }
    }
}
