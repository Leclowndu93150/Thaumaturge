package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class MagicalCavePondFeature extends Feature<NoneFeatureConfiguration> {
    public MagicalCavePondFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (context.random().nextInt(10) != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos surface = origin.below();
        if (!level.getBlockState(origin).isAir()
                || !level.getBlockState(surface).is(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)) {
            return false;
        }

        int radius = context.random().nextInt(4) == 0 ? 2 : 1;
        int localX = origin.getX() & 15;
        int localZ = origin.getZ() & 15;
        BlockPos.MutableBlockPos cellSurface = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos waterPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos support = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int dx = Math.max(-radius, -localX); dx <= Math.min(radius, 15 - localX); dx++) {
            for (int dz = Math.max(-radius, -localZ); dz <= Math.min(radius, 15 - localZ); dz++) {
                if (dx * dx + dz * dz > radius * radius + 1) {
                    continue;
                }

                cellSurface.setWithOffset(surface, dx, 0, dz);
                waterPos.set(cellSurface).move(Direction.DOWN);
                support.set(waterPos).move(Direction.DOWN);
                above.set(cellSurface).move(Direction.UP);
                if (!level.getBlockState(cellSurface).is(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)
                        || !level.getBlockState(above).isAir()
                        || !level.getBlockState(waterPos).is(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)
                        || !level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
                    continue;
                }

                level.setBlock(cellSurface, Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);
                placed = true;
            }
        }
        return placed;
    }
}
