package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class MagicForestFloraFeature extends Feature<MagicForestFloraConfig> {
    private static final int GRASS_MIN_Y = 30;
    private static final int VISHROOM_MIN_Y = 50;
    private static final int PLACE_FLAGS = 19;

    public MagicForestFloraFeature(Codec<MagicForestFloraConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MagicForestFloraConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        MagicForestFloraConfig config = context.config();
        BlockPos origin = context.origin();
        boolean any = false;

        for (int a = 0; a < config.grassAttempts(); a++) {
            int x = origin.getX() + 4 + random.nextInt(8);
            int z = origin.getZ() + 4 + random.nextInt(8);
            BlockPos pos = surfacePos(level, x, z);
            while (pos.getY() > GRASS_MIN_Y && !level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                pos = pos.below();
            }
            if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(pos, config.ambientGrass().defaultBlockState(), PLACE_FLAGS);
                any = true;
                break;
            }
        }

        for (int a = 0; a < config.vishroomAttempts(); a++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            BlockPos pos = surfacePos(level, x, z);
            while (pos.getY() > VISHROOM_MIN_Y && !level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                pos = pos.below();
            }
            BlockPos above = pos.above();
            var vishroom = config.vishroom().defaultBlockState();
            if (level.getBlockState(above).canBeReplaced() && vishroom.canSurvive(level, above)) {
                level.setBlock(above, vishroom, PLACE_FLAGS);
                any = true;
            }
        }
        return any;
    }

    private static BlockPos surfacePos(WorldGenLevel level, int x, int z) {
        return new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
    }
}
