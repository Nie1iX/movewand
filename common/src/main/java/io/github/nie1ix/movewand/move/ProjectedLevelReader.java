package io.github.nie1ix.movewand.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;

final class ProjectedLevelReader implements LevelReader {
    private final LevelReader delegate;
    private final Map<BlockPos, BlockState> projectedStates;

    ProjectedLevelReader(LevelReader delegate, Map<BlockPos, BlockState> projectedStates) {
        this.delegate = delegate;
        this.projectedStates = projectedStates;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos position) {
        return delegate.getBlockEntity(position);
    }

    @Override
    public BlockState getBlockState(BlockPos position) {
        return projectedStates.getOrDefault(position, delegate.getBlockState(position));
    }

    @Override
    public FluidState getFluidState(BlockPos position) {
        return getBlockState(position).getFluidState();
    }

    @Override
    public float getShade(net.minecraft.core.Direction direction, boolean shaded) {
        return delegate.getShade(direction, shaded);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos position, ColorResolver resolver) {
        return delegate.getBlockTint(position, resolver);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return delegate.getChunkForCollisions(chunkX, chunkZ);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(Entity entity, AABB box) {
        return delegate.getEntityCollisions(entity, box);
    }

    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus status, boolean nonnull) {
        return delegate.getChunk(chunkX, chunkZ, status, nonnull);
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return delegate.hasChunk(chunkX, chunkZ);
    }

    @Override
    public int getHeight(Heightmap.Types type, int x, int z) {
        return delegate.getHeight(type, x, z);
    }

    @Override
    public int getSkyDarken() {
        return delegate.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return delegate.getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return delegate.getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public boolean isClientSide() {
        return delegate.isClientSide();
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return delegate.dimensionType();
    }

    @Override
    public RegistryAccess registryAccess() {
        return delegate.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return delegate.enabledFeatures();
    }

    @Override
    public EnvironmentAttributeReader environmentAttributes() {
        return delegate.environmentAttributes();
    }
}
