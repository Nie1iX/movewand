package io.github.nie1ix.movewand.move;

import net.fabricmc.api.ModInitializer;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.gametest.framework.GameTestHelper;
import io.github.nie1ix.movewand.registry.ModItems;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;

import java.util.List;
import java.util.function.Consumer;

public final class MoveBlockEntityGameTest implements ModInitializer, FabricGameTest {
    private static final String TEST_NAMESPACE = "movewand_gametest";
    private static final Block TEST_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            Identifier.fromNamespaceAndPath(TEST_NAMESPACE, "data_block"),
            new DataBlock(BlockBehaviour.Properties.of())
    );
    private static final BlockEntityType<DataBlockEntity> TEST_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(TEST_NAMESPACE, "data_block"),
            BlockEntityType.Builder.of(DataBlockEntity::new, TEST_BLOCK).build(null)
    );

    private static final BlockPos SOURCE_RELATIVE = new BlockPos(1, 2, 1);

    @Override
    public void onInitialize() {
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesChestInventory(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, Blocks.CHEST, blockEntity -> setItem(blockEntity, Items.DIAMOND));
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesBarrelInventory(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, Blocks.BARREL, blockEntity -> setItem(blockEntity, Items.EMERALD));
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesHopperInventory(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, Blocks.HOPPER, blockEntity -> setItem(blockEntity, Items.REDSTONE));
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesFurnaceProgressAndInventory(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, Blocks.FURNACE, blockEntity -> {
            AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) blockEntity;
            furnace.setItem(0, new ItemStack(Items.IRON_ORE));
            furnace.setItem(1, new ItemStack(Items.COAL));
            CompoundTag state = furnace.saveWithoutMetadata(furnace.getLevel().registryAccess());
            state.putShort("BurnTime", (short) 120);
            state.putShort("CookTime", (short) 45);
            state.putShort("CookTimeTotal", (short) 200);
            furnace.loadWithComponents(state, furnace.getLevel().registryAccess());
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesBrewingStandFuelAndInventory(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, Blocks.BREWING_STAND, blockEntity -> {
            setItem(blockEntity, Items.BLAZE_POWDER);
            CompoundTag state = blockEntity.saveWithoutMetadata(blockEntity.getLevel().registryAccess());
            state.putShort("BrewTime", (short) 200);
            state.putByte("Fuel", (byte) 12);
            blockEntity.loadWithComponents(state, blockEntity.getLevel().registryAccess());
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesLecternBook(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, Blocks.LECTERN, blockEntity -> {
            ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
            book.set(
                    DataComponents.WRITABLE_BOOK_CONTENT,
                    new WritableBookContent(List.of(Filterable.passThrough("MoveWand")))
            );
            ((LecternBlockEntity) blockEntity).setBook(book);
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesShulkerBoxInventory(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, Blocks.SHULKER_BOX, blockEntity -> setItem(blockEntity, Items.NETHER_STAR));
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesSignText(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, Blocks.OAK_SIGN, blockEntity -> {
            SignBlockEntity sign = (SignBlockEntity) blockEntity;
            sign.setText(sign.getFrontText().setMessage(0, Component.literal("MoveWand")), true);
            sign.setWaxed(true);
        });
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void movesModdedBlockEntityData(GameTestHelper context) {
        assertBlockEntityNbtIsPreserved(context, TEST_BLOCK, blockEntity -> ((DataBlockEntity) blockEntity).setMarker(42));
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotMoveLockedBlockEntity(GameTestHelper context) {
        BlockPos source = context.absolutePos(SOURCE_RELATIVE);
        context.setBlock(SOURCE_RELATIVE, TEST_BLOCK);
        DataBlockEntity blockEntity = (DataBlockEntity) context.getLevel().getBlockEntity(source);
        context.assertTrue(blockEntity != null, "source block entity must exist");
        blockEntity.setLock("secret");

        ServerPlayer player = makeServerPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.moveWand()));
        ServerSelectionManager.replace(player, BlockSelection.of(List.of(source)));
        MoveService.move(player, 1, 0, 0, 0);

        context.assertBlockPresent(TEST_BLOCK, SOURCE_RELATIVE);
        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.east());
        context.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void doesNotMoveOneHalfOfDoubleChest(GameTestHelper context) {
        BlockState left = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, net.minecraft.core.Direction.NORTH)
                .setValue(ChestBlock.TYPE, ChestType.LEFT);
        BlockState right = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, net.minecraft.core.Direction.NORTH)
                .setValue(ChestBlock.TYPE, ChestType.RIGHT);
        context.setBlock(SOURCE_RELATIVE, left);
        context.setBlock(SOURCE_RELATIVE.east(), right);

        ServerPlayer player = makeServerPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.moveWand()));
        ServerSelectionManager.replace(player, BlockSelection.of(List.of(context.absolutePos(SOURCE_RELATIVE))));
        MoveService.move(player, 0, 0, 1, 0);

        context.assertBlockPresent(Blocks.CHEST, SOURCE_RELATIVE);
        context.assertBlockPresent(Blocks.CHEST, SOURCE_RELATIVE.east());
        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE.south());
        context.succeed();
    }

    private static void assertBlockEntityNbtIsPreserved(
            GameTestHelper context,
            Block block,
            Consumer<BlockEntity> configure
    ) {
        BlockPos source = context.absolutePos(SOURCE_RELATIVE);
        BlockPos destination = source.east();
        ServerLevel level = context.getLevel();
        context.setBlock(SOURCE_RELATIVE.below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE.east().below(), Blocks.STONE);
        context.setBlock(SOURCE_RELATIVE, block);

        BlockEntity sourceBlockEntity = level.getBlockEntity(source);
        context.assertTrue(sourceBlockEntity != null, "source block entity must exist");
        configure.accept(sourceBlockEntity);
        sourceBlockEntity.setChanged();
        CompoundTag expected = sourceBlockEntity.saveWithoutMetadata(level.registryAccess());

        ServerPlayer player = makeServerPlayer(context);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.moveWand()));
        ServerSelectionManager.replace(player, BlockSelection.of(List.of(source)));
        MoveService.move(player, 1, 0, 0, 0);

        BlockEntity destinationBlockEntity = level.getBlockEntity(destination);
        context.assertTrue(destinationBlockEntity != null, "destination block entity must exist");
        CompoundTag actual = destinationBlockEntity.saveWithoutMetadata(level.registryAccess());
        context.assertTrue(
                expected.equals(actual),
                "block entity data must survive the move: expected " + expected + ", got " + actual
        );
        context.assertBlockPresent(Blocks.AIR, SOURCE_RELATIVE);
        context.succeed();
    }

    private static void setItem(BlockEntity blockEntity, Item item) {
        ((Container) blockEntity).setItem(0, new ItemStack(item));
    }

    @SuppressWarnings("removal")
    private static ServerPlayer makeServerPlayer(GameTestHelper context) {
        return context.makeMockServerPlayerInLevel();
    }

    private static final class DataBlock extends BaseEntityBlock {
        private static final MapCodec<DataBlock> CODEC = simpleCodec(DataBlock::new);

        private DataBlock(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
            return new DataBlockEntity(position, state);
        }

        @Override
        protected MapCodec<? extends BaseEntityBlock> codec() {
            return CODEC;
        }

        @Override
        protected RenderShape getRenderShape(BlockState state) {
            return RenderShape.MODEL;
        }
    }

    private static final class DataBlockEntity extends BlockEntity {
        private int marker;
        private String lock = "";

        private DataBlockEntity(BlockPos position, BlockState state) {
            super(TEST_BLOCK_ENTITY, position, state);
        }

        private void setMarker(int marker) {
            this.marker = marker;
        }

        private void setLock(String lock) {
            this.lock = lock;
        }

        @Override
        protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            tag.putInt("marker", marker);
            if (!lock.isEmpty()) {
                tag.putString("Lock", lock);
            }
        }

        @Override
        protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            marker = tag.getInt("marker");
            lock = tag.getString("Lock");
        }
    }
}
