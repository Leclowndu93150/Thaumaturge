package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jspecify.annotations.Nullable;

public final class MagicForestFloraFeature extends Feature<MagicForestFloraConfig> {
    private static final int GRASS_MIN_Y = 30;
    private static final int VISHROOM_MIN_Y = 50;
    private static final int PLACE_FLAGS = 19;
    private static final double FLOWER_NOISE_SCALE = 48.0;
    private static final double MAX_FLOWER_SAMPLE = 0.9999;

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
        if (config.hugeMushrooms().size() > 0) {
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    if (random.nextInt(config.hugeMushroomRarity()) == 0) {
                        BlockPos grass = findGrass(
                                level, chunkOrigin.getX() + 3 + x * 3, chunkOrigin.getZ() + 3 + z * 3, GRASS_MIN_Y);
                        if (grass != null) {
                            any |= config.hugeMushrooms()
                                    .get(random.nextInt(config.hugeMushrooms().size()))
                                    .value()
                                    .place(level, context.chunkGenerator(), random, grass.above());
                        }
                    }
                }
            }
        }
        for (int attempt = 0;
                attempt < config.flowerAttempts() && config.flowers().size() > 0;
                attempt++) {
            BlockPos grass = randomGrass(level, random, chunkOrigin);
            if (grass != null) {
                any |= placePlant(level, grass.above(), flowerAt(config.flowers(), grass.above()));
            }
        }
        for (MagicForestFloraConfig.PlantPatch patch : config.plants()) {
            for (int attempt = 0; attempt < patch.attempts(); attempt++) {
                if (random.nextInt(patch.rarity()) != 0) {
                    continue;
                }
                BlockPos grass = randomGrass(level, random, chunkOrigin);
                if (grass != null) {
                    any |= placePlant(level, grass.above(), patch.state().getState(random, grass.above()));
                }
            }
        }
        for (int attempt = 0; attempt < config.grassAttempts(); attempt++) {
            BlockPos grass = findGrass(
                    level,
                    chunkOrigin.getX() + 4 + random.nextInt(8),
                    chunkOrigin.getZ() + 4 + random.nextInt(8),
                    GRASS_MIN_Y);
            if (grass != null) {
                any |= level.setBlock(grass, config.ambientGrass().defaultBlockState(), PLACE_FLAGS);
            }
        }
        for (int attempt = 0; attempt < config.vishroomAttempts(); attempt++) {
            BlockPos grass = findGrass(
                    level,
                    chunkOrigin.getX() + random.nextInt(16),
                    chunkOrigin.getZ() + random.nextInt(16),
                    VISHROOM_MIN_Y);
            if (grass != null && isAdjacentToWood(level, grass.above())) {
                any |= placePlant(level, grass.above(), config.vishroom().defaultBlockState());
            }
        }
        return any;
    }

    private static boolean placePlant(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof DoublePlantBlock) {
            state = state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
            if (pos.getY() >= level.getMaxBuildHeight() - 1
                    || !level.getBlockState(pos.above()).canBeReplaced()) {
                return false;
            }
        }
        if (!level.getBlockState(pos).canBeReplaced() || !state.canSurvive(level, pos)) {
            return false;
        }
        if (state.getBlock() instanceof DoublePlantBlock) {
            DoublePlantBlock.placeAt(level, state, pos, PLACE_FLAGS);
            return true;
        }
        return level.setBlock(pos, state, PLACE_FLAGS);
    }

    @SuppressWarnings("removal")
    private static BlockState flowerAt(HolderSet<Block> flowers, BlockPos pos) {
        double sample = Mth.clamp(
                (1.0
                                + Biome.BIOME_INFO_NOISE.getValue(
                                        pos.getX() / FLOWER_NOISE_SCALE, pos.getZ() / FLOWER_NOISE_SCALE, false))
                        / 2.0,
                0.0,
                MAX_FLOWER_SAMPLE);
        return flowers.get((int) (sample * flowers.size())).value().defaultBlockState();
    }

    private static @Nullable BlockPos randomGrass(WorldGenLevel level, RandomSource random, BlockPos origin) {
        return findGrass(level, origin.getX() + random.nextInt(16), origin.getZ() + random.nextInt(16), GRASS_MIN_Y);
    }

    private static @Nullable BlockPos findGrass(WorldGenLevel level, int x, int z, int minimumY) {
        BlockPos.MutableBlockPos pos =
                new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
        while (pos.getY() > minimumY) {
            if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                return pos.immutable();
            }
            pos.move(0, -1, 0);
        }
        return null;
    }

    private static boolean isAdjacentToWood(WorldGenLevel level, BlockPos pos) {
        for (BlockPos adjacent : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (!adjacent.equals(pos) && level.getBlockState(adjacent).is(BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }
}
