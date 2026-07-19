package io.github.nie1ix.movewand.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import io.github.nie1ix.movewand.selection.BlockSelection;
import io.github.nie1ix.movewand.selection.SelectionEditor;

import java.util.Set;
import java.util.function.Predicate;

public final class MoveValidator {
    public static final int MAX_SELECTION_BLOCKS = SelectionEditor.DEFAULT_MAX_POSITIONS;
    private static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("c", "relocation_not_supported")
    );
    private static final TagKey<Block> MOVEWAND_RELOCATION_NOT_SUPPORTED = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("movewand", "relocation_not_supported")
    );
    private static final TagKey<Block> CREATE_NON_MOVABLE = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("create", "non_movable")
    );
    private static final TagKey<Block> FORGE_RELOCATION_NOT_SUPPORTED = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("forge", "relocation_not_supported")
    );

    private MoveValidator() {
    }

    public static boolean isUnmovable(BlockState state) {
        return state.is(Blocks.BEDROCK)
                || state.is(Blocks.SPAWNER)
                || state.is(Blocks.TRIAL_SPAWNER)
                || state.is(RELOCATION_NOT_SUPPORTED)
                || state.is(MOVEWAND_RELOCATION_NOT_SUPPORTED)
                || state.is(CREATE_NON_MOVABLE)
                || state.is(FORGE_RELOCATION_NOT_SUPPORTED);
    }

    public static MoveValidation validate(BlockSelection source, Set<BlockPos> targets, Predicate<BlockPos> isAir) {
        return validate(source, targets, isAir, MAX_SELECTION_BLOCKS);
    }

    public static MoveValidation validate(BlockSelection source, Set<BlockPos> targets, Predicate<BlockPos> isAir, int maxSelectionBlocks) {
        if (source.positions().size() > maxSelectionBlocks) {
            return MoveValidation.SELECTION_TOO_LARGE;
        }

        for (BlockPos target : targets) {
            if (!source.positions().contains(target) && !isAir.test(target)) {
                return MoveValidation.TARGET_OCCUPIED;
            }
        }
        return MoveValidation.VALID;
    }
}
