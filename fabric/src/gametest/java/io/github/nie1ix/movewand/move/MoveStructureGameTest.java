package io.github.nie1ix.movewand.move;

import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import io.github.nie1ix.movewand.transform.BlockStateTransform;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;
import java.util.List;

public final class MoveStructureGameTest implements CustomTestMethodInvoker {
    private static final BlockPos SOURCE = new BlockPos(1, 2, 1);

    @GameTest
    public void movesBothDoorHalvesWhenOnlyTheLowerHalfIsSelected(GameTestHelper context) {
        BlockState lower = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        context.setBlock(SOURCE.below(), Blocks.STONE);
        context.setBlock(SOURCE.east().below(), Blocks.STONE);
        context.setBlock(SOURCE, lower);
        context.setBlock(SOURCE.above(), upper);

        move(context, List.of(SOURCE), 1, 0, 0, 0);

        context.assertBlockPresent(Blocks.AIR, SOURCE);
        context.assertBlockPresent(Blocks.AIR, SOURCE.above());
        context.assertBlockState(SOURCE.east(), state -> state.equals(lower), state -> Component.literal("door lower half must move"));
        context.assertBlockState(SOURCE.east().above(), state -> state.equals(upper), state -> Component.literal("door upper half must move"));
        context.succeed();
    }

    @GameTest
    public void keepsCarpetsWhenMovingAnOverlappingStructure(GameTestHelper context) {
        context.setBlock(SOURCE, Blocks.STONE);
        context.setBlock(SOURCE.above(), Blocks.MOSS_CARPET);
        context.setBlock(SOURCE.south(), Blocks.STONE);
        context.setBlock(SOURCE.south().above(), Blocks.MOSS_CARPET);

        move(context, List.of(SOURCE, SOURCE.above(), SOURCE.south(), SOURCE.south().above()), 0, 0, 1, 0);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.MOSS_CARPET, SOURCE.south().above());
            context.assertBlockPresent(Blocks.MOSS_CARPET, SOURCE.south(2).above());
            assertNoDrops(context);
            context.succeed();
        });
    }

    @GameTest
    public void keepsRedstoneComponentsWhenRotating(GameTestHelper context) {
        BlockPos comparator = SOURCE.east();
        BlockPos repeater = comparator.east();
        for (BlockPos position : List.of(SOURCE, comparator, repeater)) {
            context.setBlock(position, Blocks.STONE);
        }
        context.setBlock(SOURCE.above(), Blocks.REDSTONE_WIRE);
        context.setBlock(comparator.above(), Blocks.COMPARATOR);
        context.setBlock(repeater.above(), Blocks.REPEATER);

        move(context, List.of(SOURCE, comparator, repeater, SOURCE.above(), comparator.above(), repeater.above()), 0, 0, 0, 1);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.REDSTONE_WIRE, SOURCE.above());
            context.assertBlockPresent(Blocks.COMPARATOR, SOURCE.south().above());
            context.assertBlockPresent(Blocks.REPEATER, SOURCE.south(2).above());
            assertNoDrops(context);
            context.succeed();
        });
    }

    @GameTest
    public void rotatesWallTorchWithItsSupport(GameTestHelper context) {
        BlockState torch = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
        context.setBlock(SOURCE, Blocks.STONE);
        context.setBlock(SOURCE.east(), torch);

        move(context, List.of(SOURCE, SOURCE.east()), 0, 0, 0, 1);

        context.runAfterDelay(2, () -> {
            context.assertBlockState(SOURCE.south(), state -> state.equals(BlockStateTransform.rotateY(torch, 1)),
                    state -> Component.literal("wall torch must rotate with its support"));
            assertNoDrops(context);
            context.succeed();
        });
    }

    @GameTest
    public void movesIntoFlowingWater(GameTestHelper context) {
        context.setBlock(SOURCE, Blocks.STONE);
        context.setBlock(SOURCE.east(), Blocks.WATER.defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL, 3));

        move(context, List.of(SOURCE), 1, 0, 0, 0);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.AIR, SOURCE);
            context.assertBlockPresent(Blocks.STONE, SOURCE.east());
            context.succeed();
        });
    }

    @GameTest
    public void doesNotMoveSpawner(GameTestHelper context) {
        context.setBlock(SOURCE, Blocks.SPAWNER);

        move(context, List.of(SOURCE), 1, 0, 0, 0);

        context.assertBlockPresent(Blocks.SPAWNER, SOURCE);
        context.assertBlockPresent(Blocks.AIR, SOURCE.east());
        context.succeed();
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }

    @SuppressWarnings("removal") // Fabric's connected mock player is deprecated but required for networking assertions.
    private static void move(GameTestHelper context, List<BlockPos> positions, int x, int y, int z, int turns) {
        ServerPlayer player = (ServerPlayer) context.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.moveWand()));
        ServerSelectionManager.replace(player, BlockSelection.of(positions.stream().map(context::absolutePos).toList()));
        MoveService.move(player, x, y, z, turns);
    }

    private static void assertNoDrops(GameTestHelper context) {
        AABB bounds = new AABB(context.absolutePos(SOURCE)).inflate(4);
        context.assertTrue(context.getLevel().getEntitiesOfClass(ItemEntity.class, bounds).isEmpty(),
                "moving selected blocks must not create item drops");
    }
}
