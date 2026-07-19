package io.github.nie1ix.movewand.transform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import io.github.nie1ix.movewand.selection.BlockSelection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SelectionTransform {
    private SelectionTransform() {
    }

    public static Set<BlockPos> translate(BlockSelection selection, Direction direction) {
        return Set.copyOf(translationMap(selection, direction).values());
    }

    public static Set<BlockPos> translate(BlockSelection selection, BlockPos offset) {
        return Set.copyOf(translationMap(selection, offset).values());
    }

    public static Map<BlockPos, BlockPos> translationMap(BlockSelection selection, Direction direction) {
        return translationMap(selection, new BlockPos(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
    }

    public static Map<BlockPos, BlockPos> translationMap(BlockSelection selection, BlockPos offset) {
        Map<BlockPos, BlockPos> targets = new LinkedHashMap<>();
        for (BlockPos position : selection.positions()) {
            targets.put(position, position.offset(offset));
        }
        return Map.copyOf(targets);
    }

    public static Set<BlockPos> rotateY(BlockSelection selection, BlockPos pivot, RotationDirection direction) {
        return Set.copyOf(rotationMap(selection, pivot, direction).values());
    }

    public static Set<BlockPos> rotateY(BlockSelection selection, RotationDirection direction) {
        return rotateY(selection, selection.pivot(), direction);
    }

    public static Map<BlockPos, BlockPos> rotationMap(BlockSelection selection, BlockPos pivot, RotationDirection direction) {
        Map<BlockPos, BlockPos> positions = new LinkedHashMap<>();
        for (BlockPos position : selection.positions()) {
            positions.put(position, rotatePosition(position, pivot, direction));
        }
        return Map.copyOf(positions);
    }

    public static Map<BlockPos, BlockPos> transformMap(BlockSelection selection, BlockPos offset, int clockwiseTurns) {
        Map<BlockPos, BlockPos> targets = new LinkedHashMap<>();
        int turns = Math.floorMod(clockwiseTurns, 4);
        for (BlockPos position : selection.positions()) {
            BlockPos target = position;
            for (int turn = 0; turn < turns; turn++) {
                target = rotatePosition(target, selection.pivot(), RotationDirection.CLOCKWISE);
            }
            targets.put(position, target.offset(offset));
        }
        return Map.copyOf(targets);
    }

    private static BlockPos rotatePosition(BlockPos position, BlockPos pivot, RotationDirection direction) {
        int deltaX = position.getX() - pivot.getX();
        int deltaZ = position.getZ() - pivot.getZ();
        int x = switch (direction) {
            case CLOCKWISE -> pivot.getX() - deltaZ;
            case COUNTER_CLOCKWISE -> pivot.getX() + deltaZ;
        };
        int z = switch (direction) {
            case CLOCKWISE -> pivot.getZ() + deltaX;
            case COUNTER_CLOCKWISE -> pivot.getZ() - deltaX;
        };
        return new BlockPos(x, position.getY(), z);
    }
}
