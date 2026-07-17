package io.github.nie1ix.movewand.move;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;

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
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MOVE_WAND));
        ServerSelectionManager.replace(player, BlockSelection.of(List.of(context.absolutePos(sourceRelative))));
        MoveService.move(player, 1, 0, 0, 0);
    }
}
