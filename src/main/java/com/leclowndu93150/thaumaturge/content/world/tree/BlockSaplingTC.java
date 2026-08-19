package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockSaplingTC extends SaplingBlock {
    public static final MapCodec<BlockSaplingTC> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(TreeGrower.CODEC.fieldOf("tree").forGetter(b -> b.treeGrower), propertiesCodec()).apply(i, BlockSaplingTC::new));

    private static final float BONEMEAL_SUCCESS_CHANCE = 0.25F;

    public BlockSaplingTC(TreeGrower treeGrower, BlockBehaviour.Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    public MapCodec<? extends BlockSaplingTC> codec() {
        return CODEC;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < BONEMEAL_SUCCESS_CHANCE;
    }
}
