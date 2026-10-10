package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jspecify.annotations.Nullable;

public final class MagicForestFloraFeature extends Feature<MagicForestFloraConfig> {
    private static final int CHUNK_SPAN = 16;
    private static final int CORE_INSET = 4;
    private static final int CORE_SPAN = 8;
    private static final int AMBIENT_GRASS_FLOOR = 30;
    private static final int VISHROOM_FLOOR = 50;
    private static final int MUSHROOM_GRID = 4;
    private static final int MUSHROOM_CELL = 4;
    private static final int MUSHROOM_CELL_CENTRE = 2;
    private static final int MUSHROOM_JITTER = 1;
    private static final int LOG_REACH = 1;
    private static final int PATCH_TRIES = 8;
    private static final int PATCH_SPREAD_XZ = 3;
    private static final int PATCH_SPREAD_Y = 1;
    private static final long FLOWER_NOISE_SEED = 0x6D61676963L;
    private static final int FLOWER_NOISE_FIRST_OCTAVE = 0;
    private static final double FLOWER_NOISE_AMPLITUDE = 1.0;
    private static final float FLOWER_NOISE_SCALE = 1.0F / 48.0F;
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS;

    public MagicForestFloraFeature(Codec<MagicForestFloraConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MagicForestFloraConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ChunkGenerator generator = context.chunkGenerator();
        MagicForestFloraConfig config = context.config();
        ChunkPos chunk = ChunkPos.containing(context.origin());
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        boolean placed = placeAmbientGrass(level, random, config, minX, minZ);
        placed |= placeHugeMushrooms(level, generator, random, config, minX, minZ);
        placed |= placeFlowers(level, generator, random, config, minX, minZ);
        placed |= placeGroundCover(level, generator, random, config, minX, minZ);
        placed |= placeVishrooms(level, random, config, minX, minZ);
        return placed;
    }

    private static boolean placeAmbientGrass(WorldGenLevel level, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        for (int attempt = 0; attempt < config.grassAttempts(); attempt++) {
            int x = minX + CORE_INSET + random.nextInt(CORE_SPAN);
            int z = minZ + CORE_INSET + random.nextInt(CORE_SPAN);
            BlockPos grass = grassBelowSky(level, x, z, AMBIENT_GRASS_FLOOR);
            if (grass != null) {
                level.setBlock(grass, config.ambientGrass().withPropertiesOf(level.getBlockState(grass)), PLACE_FLAGS);
                return true;
            }
        }
        return false;
    }

    private static boolean placeHugeMushrooms(WorldGenLevel level, ChunkGenerator generator, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        boolean placed = false;
        for (int cellX = 0; cellX < MUSHROOM_GRID; cellX++) {
            for (int cellZ = 0; cellZ < MUSHROOM_GRID; cellZ++) {
                if (random.nextInt(config.hugeMushroomRarity()) != 0) {
                    continue;
                }
                int x = minX + cellX * MUSHROOM_CELL + MUSHROOM_CELL_CENTRE + random.nextIntBetweenInclusive(-MUSHROOM_JITTER, MUSHROOM_JITTER);
                int z = minZ + cellZ * MUSHROOM_CELL + MUSHROOM_CELL_CENTRE + random.nextIntBetweenInclusive(-MUSHROOM_JITTER, MUSHROOM_JITTER);
                BlockPos ground = groundSpot(level, x, z);
                placed |= config.hugeMushrooms().getRandomElement(random).map(Holder::value).map(mushroom -> mushroom.place(level, generator, random, ground)).orElse(false);
            }
        }
        return placed;
    }

    private static boolean placeFlowers(WorldGenLevel level, ChunkGenerator generator, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        List<BlockState> flowers = config.flowers().stream().map(holder -> holder.value().defaultBlockState()).toList();
        if (flowers.isEmpty()) {
            return false;
        }
        NoiseProvider bands = new NoiseProvider(FLOWER_NOISE_SEED, new NormalNoise.NoiseParameters(FLOWER_NOISE_FIRST_OCTAVE, FLOWER_NOISE_AMPLITUDE), FLOWER_NOISE_SCALE, flowers);
        SimpleBlockConfiguration flower = new SimpleBlockConfiguration(bands);
        boolean placed = false;
        for (int attempt = 0; attempt < config.flowerAttempts(); attempt++) {
            placed |= scatter(level, generator, random, flower, randomGroundSpot(level, random, minX, minZ));
        }
        return placed;
    }

    private static boolean placeGroundCover(WorldGenLevel level, ChunkGenerator generator, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        boolean placed = false;
        for (MagicForestFloraConfig.PlantPatch patch : config.plants()) {
            SimpleBlockConfiguration plant = new SimpleBlockConfiguration(patch.state());
            for (int attempt = 0; attempt < patch.attempts(); attempt++) {
                if (random.nextInt(patch.rarity()) == 0) {
                    placed |= scatter(level, generator, random, plant, randomGroundSpot(level, random, minX, minZ));
                }
            }
        }
        return placed;
    }

    private static boolean placeVishrooms(WorldGenLevel level, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        boolean placed = false;
        BlockState vishroom = config.vishroom().defaultBlockState();
        for (int attempt = 0; attempt < config.vishroomAttempts(); attempt++) {
            BlockPos grass = grassBelowSky(level, minX + random.nextInt(CHUNK_SPAN), minZ + random.nextInt(CHUNK_SPAN), VISHROOM_FLOOR);
            if (grass == null) {
                continue;
            }
            BlockPos spot = grass.above();
            if (roomForPlant(level.getBlockState(spot)) && besideLog(level, spot) && vishroom.canSurvive(level, spot)) {
                level.setBlock(spot, vishroom, PLACE_FLAGS);
                placed = true;
            }
        }
        return placed;
    }

    private static boolean roomForPlant(BlockState state) {
        return state.canBeReplaced() && state.getFluidState().isEmpty() && !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);
    }

    private static boolean besideLog(WorldGenLevel level, BlockPos spot) {
        return BlockPos.betweenClosedStream(spot.offset(-LOG_REACH, -LOG_REACH, -LOG_REACH), spot.offset(LOG_REACH, LOG_REACH, LOG_REACH)).anyMatch(pos -> level.getBlockState(pos).is(BlockTags.LOGS));
    }

    private static boolean scatter(WorldGenLevel level, ChunkGenerator generator, RandomSource random, SimpleBlockConfiguration plant, BlockPos centre) {
        boolean placed = false;
        for (int attempt = 0; attempt < PATCH_TRIES; attempt++) {
            BlockPos spot = centre.offset(random.nextIntBetweenInclusive(-PATCH_SPREAD_XZ, PATCH_SPREAD_XZ), random.nextIntBetweenInclusive(-PATCH_SPREAD_Y, PATCH_SPREAD_Y),
                    random.nextIntBetweenInclusive(-PATCH_SPREAD_XZ, PATCH_SPREAD_XZ));
            if (level.isEmptyBlock(spot)) {
                placed |= Feature.SIMPLE_BLOCK.place(plant, level, generator, random, spot);
            }
        }
        return placed;
    }

    private static BlockPos randomGroundSpot(WorldGenLevel level, RandomSource random, int minX, int minZ) {
        return groundSpot(level, minX + random.nextInt(CHUNK_SPAN), minZ + random.nextInt(CHUNK_SPAN));
    }

    private static BlockPos groundSpot(WorldGenLevel level, int x, int z) {
        return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
    }

    private static @Nullable BlockPos grassBelowSky(WorldGenLevel level, int x, int z, int floorY) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z);
        while (probe.getY() > floorY) {
            if (level.getBlockState(probe).is(Blocks.GRASS_BLOCK)) {
                return probe.immutable();
            }
            probe.move(0, -1, 0);
        }
        return null;
    }
}
