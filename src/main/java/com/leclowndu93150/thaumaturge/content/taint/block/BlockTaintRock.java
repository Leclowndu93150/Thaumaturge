package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTaintRock extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintRock> CODEC = simpleCodec(BlockTaintRock::new);

    public BlockTaintRock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintRock> codec() {
        return CODEC;
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TCBlocks.STONE_POROUS.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.spreadFibres(level, pos, false);
    }
}
