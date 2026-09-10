package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.jspecify.annotations.Nullable;

public final class MagicForestFloraFeature extends Feature<MagicForestFloraConfig> {
    private static final int GRASS_MIN_Y = 30;
    private static final int VISHROOM_MIN_Y = 50;
    private static final int GIANT_MUSHROOM_GRID_SIZE = 4;
    private static final int GIANT_MUSHROOM_CHANCE = 40;
    private static final int FLOWER_ATTEMPTS = 10;
    private static final int TALL_GRASS_ATTEMPTS = 12;
    private static final int SHORT_GRASS_ATTEMPTS = 10;
    private static final int FERN_ATTEMPTS = 6;
    private static final int MUSHROOM_ATTEMPTS = 6;
    private static final int PLACE_FLAGS = 19;
    private static final HugeMushroomFeatureConfiguration HUGE_BROWN_MUSHROOM = new HugeMushroomFeatureConfiguration(
            BlockStateProvider.simple(Blocks.BROWN_MUSHROOM_BLOCK), BlockStateProvider.simple(Blocks.MUSHROOM_STEM), 3);
    private static final HugeMushroomFeatureConfiguration HUGE_RED_MUSHROOM = new HugeMushroomFeatureConfiguration(
            BlockStateProvider.simple(Blocks.RED_MUSHROOM_BLOCK), BlockStateProvider.simple(Blocks.MUSHROOM_STEM), 3);

    public MagicForestFloraFeature(Codec<MagicForestFloraConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MagicForestFloraConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        MagicForestFloraConfig config = context.config();
        BlockPos origin = context.origin();
        BlockPos chunkOrigin = new BlockPos(origin.getX() & ~15, origin.getY(), origin.getZ() & ~15);
        boolean any = false;

        for (int x = 0; x < GIANT_MUSHROOM_GRID_SIZE; x++) {
            for (int z = 0; z < GIANT_MUSHROOM_GRID_SIZE; z++) {
                if (random.nextInt(GIANT_MUSHROOM_CHANCE) == 0) {
                    int blockX = chunkOrigin.getX() + 3 + x * 3;
                    int blockZ = chunkOrigin.getZ() + 3 + z * 3;
                    int blockY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
                    HugeMushroomFeatureConfiguration mushroom =
                            random.nextBoolean() ? HUGE_BROWN_MUSHROOM : HUGE_RED_MUSHROOM;
                    any |= (mushroom == HUGE_BROWN_MUSHROOM ? Feature.HUGE_BROWN_MUSHROOM : Feature.HUGE_RED_MUSHROOM)
                            .place(
                                    mushroom,
                                    level,
                                    context.chunkGenerator(),
                                    random,
                                    new BlockPos(blockX, blockY, blockZ));
                }
            }
        }

        for (int a = 0; a < FLOWER_ATTEMPTS; a++) {
            any |= placePlant(level, random, chunkOrigin, Blocks.DANDELION.defaultBlockState());
        }

        for (int a = 0; a < TALL_GRASS_ATTEMPTS; a++) {
            any |= placePlant(level, random, chunkOrigin, Blocks.TALL_GRASS.defaultBlockState());
        }

        for (int a = 0; a < SHORT_GRASS_ATTEMPTS; a++) {
            any |= placePlant(level, random, chunkOrigin, Blocks.SHORT_GRASS.defaultBlockState());
        }

        for (int a = 0; a < FERN_ATTEMPTS; a++) {
            any |= placePlant(level, random, chunkOrigin, Blocks.FERN.defaultBlockState());
        }

        for (int a = 0; a < MUSHROOM_ATTEMPTS; a++) {
            if (random.nextInt(4) == 0) {
                any |= placePlant(level, random, chunkOrigin, Blocks.BROWN_MUSHROOM.defaultBlockState());
            }
            if (random.nextInt(8) == 0) {
                any |= placePlant(level, random, chunkOrigin, Blocks.RED_MUSHROOM.defaultBlockState());
            }
        }

        for (int a = 0; a < config.grassAttempts(); a++) {
            int x = chunkOrigin.getX() + 4 + random.nextInt(8);
            int z = chunkOrigin.getZ() + 4 + random.nextInt(8);
            BlockPos grass = findGrass(level, x, z, GRASS_MIN_Y);
            if (grass != null) {
                level.setBlock(grass, config.ambientGrass().defaultBlockState(), PLACE_FLAGS);
                any = true;
            }
        }

        for (int a = 0; a < config.vishroomAttempts(); a++) {
            int x = chunkOrigin.getX() + random.nextInt(16);
            int z = chunkOrigin.getZ() + random.nextInt(16);
            BlockPos grass = findGrass(level, x, z, VISHROOM_MIN_Y);
            if (grass == null) continue;
            BlockPos above = grass.above();
            var vishroom = config.vishroom().defaultBlockState();
            if (level.getBlockState(above).canBeReplaced()
                    && vishroom.canSurvive(level, above)
                    && isAdjacentToWood(level, above)) {
                level.setBlock(above, vishroom, PLACE_FLAGS);
                any = true;
            }
        }
        return any;
    }

    private static BlockPos surfacePos(WorldGenLevel level, int x, int z) {
        return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
    }

    private static boolean placePlant(WorldGenLevel level, RandomSource random, BlockPos origin, BlockState state) {
        int x = origin.getX() + random.nextInt(16);
        int z = origin.getZ() + random.nextInt(16);
        BlockPos grass = findGrass(level, x, z, GRASS_MIN_Y);
        if (grass == null) return false;
        BlockPos pos = grass.above();
        if (!level.getBlockState(pos).canBeReplaced() || !state.canSurvive(level, pos)) return false;
        if (state.getBlock() instanceof DoublePlantBlock) {
            if (!level.getBlockState(pos.above()).canBeReplaced()) return false;
            DoublePlantBlock.placeAt(level, state, pos, PLACE_FLAGS);
            return true;
        }
        return level.setBlock(pos, state, PLACE_FLAGS);
    }

    private static @Nullable BlockPos findGrass(WorldGenLevel level, int x, int z, int minimumY) {
        BlockPos pos = surfacePos(level, x, z);
        while (pos.getY() > minimumY) {
            if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) return pos;
            pos = pos.below();
        }
        return null;
    }

    private static boolean isAdjacentToWood(WorldGenLevel level, BlockPos pos) {
        for (BlockPos adjacentPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (!adjacentPos.equals(pos) && level.getBlockState(adjacentPos).is(BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }
}
