package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthIndex;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthRuntime;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazePlan;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class OuterLandsChunkGenerator extends ChunkGenerator {
    public static final MapCodec<OuterLandsChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Biome.CODEC.fieldOf("biome").forGetter(generator -> generator.biome),
                            BandSettings.CODEC
                                    .optionalFieldOf("band", BandSettings.DEFAULT)
                                    .forGetter(generator -> generator.band))
                    .apply(instance, OuterLandsChunkGenerator::new));

    public static final int GEN_DEPTH = 128;

    private final Holder<Biome> biome;
    private final BandSettings band;
    private final LabyrinthRuntime runtime;

    public OuterLandsChunkGenerator(Holder<Biome> biome, BandSettings band) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
        this.band = band;
        this.runtime = new LabyrinthRuntime(band);
    }

    public LabyrinthRuntime runtime() {
        return runtime;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        if (runtime.index().planAt(pos.x, pos.z, LabyrinthIndex.MARGIN_CHUNKS) != null) {
            BandFiller.fill(chunk, band);
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(
            WorldGenRegion level, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {}

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        ChunkPos pos = chunk.getPos();
        MazePlan plan = runtime.index().planAt(pos.x, pos.z, 0);
        if (plan != null) {
            BandFiller.ensure(chunk, band);
            RoomStamper.stamp(level, chunk, plan, runtime);
        }
    }

    @Override
    public void createStructures(
            RegistryAccess registryAccess,
            ChunkGeneratorStructureState state,
            StructureManager structureManager,
            ChunkAccess centerChunk,
            StructureTemplateManager templateManager) {}

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess centerChunk) {}

    @Override
    public void applyCarvers(
            WorldGenRegion region,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {}

    @Override
    public int getBaseHeight(
            int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return band.floorY() + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos) {
        MazePlan plan = runtime.index().planAtBlock(feetPos.getX(), feetPos.getZ());
        if (plan == null) {
            return;
        }
        MazeGeometry geometry = plan.geometry();
        int cellX = geometry.cellOfBlockX(feetPos.getX());
        int cellZ = geometry.cellOfBlockZ(feetPos.getZ());
        String room = plan.roomAt(cellX, cellZ)
                .map(placed -> placed.type().location() + " t" + placed.transform())
                .orElse("-");
        result.add("Labyrinth " + plan.id().value() + " cell " + cellX + "," + cellZ + " " + room);
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getGenDepth() {
        return GEN_DEPTH;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }
}
