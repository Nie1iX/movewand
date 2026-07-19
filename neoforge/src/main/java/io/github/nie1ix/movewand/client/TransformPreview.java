package io.github.nie1ix.movewand.client;

import io.github.nie1ix.movewand.move.MoveService;
import io.github.nie1ix.movewand.network.MoveRequestPayload;
import io.github.nie1ix.movewand.transform.RelativeMove;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TransformPreview {
    private static BlockPos offset = BlockPos.ZERO;
    private static int clockwiseTurns;

    private TransformPreview() {
    }

    public static void add(RelativeMove move, Direction horizontalView) {
        offset = offset.relative(move.resolve(horizontalView));
    }

    public static BlockPos offset() {
        return offset;
    }

    public static int clockwiseTurns() {
        return clockwiseTurns;
    }

    public static void rotateClockwise() {
        clockwiseTurns = Math.floorMod(clockwiseTurns + 1, 4);
    }

    public static void rotateCounterClockwise() {
        clockwiseTurns = Math.floorMod(clockwiseTurns - 1, 4);
    }

    public static boolean isActive() {
        return !offset.equals(BlockPos.ZERO) || clockwiseTurns != 0;
    }

    static boolean isOffsetWithinRange(BlockPos offset) {
        return offset.equals(BlockPos.ZERO) || MoveService.hasValidOffset(offset.getX(), offset.getY(), offset.getZ());
    }

    public static void apply() {
        if (isActive()) {
            PacketDistributor.sendToServer(new MoveRequestPayload(offset.getX(), offset.getY(), offset.getZ(), clockwiseTurns));
        }
    }

    public static void cancel() {
        offset = BlockPos.ZERO;
        clockwiseTurns = 0;
    }
}
