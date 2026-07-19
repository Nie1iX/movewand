package io.github.nie1ix.movewand.oritech;

import io.github.nie1ix.movewand.move.integration.MoveContext;
import io.github.nie1ix.movewand.move.integration.MoveIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class OritechMoveIntegration implements MoveIntegration {
    private static final String CONNECTED_CORES = "connectedCores";
    private static final String CONTROLLER_X = "controller_x";
    private static final String CONTROLLER_Y = "controller_y";
    private static final String CONTROLLER_Z = "controller_z";

    @Override
    public Set<BlockPos> expandSelection(ServerLevel level, Set<BlockPos> positions) {
        Set<BlockPos> expanded = new LinkedHashSet<>(positions);
        boolean changed;
        do {
            changed = false;
            for (BlockPos position : Set.copyOf(expanded)) {
                BlockEntity blockEntity = level.getBlockEntity(position);
                if (blockEntity == null) {
                    continue;
                }
                CompoundTag data = blockEntity.saveWithoutMetadata(level.registryAccess());
                for (BlockPos core : connectedCores(data)) {
                    changed |= expanded.add(core);
                }
                BlockPos controller = controller(data);
                if (controller != null) {
                    changed |= expanded.add(controller);
                }
            }
        } while (changed);
        return expanded;
    }

    @Override
    public void transformBlockEntityData(MoveContext context) {
        for (CompoundTag data : context.blockEntityData().values()) {
            relocateConnectedCores(data, context.destinations());
            relocateController(data, context.destinations());
        }
    }

    private static Set<BlockPos> connectedCores(CompoundTag data) {
        Set<BlockPos> cores = new LinkedHashSet<>();
        ListTag connectedCores = data.getList(CONNECTED_CORES, Tag.TAG_COMPOUND);
        for (Tag entry : connectedCores) {
            if (entry instanceof CompoundTag core) {
                cores.add(position(core, "x", "y", "z"));
            }
        }
        return cores;
    }

    private static void relocateConnectedCores(CompoundTag data, Map<BlockPos, BlockPos> destinations) {
        ListTag connectedCores = data.getList(CONNECTED_CORES, Tag.TAG_COMPOUND);
        for (Tag entry : connectedCores) {
            if (entry instanceof CompoundTag core) {
                relocate(core, "x", "y", "z", destinations);
            }
        }
    }

    private static BlockPos controller(CompoundTag data) {
        if (!data.contains(CONTROLLER_X, Tag.TAG_INT)
                || !data.contains(CONTROLLER_Y, Tag.TAG_INT)
                || !data.contains(CONTROLLER_Z, Tag.TAG_INT)) {
            return null;
        }
        return position(data, CONTROLLER_X, CONTROLLER_Y, CONTROLLER_Z);
    }

    private static void relocateController(CompoundTag data, Map<BlockPos, BlockPos> destinations) {
        if (controller(data) != null) {
            relocate(data, CONTROLLER_X, CONTROLLER_Y, CONTROLLER_Z, destinations);
        }
    }

    private static BlockPos position(CompoundTag data, String x, String y, String z) {
        return new BlockPos(data.getInt(x), data.getInt(y), data.getInt(z));
    }

    private static void relocate(
            CompoundTag data,
            String x,
            String y,
            String z,
            Map<BlockPos, BlockPos> destinations
    ) {
        BlockPos destination = destinations.get(position(data, x, y, z));
        if (destination == null) {
            return;
        }
        data.putInt(x, destination.getX());
        data.putInt(y, destination.getY());
        data.putInt(z, destination.getZ());
    }
}
