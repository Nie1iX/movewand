package io.github.nie1ix.movewand.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class SelectionEditor {
    public static final int DEFAULT_MAX_POSITIONS = 10000;

    private final Set<BlockPos> positions = new LinkedHashSet<>();
    private final int maxPositions;
    private BlockPos pendingBoxCorner;
    private BlockPos pivot;

    public SelectionEditor() {
        this(DEFAULT_MAX_POSITIONS);
    }

    public SelectionEditor(int maxPositions) {
        if (maxPositions < 1) {
            throw new IllegalArgumentException("Selection limit must be positive");
        }
        this.maxPositions = maxPositions;
    }

    public boolean selectBoxCorner(BlockPos corner) {
        if (pendingBoxCorner == null) {
            pendingBoxCorner = corner;
            return true;
        }

        long width = (long) Math.max(pendingBoxCorner.getX(), corner.getX()) - Math.min(pendingBoxCorner.getX(), corner.getX()) + 1;
        long height = (long) Math.max(pendingBoxCorner.getY(), corner.getY()) - Math.min(pendingBoxCorner.getY(), corner.getY()) + 1;
        long depth = (long) Math.max(pendingBoxCorner.getZ(), corner.getZ()) - Math.min(pendingBoxCorner.getZ(), corner.getZ()) + 1;
        // Divide before multiplying so a huge box cannot overflow while checking the selection limit.
        if (width > maxPositions || height > maxPositions || depth > maxPositions || width * height > maxPositions / depth) {
            pendingBoxCorner = null;
            return false;
        }

        positions.clear();
        for (int x = Math.min(pendingBoxCorner.getX(), corner.getX()); x <= Math.max(pendingBoxCorner.getX(), corner.getX()); x++) {
            for (int y = Math.min(pendingBoxCorner.getY(), corner.getY()); y <= Math.max(pendingBoxCorner.getY(), corner.getY()); y++) {
                for (int z = Math.min(pendingBoxCorner.getZ(), corner.getZ()); z <= Math.max(pendingBoxCorner.getZ(), corner.getZ()); z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        pivot = pendingBoxCorner;
        pendingBoxCorner = null;
        return true;
    }

    public ExpandResult expand(Direction direction, boolean grow) {
        if (positions.isEmpty()) {
            return ExpandResult.EMPTY_SELECTION;
        }

        // Shrinking pulls in the face opposite the pressed direction, so the face you are
        // "pointing at" stays put and the selection visibly recedes from the other side.
        Direction effectiveDirection = grow ? direction : direction.getOpposite();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int delta = grow ? 1 : -1;
        int stepX = effectiveDirection.getStepX();
        int stepY = effectiveDirection.getStepY();
        int stepZ = effectiveDirection.getStepZ();
        if (stepX > 0) {
            maxX += delta;
        } else if (stepX < 0) {
            minX -= delta;
        }
        if (stepY > 0) {
            maxY += delta;
        } else if (stepY < 0) {
            minY -= delta;
        }
        if (stepZ > 0) {
            maxZ += delta;
        } else if (stepZ < 0) {
            minZ -= delta;
        }

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return ExpandResult.TOO_SMALL;
        }

        long width = (long) (maxX - minX + 1);
        long height = (long) (maxY - minY + 1);
        long depth = (long) (maxZ - minZ + 1);
        if (width > maxPositions || height > maxPositions || depth > maxPositions || width * height > (long) maxPositions / depth) {
            return ExpandResult.TOO_LARGE;
        }

        Set<BlockPos> newPositions = new LinkedHashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    newPositions.add(new BlockPos(x, y, z));
                }
            }
        }

        positions.clear();
        positions.addAll(newPositions);
        if (pivot == null || !positions.contains(pivot)) {
            pivot = positions.stream().findFirst().orElse(null);
        }
        pendingBoxCorner = null;
        return ExpandResult.SUCCESS;
    }

    public void toggleBlock(BlockPos position) {
        toggleBlocks(Set.of(position), position);
    }

    public void toggleBlocks(Set<BlockPos> blocks, BlockPos pivotCandidate) {
        if (blocks.isEmpty()) {
            return;
        }

        pendingBoxCorner = null;

        if (positions.containsAll(blocks)) {
            positions.removeAll(blocks);
            if (!positions.contains(pivot)) {
                pivot = positions.stream().findFirst().orElse(null);
            }
        } else {
            positions.addAll(blocks);
            if (pivot == null) {
                pivot = pivotCandidate;
            }
        }
    }

    public void clear() {
        positions.clear();
        pendingBoxCorner = null;
        pivot = null;
    }

    public boolean replace(BlockSelection selection) {
        if (selection.positions().size() > maxPositions) {
            return false;
        }

        positions.clear();
        positions.addAll(selection.positions());
        pendingBoxCorner = null;
        pivot = selection.pivot();
        return true;
    }

    public Optional<BlockSelection> selection() {
        return positions.isEmpty() ? Optional.empty() : Optional.of(BlockSelection.of(positions, pivot));
    }

    public Optional<BlockPos> pendingBoxCorner() {
        return Optional.ofNullable(pendingBoxCorner);
    }
}
