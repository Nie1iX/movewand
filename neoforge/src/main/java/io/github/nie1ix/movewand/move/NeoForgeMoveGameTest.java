package io.github.nie1ix.movewand.move;

import io.github.nie1ix.movewand.MoveWand;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.Set;
import java.util.function.Consumer;

public final class NeoForgeMoveGameTest {
    private static final BlockPos SOURCE = new BlockPos(1, 2, 1);
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, MoveWand.MOD_ID);

    private NeoForgeMoveGameTest() {
    }

    static {
        TEST_FUNCTIONS.register("moves_selection", () -> NeoForgeMoveGameTest::movesSelectionAndSendsItsUpdate);
    }

    @SuppressWarnings("removal") // The connected mock player exercises the selection update packet path.
    private static void movesSelectionAndSendsItsUpdate(GameTestHelper context) {
        context.setBlock(SOURCE, Blocks.STONE);

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        NetworkRegistry.configureMockConnection(player.connection.getConnection());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.moveWand()));
        BlockPos source = context.absolutePos(SOURCE);
        BlockPos destination = source.east();
        ServerSelectionManager.replace(player, BlockSelection.of(Set.of(source), source));

        MoveService.move(player, 1, 0, 0, 0);

        context.assertBlockPresent(Blocks.AIR, SOURCE);
        context.assertBlockPresent(Blocks.STONE, SOURCE.east());
        context.assertTrue(ServerSelectionManager.selection(player)
                        .map(selection -> selection.positions().equals(Set.of(destination))
                                && selection.pivot().equals(destination))
                        .orElse(false),
                "the moved selection must be stored after its update packet is sent");
        context.succeed();
    }
}
