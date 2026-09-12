package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTaintLog extends RotatedPillarBlock implements ITaintBlock {
    public static final MapCodec<BlockTaintLog> CODEC = simpleCodec(BlockTaintLog::new);

    private static final int DIE_CHANCE = 10;

    public BlockTaintLog(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintLog> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (!TaintHelper.isEcologicallySustained(level, pos) && random.nextInt(DIE_CHANCE) == 0) {
            die(level, pos, state);
            return;
        }
        TaintHelper.spreadFibres(level, pos, false);
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TCBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    public BlockState withAxis(Direction.Axis axis) {
        return this.defaultBlockState().setValue(AXIS, axis);
    }
}
