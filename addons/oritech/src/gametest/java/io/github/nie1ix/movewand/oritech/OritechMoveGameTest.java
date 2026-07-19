package io.github.nie1ix.movewand.oritech;

import io.github.nie1ix.movewand.move.MoveService;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public final class OritechMoveGameTest implements FabricGameTest {
    private static final BlockPos CONTROLLER = new BlockPos(1, 2, 1);
    private static final BlockPos CORE = CONTROLLER.east();

    @GameTest(template = EMPTY_STRUCTURE)
    @SuppressWarnings("removal")
    public void movesOritechControllerAndCoreTogether(GameTestHelper context) {
        Block controllerBlock = oritechBlock(context, "deep_drill_block");
        Block coreBlock = oritechBlock(context, "machine_core_2");
        BlockPos controller = context.absolutePos(CONTROLLER);
        BlockPos core = context.absolutePos(CORE);
        BlockPos movedController = controller.south();
        BlockPos movedCore = core.south();

        context.setBlock(CONTROLLER, controllerBlock);
        context.setBlock(CORE, coreBlock);
        setControllerCores(context, controller, core);
        setCoreController(context, core, controller);

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.moveWand()));
        ServerSelectionManager.replace(player, BlockSelection.of(List.of(controller)));
        MoveService.move(player, 0, 0, 1, 0);

        context.assertBlockPresent(Blocks.AIR, CONTROLLER);
        context.assertBlockPresent(Blocks.AIR, CORE);
        context.assertBlockPresent(controllerBlock, CONTROLLER.south());
        context.assertBlockPresent(coreBlock, CORE.south());

        CompoundTag movedControllerData = requireBlockEntity(context, movedController)
                .saveWithoutMetadata(context.getLevel().registryAccess());
        CompoundTag movedCoreData = requireBlockEntity(context, movedCore)
                .saveWithoutMetadata(context.getLevel().registryAccess());
        CompoundTag connectedCore = movedControllerData.getList("connectedCores", Tag.TAG_COMPOUND).getCompound(0);
        context.assertTrue(
                new BlockPos(
                        connectedCore.getInt("x"),
                        connectedCore.getInt("y"),
                        connectedCore.getInt("z")
                ).equals(movedCore),
                "Oritech controller must point at the moved core"
        );
        context.assertTrue(
                new BlockPos(
                        movedCoreData.getInt("controller_x"),
                        movedCoreData.getInt("controller_y"),
                        movedCoreData.getInt("controller_z")
                ).equals(movedController),
                "Oritech core must point at the moved controller"
        );
        context.succeed();
    }

    private static Block oritechBlock(GameTestHelper context, String path) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("oritech", path));
        context.assertTrue(block != Blocks.AIR, "Oritech block must be registered: " + path);
        return block;
    }

    private static void setControllerCores(GameTestHelper context, BlockPos controllerPosition, BlockPos corePosition) {
        BlockEntity controller = requireBlockEntity(context, controllerPosition);
        CompoundTag data = controller.saveWithoutMetadata(context.getLevel().registryAccess());
        CompoundTag connectedCore = new CompoundTag();
        connectedCore.putInt("x", corePosition.getX());
        connectedCore.putInt("y", corePosition.getY());
        connectedCore.putInt("z", corePosition.getZ());
        ListTag connectedCores = new ListTag();
        connectedCores.add(connectedCore);
        data.put("connectedCores", connectedCores);
        controller.loadWithComponents(data, context.getLevel().registryAccess());
        controller.setChanged();
    }

    private static void setCoreController(GameTestHelper context, BlockPos corePosition, BlockPos controllerPosition) {
        BlockEntity core = requireBlockEntity(context, corePosition);
        CompoundTag data = core.saveWithoutMetadata(context.getLevel().registryAccess());
        data.putInt("controller_x", controllerPosition.getX());
        data.putInt("controller_y", controllerPosition.getY());
        data.putInt("controller_z", controllerPosition.getZ());
        core.loadWithComponents(data, context.getLevel().registryAccess());
        core.setChanged();
    }

    private static BlockEntity requireBlockEntity(GameTestHelper context, BlockPos position) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(position);
        context.assertTrue(blockEntity != null, "Oritech block entity must exist at " + position);
        return blockEntity;
    }
}
