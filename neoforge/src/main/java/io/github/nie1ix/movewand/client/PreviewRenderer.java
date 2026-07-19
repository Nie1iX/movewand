package io.github.nie1ix.movewand.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.nie1ix.movewand.move.MoveProjection;
import io.github.nie1ix.movewand.move.MoveValidator;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.transform.BlockStateTransform;
import io.github.nie1ix.movewand.transform.SelectionTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PreviewRenderer {
    private static final int GHOST_ALPHA = 96;

    private PreviewRenderer() {
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || !shouldRenderSelection(client.player.getMainHandItem(), ModItems.moveWand())) {
            return;
        }

        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
        Vec3 camera = event.getCamera().getPosition();
        ClientSelectionHandler.pendingBoxCorner().ifPresent(corner -> renderPendingBoxCorner(event.getPoseStack(), buffers, camera, corner));
        ClientSelectionHandler.selection().ifPresent(selection -> {
            if (TransformPreview.isActive()) {
                renderPreview(event.getPoseStack(), buffers, client, camera, selection);
            } else {
                renderSelection(event.getPoseStack(), buffers, client, camera, selection);
            }
        });
        buffers.endBatch();
    }

    static boolean shouldRenderSelection(ItemStack mainHandItem, Item moveWand) {
        return mainHandItem.is(moveWand);
    }

    static int ghostAlpha(int alpha) {
        return Math.min(alpha, GHOST_ALPHA);
    }

    static boolean usesBlockAtlasGhost(RenderShape renderShape) {
        return renderShape != RenderShape.ENTITYBLOCK_ANIMATED;
    }

    private static void renderPreview(
            PoseStack matrices,
            MultiBufferSource.BufferSource buffers,
            Minecraft client,
            Vec3 camera,
            BlockSelection selection
    ) {
        BlockPos offset = TransformPreview.offset();
        Map<BlockPos, BlockPos> targets = SelectionTransform.transformMap(selection, offset, TransformPreview.clockwiseTurns());
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        for (BlockPos source : targets.keySet()) {
            states.put(source, BlockStateTransform.rotateY(client.level.getBlockState(source), TransformPreview.clockwiseTurns()));
        }

        LevelReader projectedLevel = MoveProjection.levelAfterMove(client.level, states, targets);
        boolean withinRange = TransformPreview.isOffsetWithinRange(offset);
        renderGhostBlocks(matrices, buffers, client.getBlockRenderer(), targets, states, camera);

        for (Map.Entry<BlockPos, BlockPos> entry : targets.entrySet()) {
            BlockPos target = entry.getValue();
            BlockState state = states.get(entry.getKey());
            boolean emptyOrOverlapping = selection.positions().contains(target) || client.level.getBlockState(target).isAir();
            boolean valid = withinRange && emptyOrOverlapping && state.canSurvive(projectedLevel, target);
            AABB box = new AABB(target).move(-camera.x, -camera.y, -camera.z).inflate(0.002);
            LevelRenderer.renderLineBox(
                    matrices,
                    buffers.getBuffer(RenderType.lines()),
                    box,
                    valid ? 0.1f : 1.0f,
                    valid ? 0.45f : 0.1f,
                    valid ? 1.0f : 0.1f,
                    0.8f
            );
        }
    }

    private static void renderGhostBlocks(
            PoseStack matrices,
            MultiBufferSource.BufferSource buffers,
            BlockRenderDispatcher blockRenderer,
            Map<BlockPos, BlockPos> targets,
            Map<BlockPos, BlockState> states,
            Vec3 camera
    ) {
        VertexConsumer vertices = new AlphaVertexConsumer(buffers.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS)));
        MultiBufferSource blockAtlasBuffers = ignored -> vertices;
        MultiBufferSource nativeBuffers = renderType -> new AlphaVertexConsumer(buffers.getBuffer(renderType));

        for (Map.Entry<BlockPos, BlockPos> entry : targets.entrySet()) {
            BlockPos target = entry.getValue();
            BlockState state = states.get(entry.getKey());
            matrices.pushPose();
            matrices.translate(target.getX() - camera.x, target.getY() - camera.y, target.getZ() - camera.z);
            blockRenderer.renderSingleBlock(
                    state,
                    matrices,
                    usesBlockAtlasGhost(state.getRenderShape()) ? blockAtlasBuffers : nativeBuffers,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY
            );
            matrices.popPose();
        }
    }

    private static void renderPendingBoxCorner(PoseStack matrices, MultiBufferSource buffers, Vec3 camera, BlockPos corner) {
        AABB box = new AABB(corner).move(-camera.x, -camera.y, -camera.z).inflate(0.004);
        LevelRenderer.renderLineBox(matrices, buffers.getBuffer(RenderType.lines()), box, 1.0f, 0.75f, 0.1f, 1.0f);
    }

    private static void renderSelection(
            PoseStack matrices,
            MultiBufferSource buffers,
            Minecraft client,
            Vec3 camera,
            BlockSelection selection
    ) {
        for (BlockPos position : selection.positions()) {
            boolean unmovable = MoveValidator.isUnmovable(client.level.getBlockState(position));
            AABB box = new AABB(position).move(-camera.x, -camera.y, -camera.z).inflate(0.002);
            LevelRenderer.renderLineBox(
                    matrices,
                    buffers.getBuffer(RenderType.lines()),
                    box,
                    unmovable ? 1.0f : 0.2f,
                    unmovable ? 0.25f : 0.8f,
                    unmovable ? 0.05f : 1.0f,
                    unmovable ? 0.8f : 0.45f
            );
        }
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
}
