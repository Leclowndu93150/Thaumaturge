package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.content.entity.EntityFallingTaint;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class BlockTaintCrust extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintCrust> CODEC = simpleCodec(BlockTaintCrust::new);

    private static final int CREEP_REACH = 4;
    private static final int GOO_BLOCKING_AMOUNT = 4;

    public BlockTaintCrust(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintCrust> codec() {
        return CODEC;
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TCBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (tryToFall(level, pos, pos)) {
            return;
        }
        if (level.isEmptyBlock(pos.above())) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            for (int a = 1; a < CREEP_REACH; a++) {
                if (!level.isEmptyBlock(pos.relative(dir).below(a))) {
                    return;
                }
                if (!level.getBlockState(pos.below(a)).is(this)) {
                    return;
                }
            }
            tryToFall(level, pos, pos.relative(dir));
        }
    }

    private boolean tryToFall(ServerLevel level, BlockPos pos, BlockPos target) {
        if (!BlockTaintFibre.isOnlyAdjacentToTaint(level, pos)) {
            return false;
        }
        if (!canFallBelow(level, target.below()) || target.getY() < level.getMinY()) {
            return false;
        }
        EntityFallingTaint falling = new EntityFallingTaint(level, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, level.getBlockState(pos), pos);
        level.addFreshEntity(falling);
        return true;
    }

    public static boolean canFallBelow(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                for (int yy = -1; yy <= 1; yy++) {
                    if (level.getBlockState(pos.offset(xx, yy, zz)).is(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }
        if (state.isAir()) {
            return true;
        }
        FluidState fluid = state.getFluidState();
        if (state.is(TCBlocks.FLUX_GOO.get()) && fluid.getAmount() >= GOO_BLOCKING_AMOUNT) {
            return false;
        }
        if (state.getBlock() instanceof BaseFireBlock || state.is(TCBlocks.TAINT_FIBRE.get())) {
            return true;
        }
        if (state.canBeReplaced()) {
            return true;
        }
        return fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
    }
}
