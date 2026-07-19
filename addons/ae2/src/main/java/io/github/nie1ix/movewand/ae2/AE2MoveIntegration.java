package io.github.nie1ix.movewand.ae2;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientableBlock;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.RelativeSide;
import appeng.block.networking.CableBusBlock;
import io.github.nie1ix.movewand.move.integration.MoveContext;
import io.github.nie1ix.movewand.move.integration.MoveIntegration;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

public final class AE2MoveIntegration implements MoveIntegration {
    @Override
    public BlockState transformBlockState(BlockState state, int clockwiseTurns) {
        if (!(state.getBlock() instanceof IOrientableBlock block)) {
            return state;
        }

        BlockOrientation orientation = rotateY(block.getOrientation(state), clockwiseTurns);
        IOrientationStrategy strategy = block.getOrientationStrategy();
        return strategy.setOrientation(
                state,
                orientation.getSide(RelativeSide.FRONT),
                orientation.getSide(RelativeSide.TOP)
        );
    }

    @Override
    public void transformBlockEntityData(MoveContext context) {
        for (var entry : context.blockEntityData().entrySet()) {
            BlockState state = context.level().getBlockState(entry.getKey());
            if (state.getBlock() instanceof CableBusBlock) {
                rotateCableBusSides(entry.getValue(), context.clockwiseTurns());
            }
        }
    }

    static void rotateCableBusSides(CompoundTag data, int clockwiseTurns) {
        for (int turn = 0; turn < Math.floorMod(clockwiseTurns, 4); turn++) {
            rotateHorizontalKeys(data, "north", "east", "south", "west");
            rotateHorizontalKeys(data, "facadeNorth", "facadeEast", "facadeSouth", "facadeWest");
        }
    }

    static BlockOrientation rotateY(BlockOrientation orientation, int clockwiseTurns) {
        BlockOrientation rotated = orientation;
        for (int turn = 0; turn < Math.floorMod(clockwiseTurns, 4); turn++) {
            rotated = rotated.rotateClockwiseAround(Direction.UP);
        }
        return rotated;
    }

    private static void rotateHorizontalKeys(
            CompoundTag data,
            String north,
            String east,
            String south,
            String west
    ) {
        Tag northData = data.get(north);
        Tag eastData = data.get(east);
        Tag southData = data.get(south);
        Tag westData = data.get(west);
        data.remove(north);
        data.remove(east);
        data.remove(south);
        data.remove(west);
        putIfPresent(data, east, northData);
        putIfPresent(data, south, eastData);
        putIfPresent(data, west, southData);
        putIfPresent(data, north, westData);
    }

    private static void putIfPresent(CompoundTag data, String key, Tag value) {
        if (value != null) {
            data.put(key, value.copy());
        }
    }
}
