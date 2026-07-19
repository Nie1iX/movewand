package io.github.nie1ix.movewand.client;

import io.github.nie1ix.movewand.move.MoveProjection;
import io.github.nie1ix.movewand.move.MoveValidator;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.transform.BlockStateTransform;
import io.github.nie1ix.movewand.transform.SelectionTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PreviewRenderer {
    private static final GizmoStyle GHOST_STYLE = GizmoStyle.stroke(0x60FFFFFF, 1.0f);
    private static final GizmoStyle PENDING_CORNER_STYLE = GizmoStyle.stroke(0xFFFFBF1A, 1.0f);
    private static final GizmoStyle MOVABLE_SELECTION_STYLE = GizmoStyle.stroke(0x732ECCFF, 1.0f);
    private static final GizmoStyle UNMOVABLE_SELECTION_STYLE = GizmoStyle.stroke(0xCCFF400D, 1.0f);
    private static final GizmoStyle VALID_TARGET_STYLE = GizmoStyle.stroke(0xCC1A73FF, 1.0f);
    private static final GizmoStyle INVALID_TARGET_STYLE = GizmoStyle.stroke(0xCCFF1A1A, 1.0f);

    private PreviewRenderer() {
    }

    public static void render(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null
                || !shouldRenderSelection(client.player.getMainHandItem(), ModItems.moveWand())) {
            return;
        }

        try (Gizmos.TemporaryCollection ignored = event.getLevelRenderer().collectPerFrameRenderThreadGizmos()) {
            ClientSelectionHandler.pendingBoxCorner().ifPresent(corner -> Gizmos.cuboid(corner, PENDING_CORNER_STYLE));
            ClientSelectionHandler.selection().ifPresent(selection -> {
                if (TransformPreview.isActive()) {
                    renderPreview(client, selection);
                } else {
                    renderSelection(client, selection);
                }
            });
        }
    }

    static boolean shouldRenderSelection(ItemStack mainHandItem, Item moveWand) {
        return mainHandItem.is(moveWand);
    }

    private static void renderPreview(Minecraft client, BlockSelection selection) {
        BlockPos offset = TransformPreview.offset();
        Map<BlockPos, BlockPos> targets = SelectionTransform.transformMap(selection, offset, TransformPreview.clockwiseTurns());
        Map<BlockPos, BlockState> states = new LinkedHashMap<>();
        for (BlockPos source : targets.keySet()) {
            states.put(source, BlockStateTransform.rotateY(client.level.getBlockState(source), TransformPreview.clockwiseTurns()));
        }

        LevelReader projectedLevel = MoveProjection.levelAfterMove(client.level, states, targets);
        boolean withinRange = TransformPreview.isOffsetWithinRange(offset);
        for (Map.Entry<BlockPos, BlockPos> entry : targets.entrySet()) {
            BlockPos target = entry.getValue();
            BlockState state = states.get(entry.getKey());
            boolean emptyOrOverlapping = selection.positions().contains(target) || client.level.getBlockState(target).isAir();
            boolean valid = withinRange && emptyOrOverlapping && state.canSurvive(projectedLevel, target);
            Gizmos.cuboid(target, GHOST_STYLE);
            Gizmos.cuboid(target, valid ? VALID_TARGET_STYLE : INVALID_TARGET_STYLE);
        }
    }

    private static void renderSelection(Minecraft client, BlockSelection selection) {
        for (BlockPos position : selection.positions()) {
            Gizmos.cuboid(
                    position,
                    MoveValidator.isUnmovable(client.level.getBlockState(position))
                            ? UNMOVABLE_SELECTION_STYLE
                            : MOVABLE_SELECTION_STYLE
            );
        }
    }
}
