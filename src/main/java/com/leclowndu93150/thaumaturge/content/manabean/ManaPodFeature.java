package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.fml.ModList;

public final class ManaPodFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CAVE_SCAN_BELOW = 8;
    private static final int CAVE_SCAN_ABOVE = 8;
    private static final int SURFACE_SCAN_BELOW = 32;
    private static final int SURFACE_SCAN_ABOVE = 16;
    private static final int SURFACE_CENTRE_OFFSET = 1;
    private static final int MIN_START_ABOVE_FLOOR = 1;
    private static final int DRIFT_BOUND = 4;
    private static final int DYNAMIC_TREE_COLUMNS_PER_LEVEL = 8;
    private static final int MIN_POD_AGE = 2;
    private static final int POD_AGE_SPREAD = 5;

    public ManaPodFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int centre;
        int below;
        int above;
        if (level.getBiome(origin).is(TTBiomes.MAGICAL_FOREST_CAVES)) {
            centre = origin.getY();
            below = CAVE_SCAN_BELOW;
            above = CAVE_SCAN_ABOVE;
        } else {
            centre = level.getHeight(Heightmap.Types.MOTION_BLOCKING, origin.getX(), origin.getZ()) - SURFACE_CENTRE_OFFSET;
            below = SURFACE_SCAN_BELOW;
            above = SURFACE_SCAN_ABOVE;
        }
        int start = Math.max(centre - below, level.getMinY() + MIN_START_ABOVE_FLOOR);
        int end = Math.min(centre + above, level.getMaxY());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int x = origin.getX();
        int z = origin.getZ();
        boolean dynamicTrees = ModList.get().isLoaded(TTIds.DYNAMIC_TREES);
        int columnChecksPerLevel = dynamicTrees ? DYNAMIC_TREE_COLUMNS_PER_LEVEL : 1;
        for (int y = start; y <= end; y++) {
            for (int check = 0; check < columnChecksPerLevel; check++) {
                pos.set(x, y, z);
                boolean clear = level.isEmptyBlock(pos) && level.isEmptyBlock(pos.below());
                if (clear && BlockManaPod.canGrowAt(level, pos)) {
                    placePod(level, pos, random);
                    return true;
                }
                if (!clear || dynamicTrees) {
                    x = origin.getX() + random.nextInt(DRIFT_BOUND) - random.nextInt(DRIFT_BOUND);
                    z = origin.getZ() + random.nextInt(DRIFT_BOUND) - random.nextInt(DRIFT_BOUND);
                }
            }
        }
        return true;
    }

    private static void placePod(WorldGenLevel level, BlockPos pos, RandomSource random) {
        int age = MIN_POD_AGE + random.nextInt(POD_AGE_SPREAD);
        level.setBlock(pos, BlockManaPod.placementState(level, pos).setValue(BlockManaPod.AGE, age), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(pos) instanceof BlockEntityManaPod pod) {
            pod.assignWildAspect(level.registryAccess(), random);
        }
    }
}
