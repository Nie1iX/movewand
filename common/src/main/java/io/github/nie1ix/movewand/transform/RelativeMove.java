package io.github.nie1ix.movewand.transform;

import net.minecraft.core.Direction;

public enum RelativeMove {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    UP,
    DOWN;

    public int id() {
        return ordinal();
    }

    public static RelativeMove fromId(int id) {
        RelativeMove[] moves = values();
        if (id < 0 || id >= moves.length) {
            throw new IllegalArgumentException("Unknown relative move id: " + id);
        }

        return moves[id];
    }

    public Direction resolve(Direction horizontalView) {
        return switch (this) {
            case FORWARD -> requireHorizontal(horizontalView);
            case BACKWARD -> requireHorizontal(horizontalView).getOpposite();
            case LEFT -> requireHorizontal(horizontalView).getCounterClockWise();
            case RIGHT -> requireHorizontal(horizontalView).getClockWise();
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
        };
    }

    private static Direction requireHorizontal(Direction direction) {
        if (!direction.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Relative movement requires a horizontal view direction");
        }
        return direction;
    }
}
