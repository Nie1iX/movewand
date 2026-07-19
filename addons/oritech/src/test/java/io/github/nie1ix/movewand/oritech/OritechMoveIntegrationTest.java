package io.github.nie1ix.movewand.oritech;

import io.github.nie1ix.movewand.move.engine.MoveContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OritechMoveIntegrationTest {
    @Test
    void relocatesControllerAndCoreReferences() {
        BlockPos controller = new BlockPos(1, 2, 3);
        BlockPos core = new BlockPos(2, 2, 3);
        BlockPos relocatedController = new BlockPos(11, 2, 3);
        BlockPos relocatedCore = new BlockPos(12, 2, 3);

        CompoundTag controllerData = new CompoundTag();
        CompoundTag connectedCore = new CompoundTag();
        connectedCore.putInt("x", core.getX());
        connectedCore.putInt("y", core.getY());
        connectedCore.putInt("z", core.getZ());
        ListTag connectedCores = new ListTag();
        connectedCores.add(connectedCore);
        controllerData.put("connectedCores", connectedCores);

        CompoundTag coreData = new CompoundTag();
        coreData.putInt("controller_x", controller.getX());
        coreData.putInt("controller_y", controller.getY());
        coreData.putInt("controller_z", controller.getZ());

        Map<BlockPos, CompoundTag> data = new LinkedHashMap<>();
        data.put(controller, controllerData);
        data.put(core, coreData);
        Map<BlockPos, BlockPos> destinations = Map.of(
                controller, relocatedController,
                core, relocatedCore
        );

        new OritechMoveIntegration().transformBlockEntityData(
                new MoveContext(null, destinations, data, 0)
        );

        CompoundTag relocatedConnectedCore = controllerData.getList("connectedCores", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals(relocatedCore.getX(), relocatedConnectedCore.getInt("x"));
        assertEquals(relocatedCore.getY(), relocatedConnectedCore.getInt("y"));
        assertEquals(relocatedCore.getZ(), relocatedConnectedCore.getInt("z"));
        assertEquals(relocatedController.getX(), coreData.getInt("controller_x"));
        assertEquals(relocatedController.getY(), coreData.getInt("controller_y"));
        assertEquals(relocatedController.getZ(), coreData.getInt("controller_z"));
    }
}
