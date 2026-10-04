package com.leclowndu93150.thaumaturge.content.taint.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTaintSoil extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintSoil> CODEC = simpleCodec(BlockTaintSoil::new);

    public BlockTaintSoil(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintSoil> codec() {
        return CODEC;
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
    }
}
