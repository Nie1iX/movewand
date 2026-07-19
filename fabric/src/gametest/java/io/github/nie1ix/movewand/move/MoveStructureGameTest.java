package io.github.nie1ix.movewand.move;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import io.github.nie1ix.movewand.transform.BlockStateTransform;

import java.util.ArrayList;
import java.util.List;

public final class MoveStructureGameTest implements FabricGameTest {
    private static final BlockPos SOURCE_RELATIVE = new BlockPos(1, 2, 1);

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesBothDoorHalvesWhenOnlyTheLowerHalfIsSelected(GameTestHelper context) {
        BlockState lower = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        context.setBlock(SOURCE_RELATIVE.below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east().below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE, lower);
        context.setBlock(SOURCE_RELATIVE.above(), upper);

        moveSelectedBlock(context, SOURCE_RELATIVE);

        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE);
        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.above());
        context.assertBlockState(SOURCE_RELATIVE.east(), state -> state.equals(lower), () -> "door lower half must move");
        context.assertBlockState(SOURCE_RELATIVE.east().above(), state -> state.equals(upper), () -> "door upper half must move");
        context.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesBothBedPartsWhenOnlyTheFootIsSelected(GameTestHelper context) {
        BlockState foot = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.FOOT);
        BlockState head = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.HEAD);
        context.setBlock(SOURCE_RELATIVE.below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.north().below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east().below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east().north().below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE, foot);
        context.setBlock(SOURCE_RELATIVE.north(), head);

        moveSelectedBlock(context, SOURCE_RELATIVE);

        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE);
        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.north());
        context.assertBlockState(SOURCE_RELATIVE.east(), state -> state.equals(foot), () -> "bed foot must move");
        context.assertBlockState(SOURCE_RELATIVE.east().north(), state -> state.equals(head), () -> "bed head must move");
        context.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesBedWithoutDestinationSupport(GameTestHelper context) {
        BlockState foot = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.FOOT);
        BlockState head = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.HEAD);
        context.setBlock(SOURCE_RELATIVE.below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.north().below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE, foot);
        context.setBlock(SOURCE_RELATIVE.north(), head);

        moveSelectedBlock(context, SOURCE_RELATIVE);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE);
            context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.north());
            context.assertBlockState(SOURCE_RELATIVE.east(), state -> state.equals(foot),
                    () -> "bed foot must move without destination support");
            context.assertBlockState(SOURCE_RELATIVE.east().north(), state -> state.equals(head),
                    () -> "bed head must move without destination support");
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesFlowerWhenBothPositionsHaveSupport(GameTestHelper context) {
        assertSupportedBlockMoves(context, Blocks.DANDELION.defaultBlockState(), Blocks.DIRT);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesShortGrassWhenBothPositionsHaveSupport(GameTestHelper context) {
        assertSupportedBlockMoves(context, Blocks.SHORT_GRASS.defaultBlockState(), Blocks.DIRT);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesPointedDripstoneWhenBothPositionsHaveSupport(GameTestHelper context) {
        assertSupportedBlockMoves(context, Blocks.POINTED_DRIPSTONE.defaultBlockState(), Blocks.STONE);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotDropCarpetsWhenDestinationOverlapsSource(GameTestHelper context) {
        context.setBlock(SOURCE_RELATIVE, Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.above(), Blocks.WHITE_CARPET);
        context.setBlock(SOURCE_RELATIVE.south(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.south().above(), Blocks.WHITE_CARPET);

        moveSelectedBlocks(context, List.of(
                SOURCE_RELATIVE,
                SOURCE_RELATIVE.above(),
                SOURCE_RELATIVE.south(),
                SOURCE_RELATIVE.south().above()
        ), 0, 0, 1, 0);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.WHITE_CARPET, SOURCE_RELATIVE.south().above());
            context.assertBlockPresent(Blocks.WHITE_CARPET, SOURCE_RELATIVE.south(2).above());
            assertNoDroppedItems(context);
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotDropRedstoneComponentsWhenDestinationOverlapsSource(GameTestHelper context) {
        BlockPos first = SOURCE_RELATIVE;
        BlockPos second = first.south();
        BlockPos third = second.south();
        context.setBlock(first.below(), Blocks.STONE);
        context.setBlock(second.below(), Blocks.STONE);
        context.setBlock(third.below(), Blocks.STONE);
        context.setBlock(first, Blocks.REDSTONE_WIRE);
        context.setBlock(second, Blocks.COMPARATOR);
        context.setBlock(third, Blocks.REPEATER);

        moveSelectedBlocks(context, List.of(
                first.below(), first,
                second.below(), second,
                third.below(), third
        ), 0, 0, 1, 0);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.REDSTONE_WIRE, first.south());
            context.assertBlockPresent(Blocks.COMPARATOR, second.south());
            context.assertBlockPresent(Blocks.REPEATER, third.south());
            assertNoDroppedItems(context);
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotDropRedstoneComponentsWhenRotating(GameTestHelper context) {
        BlockPos pivot = SOURCE_RELATIVE;
        BlockPos second = pivot.east();
        BlockPos third = second.east();
        context.setBlock(pivot, Blocks.STONE);
        context.setBlock(second, Blocks.STONE);
        context.setBlock(third, Blocks.STONE);
        context.setBlock(pivot.above(), Blocks.REDSTONE_WIRE);
        context.setBlock(second.above(), Blocks.COMPARATOR);
        context.setBlock(third.above(), Blocks.REPEATER);

        moveSelectedBlocks(context, List.of(
                pivot, second, third,
                pivot.above(), second.above(), third.above()
        ), 0, 0, 0, 1);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.REDSTONE_WIRE, pivot.above());
            context.assertBlockPresent(Blocks.COMPARATOR, pivot.south().above());
            context.assertBlockPresent(Blocks.REPEATER, pivot.south(2).above());
            assertNoDroppedItems(context);
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotDropCarpetsWhenMovingLargeOverlappingStructure(GameTestHelper context) {
        List<BlockPos> selection = new ArrayList<>();
        for (int y = 0; y <= 1; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos position = SOURCE_RELATIVE.offset(x, y, z);
                    context.setBlock(position, y == 0 ? Blocks.STONE : Blocks.WHITE_CARPET);
                    selection.add(position);
                }
            }
        }

        moveSelectedBlocks(context, selection, 0, 0, 1, 0);

        context.runAfterDelay(2, () -> {
            for (int x = 0; x < 3; x++) {
                for (int z = 1; z <= 3; z++) {
                    context.assertBlockPresent(Blocks.WHITE_CARPET, SOURCE_RELATIVE.offset(x, 1, z));
                }
            }
            assertNoDroppedItems(context);
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesIntoFlowingWater(GameTestHelper context) {
        context.setBlock(SOURCE_RELATIVE, Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east(), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 3));

        moveSelectedBlock(context, SOURCE_RELATIVE);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE);
            context.assertBlockPresent(Blocks.STONE, SOURCE_RELATIVE.east());
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesWallTorchWithItsSupport(GameTestHelper context) {
        BlockState torch = wallTorch();
        context.setBlock(SOURCE_RELATIVE, Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east(), torch);

        moveSelectedBlocks(context, List.of(SOURCE_RELATIVE, SOURCE_RELATIVE.east()), 0, 0, 1, 0);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE);
            context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.east());
            context.assertBlockPresent(Blocks.STONE, SOURCE_RELATIVE.south());
            context.assertBlockState(SOURCE_RELATIVE.east().south(), state -> state.equals(torch),
                    () -> "wall torch must move with its support");
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void rotatesWallTorchWithItsSupport(GameTestHelper context) {
        BlockState torch = wallTorch();
        BlockState rotatedTorch = BlockStateTransform.rotateY(torch, 1);
        context.setBlock(SOURCE_RELATIVE, Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east(), torch);

        moveSelectedBlocks(context, List.of(SOURCE_RELATIVE, SOURCE_RELATIVE.east()), 0, 0, 0, 1);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.STONE, SOURCE_RELATIVE);
            context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.east());
            context.assertBlockState(SOURCE_RELATIVE.south(), state -> state.equals(rotatedTorch),
                    () -> "wall torch must rotate with its support");
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotDropWallTorchesWhenMovingTheirSharedSupport(GameTestHelper context) {
        placeWallTorchesAroundSupport(context);

        moveSelectedBlocks(context, wallTorchStructure(), 0, 0, 2, 0);

        context.runAfterDelay(2, () -> {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos destination = SOURCE_RELATIVE.relative(direction).south(2);
                context.assertBlockState(destination, state -> state.equals(wallTorch(direction)),
                        () -> "wall torch must move with its shared support");
            }
            assertNoDroppedItems(context);
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotDropWallTorchesWhenRotatingTheirSharedSupport(GameTestHelper context) {
        placeWallTorchesAroundSupport(context);

        moveSelectedBlocks(context, wallTorchStructure(), 0, 0, 0, 1);

        context.runAfterDelay(2, () -> {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                context.assertBlockState(SOURCE_RELATIVE.relative(direction), state -> state.equals(wallTorch(direction)),
                        () -> "wall torch must rotate with its shared support");
            }
            assertNoDroppedItems(context);
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotDropSideAttachedBlocksWhenMovingTheirSharedSupport(GameTestHelper context) {
        BlockState button = wallAttached(Blocks.STONE_BUTTON.defaultBlockState(), Direction.EAST);
        BlockState lever = wallAttached(Blocks.LEVER.defaultBlockState(), Direction.WEST);
        context.setBlock(SOURCE_RELATIVE, Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east(), button);
        context.setBlock(SOURCE_RELATIVE.west(), lever);

        moveSelectedBlocks(context, List.of(SOURCE_RELATIVE, SOURCE_RELATIVE.east(), SOURCE_RELATIVE.west()), 0, 0, 2, 0);

        context.runAfterDelay(2, () -> {
            context.assertBlockState(SOURCE_RELATIVE.east().south(2), state -> state.equals(button),
                    () -> "wall button must move with its support");
            context.assertBlockState(SOURCE_RELATIVE.west().south(2), state -> state.equals(lever),
                    () -> "wall lever must move with its support");
            assertNoDroppedItems(context);
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void dropsUnselectedSideAttachedBlockWhenItsSupportMoves(GameTestHelper context) {
        context.setBlock(SOURCE_RELATIVE, Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east(), wallAttached(Blocks.STONE_BUTTON.defaultBlockState(), Direction.EAST));

        moveSelectedBlocks(context, List.of(SOURCE_RELATIVE), 0, 0, 1, 0);

        context.runAfterDelay(2, () -> {
            context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.east());
            AABB bounds = new AABB(context.absolutePos(SOURCE_RELATIVE)).inflate(2);
            context.assertTrue(context.getLevel().getEntitiesOfClass(ItemEntity.class, bounds).stream()
                            .anyMatch(entity -> entity.getItem().is(Items.STONE_BUTTON)),
                    "unselected button must drop when its support moves");
            context.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotMoveSpawner(GameTestHelper context) {
        context.setBlock(SOURCE_RELATIVE, Blocks.SPAWNER);

        moveSelectedBlock(context, SOURCE_RELATIVE);

        context.assertBlockPresent(Blocks.SPAWNER, SOURCE_RELATIVE);
        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.east());
        context.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotMoveTrialSpawner(GameTestHelper context) {
        context.setBlock(SOURCE_RELATIVE, Blocks.TRIAL_SPAWNER);

        moveSelectedBlock(context, SOURCE_RELATIVE);

        context.assertBlockPresent(Blocks.TRIAL_SPAWNER, SOURCE_RELATIVE);
        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.east());
        context.succeed();
    }

    private static void assertSupportedBlockMoves(GameTestHelper context, BlockState state, Block support) {
        context.setBlock(SOURCE_RELATIVE.below(), support);
        context.setBlock(SOURCE_RELATIVE.east().below(), support);
        context.setBlock(SOURCE_RELATIVE, state);

        moveSelectedBlock(context, SOURCE_RELATIVE);

        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE);
        context.assertBlockState(SOURCE_RELATIVE.east(), moved -> moved.equals(state), () -> "supported block state must move unchanged");
        context.succeed();
    }

    @SuppressWarnings("removal")
    private static void moveSelectedBlock(GameTestHelper context, BlockPos sourceRelative) {
        moveSelectedBlocks(context, List.of(sourceRelative), 1, 0, 0, 0);
    }

    @SuppressWarnings("removal")
    private static void moveSelectedBlocks(GameTestHelper context, List<BlockPos> sourceRelatives,
                                           int x, int y, int z, int turns) {
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.moveWand()));
        ServerSelectionManager.replace(player, BlockSelection.of(sourceRelatives.stream()
                .map(context::absolutePos)
                .toList()));
        MoveService.move(player, x, y, z, turns);
    }

    private static BlockState wallTorch() {
        return wallTorch(Direction.EAST);
    }

    private static BlockState wallTorch(Direction facing) {
        return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing);
    }

    private static BlockState wallAttached(BlockState state, Direction facing) {
        return state
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL)
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACING, facing);
    }

    private static void placeWallTorchesAroundSupport(GameTestHelper context) {
        context.setBlock(SOURCE_RELATIVE, Blocks.STONE);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            context.setBlock(SOURCE_RELATIVE.relative(direction), wallTorch(direction));
        }
    }

    private static List<BlockPos> wallTorchStructure() {
        return List.of(
                SOURCE_RELATIVE,
                SOURCE_RELATIVE.north(),
                SOURCE_RELATIVE.east(),
                SOURCE_RELATIVE.south(),
                SOURCE_RELATIVE.west()
        );
    }

    private static void assertNoDroppedItems(GameTestHelper context) {
        AABB bounds = new AABB(context.absolutePos(SOURCE_RELATIVE)).inflate(4);
        List<ItemEntity> droppedItems = context.getLevel().getEntitiesOfClass(ItemEntity.class, bounds);
        context.assertTrue(droppedItems.isEmpty(),
                "moving attached blocks must not create item drops: "
                        + droppedItems.stream().map(item -> item.getItem() + " at " + item.blockPosition()).toList());
    }
}
