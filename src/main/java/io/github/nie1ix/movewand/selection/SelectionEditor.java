package io.github.nie1ix.movewand.selection;

import net.minecraft.core.BlockPos;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class SelectionEditor {
    public static final int DEFAULT_MAX_POSITIONS = 512;

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

    public void toggleBlock(BlockPos position) {
        toggleBlocks(Set.of(position), position);
    }

    public void toggleBlocks(Set<BlockPos> blocks, BlockPos pivotCandidate) {
        if (blocks.isEmpty()) {
            return;
        }

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
