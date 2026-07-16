package io.github.nie1ix.movewand.transform;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockStateTransform {
    private BlockStateTransform() {
    }

    public static BlockState rotateY(BlockState state, int clockwiseTurns) {
        BlockState rotated = state;
        for (int turn = 0; turn < Math.floorMod(clockwiseTurns, 4); turn++) {
            rotated = rotated.rotate(Rotation.CLOCKWISE_90);
        }
        return rotated;
    }
}
