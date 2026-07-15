package io.github.nie1ix.movewand.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import io.github.nie1ix.movewand.selection.ServerSelectionManager;

public final class MoveWandItem extends Item {
    public MoveWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (context.getPlayer() instanceof ServerPlayer player) {
            ServerSelectionManager.select(player, context.getClickedPos(), player.isShiftKeyDown());
        }
        return InteractionResult.CONSUME;
    }
}
