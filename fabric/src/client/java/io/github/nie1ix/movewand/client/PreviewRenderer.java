package io.github.nie1ix.movewand.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nie1ix.movewand.move.MoveProjection;
import io.github.nie1ix.movewand.move.MoveValidator;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.transform.BlockStateTransform;
import io.github.nie1ix.movewand.transform.SelectionTransform;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PreviewRenderer {
    private static final int GHOST_OUTLINE_COLOR = 0x60FFFFFF;
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static BlockModelResolver blockModelResolver;

    private PreviewRenderer() {
    }

    public static void initialize() {
        blockModelResolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level == null || client.player == null
                    || !shouldRenderSelection(client.player.getMainHandItem(), ModItems.moveWand())) {
                return;
            }

            Vec3 camera = context.levelState().cameraRenderState.pos;
            ClientSelectionHandler.pendingBoxCorner().ifPresent(corner -> renderPendingBoxCorner(context, camera, corner));
            ClientSelectionHandler.selection().ifPresent(selection -> {
                if (TransformPreview.isActive()) {
                    renderPreview(context, client, selection, camera);
                } else {
                    renderSelection(context, client, selection, camera);
                }
            });
        });
    }

    static boolean shouldRenderSelection(ItemStack mainHandItem, Item moveWand) {
        return mainHandItem.is(moveWand);
    }

    private static void renderPreview(LevelRenderContext context, Minecraft client, BlockSelection selection, Vec3 camera) {
        BlockPos offset = TransformPreview.offset();
        Map<BlockPos, BlockPos> targets = SelectionTransform.transformMap(selection, offset, TransformPreview.clockwiseTurns());
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        for (BlockPos source : targets.keySet()) {
            states.put(source, BlockStateTransform.rotateY(client.level.getBlockState(source), TransformPreview.clockwiseTurns()));
        }

        LevelReader projectedLevel = MoveProjection.levelAfterMove(client.level, states, targets);
        boolean withinRange = TransformPreview.isOffsetWithinRange(offset);
        renderGhostBlocks(context, targets, states, camera);

        for (Map.Entry<BlockPos, BlockPos> entry : targets.entrySet()) {
            BlockPos target = entry.getValue();
            BlockState state = states.get(entry.getKey());
            boolean emptyOrOverlapping = selection.positions().contains(target) || client.level.getBlockState(target).isAir();
            boolean valid = withinRange && emptyOrOverlapping && state.canSurvive(projectedLevel, target);
            submitBlockOutline(
                    context,
                    target,
                    camera,
                    valid ? 0xCC1A73FF : 0xCCFF1A1A,
                    1.0f
            );
        }
    }

    private static void renderGhostBlocks(
            LevelRenderContext context,
            Map<BlockPos, BlockPos> targets,
            Map<BlockPos, BlockState> states,
            Vec3 camera
    ) {
        PoseStack poseStack = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();

        for (Map.Entry<BlockPos, BlockPos> entry : targets.entrySet()) {
            BlockModelRenderState model = new BlockModelRenderState();
            blockModelResolver.update(model, states.get(entry.getKey()), BLOCK_DISPLAY_CONTEXT);
            poseStack.pushPose();
            translateToBlock(poseStack, entry.getValue(), camera);
            model.submitOnlyOutline(
                    poseStack,
                    collector,
                    LightCoordsUtil.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    GHOST_OUTLINE_COLOR
            );
            poseStack.popPose();
        }
    }

    private static void renderPendingBoxCorner(LevelRenderContext context, Vec3 camera, BlockPos corner) {
        submitBlockOutline(context, corner, camera, 0xFFFFBF1A, 1.0f);
    }

    private static void renderSelection(LevelRenderContext context, Minecraft client, BlockSelection selection, Vec3 camera) {
        for (BlockPos position : selection.positions()) {
            boolean unmovable = MoveValidator.isUnmovable(client.level.getBlockState(position));
            submitBlockOutline(
                    context,
                    position,
                    camera,
                    unmovable ? 0xCCFF400D : 0x732ECCFF,
                    1.0f
            );
        }
    }

    private static void submitBlockOutline(
            LevelRenderContext context,
            BlockPos position,
            Vec3 camera,
            int color,
            float lineWidth
    ) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        translateToBlock(poseStack, position, camera);
        context.submitNodeCollector().submitShapeOutline(
                poseStack,
                Shapes.block(),
                RenderTypes.lines(),
                color,
                lineWidth,
                true
        );
        poseStack.popPose();
    }

    private static void translateToBlock(PoseStack poseStack, BlockPos position, Vec3 camera) {
        poseStack.translate(position.getX() - camera.x, position.getY() - camera.y, position.getZ() - camera.z);
    }
}
