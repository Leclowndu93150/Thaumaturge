package com.leclowndu93150.thaumaturge.content.aura.node;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class NodeFeature extends Feature<NodeFeatureConfig> {
    private static final int MAX_RISE = 4;

    public NodeFeature(Codec<NodeFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NodeFeatureConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        NodeFeatureConfig config = context.config();
        BlockPos pos = context.origin();
        if (level.getBlockState(pos.above()).isAir()) {
            pos = pos.above();
        }
        int rise = random.nextInt(MAX_RISE);
        BlockPos risen = pos.above(rise);
        if (level.getBlockState(risen).isAir() || level.getBlockState(risen).canBeReplaced()) {
            pos = risen;
        }
        if (pos.getY() > level.getMaxY()) {
            return false;
        }
        return NodeGenerator.createRandomNodeAt(level, pos, random, config.silverwood(), config.eerie(), config.small(), config.specialRarity(), config.baseAura());
    }
}
