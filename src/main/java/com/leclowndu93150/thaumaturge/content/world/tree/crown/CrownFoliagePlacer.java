package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.leclowndu93150.thaumaturge.content.world.tree.TreeLeafUpdater;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public final class CrownFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<CrownFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> foliagePlacerParts(instance).and(Codec.BOOL.fieldOf("absorb_foreign_leaves").forGetter(placer -> placer.absorbForeignLeaves)).apply(instance, CrownFoliagePlacer::new));

    private static final int CLUSTER_LAYERS = 4;
    private static final float RIM_RADIUS = 2.0F;
    private static final float BELLY_RADIUS = 3.0F;
    private static final double RADIUS_SLACK = 0.618;
    private static final double CELL_CENTER = 0.5;

    private final boolean absorbForeignLeaves;

    public CrownFoliagePlacer(IntProvider radius, IntProvider offset, boolean absorbForeignLeaves) {
        super(radius, offset);
        this.absorbForeignLeaves = absorbForeignLeaves;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return TTTreePlacers.CROWN_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, RandomSource random, TreeConfiguration config, int treeHeight, FoliagePlacer.FoliageAttachment foliageAttachment, int foliageHeight, int leafRadius, int offset) {
        BlockPos center = foliageAttachment.pos();
        BlockState leaves = config.foliageProvider.getState(level, random, center);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int layer = 0; layer < CLUSTER_LAYERS; layer++) {
            float radius = layer == 0 || layer == CLUSTER_LAYERS - 1 ? RIM_RADIUS : BELLY_RADIUS;
            int span = (int) (radius + RADIUS_SLACK);
            for (int dx = -span; dx <= span; dx++) {
                for (int dz = -span; dz <= span; dz++) {
                    if (Math.pow(Math.abs(dx) + CELL_CENTER, 2.0) + Math.pow(Math.abs(dz) + CELL_CENTER, 2.0) <= radius * radius) {
                        cursor.setWithOffset(center, dx, layer, dz);
                        settleLeaf(level, foliageSetter, cursor, leaves);
                    }
                }
            }
        }
    }

    private void settleLeaf(WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, BlockPos pos, BlockState leaves) {
        BlockState present = level.getBlockState(pos);
        if (present.isAir()) {
            foliageSetter.set(pos, leaves);
        } else if (absorbForeignLeaves && present.is(BlockTags.LEAVES) && !present.is(leaves.getBlock())) {
            foliageSetter.set(pos, TreeLeafUpdater.carryDistance(leaves, present));
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeConfiguration config) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        return false;
    }
}
