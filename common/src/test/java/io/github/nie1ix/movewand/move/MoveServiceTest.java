package io.github.nie1ix.movewand.move;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveServiceTest {
    @Test
    void acceptsANonZeroOffsetWithinSixteenBlocks() {
        assertTrue(MoveService.hasValidOffset(0, 0, 16));
        assertTrue(MoveService.hasValidOffset(8, 8, 8));
    }

    @Test
    void rejectsZeroAndOffsetsBeyondSixteenBlocks() {
        assertFalse(MoveService.hasValidOffset(0, 0, 0));
        assertFalse(MoveService.hasValidOffset(0, 0, 17));
        assertFalse(MoveService.hasValidOffset(16, 1, 0));
    }

    @Test
    void relocatesBlockEntityCoordinatesWithoutMutatingTheSnapshot() {
        CompoundTag snapshot = new CompoundTag();
        snapshot.putInt("x", 1);
        snapshot.putInt("y", 2);
        snapshot.putInt("z", 3);

        CompoundTag relocated = MoveService.relocatedBlockEntityData(snapshot, new BlockPos(10, 20, 30));

        assertEquals(1, snapshot.getInt("x").orElseThrow());
        assertEquals(10, relocated.getInt("x").orElseThrow());
        assertEquals(20, relocated.getInt("y").orElseThrow());
        assertEquals(30, relocated.getInt("z").orElseThrow());
    }

    @Test
    void clearsSourcesWithNoDropFlags() {
        int flags = MoveService.sourceClearFlags();

        assertTrue((flags & net.minecraft.world.level.block.Block.UPDATE_CLIENTS) != 0);
        assertTrue((flags & net.minecraft.world.level.block.Block.UPDATE_SUPPRESS_DROPS) != 0);
        assertFalse((flags & net.minecraft.world.level.block.Block.UPDATE_NEIGHBORS) != 0);
    }
}
