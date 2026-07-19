package io.github.nie1ix.movewand.selection;

import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record BlockSelection(Set<BlockPos> positions, BlockPos pivot) {
    public BlockSelection(Set<BlockPos> positions) {
        this(positions, firstPosition(positions));
    }

    public BlockSelection {
        positions = Collections.unmodifiableSet(new LinkedHashSet<>(positions));
        if (positions.isEmpty()) {
            throw new IllegalArgumentException("Selection must contain at least one block");
        }
        if (!positions.contains(pivot)) {
            throw new IllegalArgumentException("Pivot must belong to the selection");
        }
    }

    public static BlockSelection of(Collection<BlockPos> positions) {
        Set<BlockPos> uniquePositions = new LinkedHashSet<>(positions);
        return new BlockSelection(uniquePositions, firstPosition(uniquePositions));
    }

    public static BlockSelection of(Collection<BlockPos> positions, BlockPos pivot) {
        return new BlockSelection(new LinkedHashSet<>(positions), pivot);
    }

    private static BlockPos firstPosition(Collection<BlockPos> positions) {
        return positions.stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selection must contain at least one block"));
    }
}
