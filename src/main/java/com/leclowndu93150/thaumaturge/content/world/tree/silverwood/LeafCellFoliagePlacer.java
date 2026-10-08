package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import net.minecraft.world.level.LevelSimulatedReader;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public final class LeafCellFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<LeafCellFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> foliagePlacerParts(instance).apply(instance, LeafCellFoliagePlacer::new));

    public LeafCellFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return TTTreePlacers.LEAF_CELL_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader reader, FoliagePlacer.FoliageSetter foliageSetter, RandomSource random, TreeConfiguration config, int treeHeight, FoliagePlacer.FoliageAttachment foliageAttachment, int foliageHeight, int leafRadius, int offset) {
        if (!(reader instanceof WorldGenLevel level)) {
            return;
        }
        BlockPos cell = foliageAttachment.pos();
        BlockState present = level.getBlockState(cell);
        if (present.is(config.foliageProvider.getState(random, cell).getBlock())) {
            foliageSetter.set(cell, present);
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
