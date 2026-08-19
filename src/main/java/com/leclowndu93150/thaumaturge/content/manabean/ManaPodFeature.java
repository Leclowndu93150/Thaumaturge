package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ManaPodFeature extends Feature<NoneFeatureConfiguration> {
    private static final int START_Y = 64;
    private static final int DRIFT = 4;
    private static final int MIN_START_AGE = 2;
    private static final int START_AGE_SPREAD = 5;

    public ManaPodFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int baseX = context.origin().getX();
        int baseZ = context.origin().getZ();
        int x = baseX;
        int z = baseZ;
        int y = Math.max(START_Y, level.getMinY() + 1);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
        while (cursor.getY() < Math.min(level.getMaxY(), level.getHeight(Heightmap.Types.MOTION_BLOCKING, cursor.getX(), cursor.getZ()))) {
            if (level.isEmptyBlock(cursor) && level.isEmptyBlock(cursor.below())) {
                if (BlockManaPod.canGrowAt(level, cursor)) {
                    int age = MIN_START_AGE + random.nextInt(START_AGE_SPREAD);
                    level.setBlock(cursor, TCBlocks.MANA_POD.get().defaultBlockState().setValue(BlockManaPod.AGE, age), 2);
                    if (level.getBlockEntity(cursor) instanceof BlockEntityManaPod pod) {
                        pod.assignWildAspect(level.registryAccess(), random);
                    }
                    return true;
                }
            } else {
                cursor.setX(baseX + random.nextInt(DRIFT) - random.nextInt(DRIFT));
                cursor.setZ(baseZ + random.nextInt(DRIFT) - random.nextInt(DRIFT));
            }
            cursor.setY(cursor.getY() + 1);
        }
        return true;
    }
}
