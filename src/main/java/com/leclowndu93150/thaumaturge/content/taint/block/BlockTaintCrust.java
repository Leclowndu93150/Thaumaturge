package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.content.entity.EntityFallingTaint;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSporeSwarmer;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintOrigins;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public final class BlockTaintCrust extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintCrust> CODEC = simpleCodec(BlockTaintCrust::new);

    private static final int CREEP_REACH = 4;
    private static final int GOO_BLOCKING_AMOUNT = 4;
    private static final int SWARMER_CHANCE = 200;
    private static final int OUTSIDE_BIOME_GOO_CHANCE = 20;
    private static final double SWARMER_SPACING = 16.0;
    private static final double CELL_CENTRE = 0.5;

    public BlockTaintCrust(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintCrust> codec() {
        return CODEC;
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!TaintOrigins.isSustained(level, pos) && random.nextInt(OUTSIDE_BIOME_GOO_CHANCE) == 0) {
            decay(level, pos, state);
            TaintBiomeManager.restoreIfCleared(level, pos);
            return;
        }
        subRandomTick(state, level, pos, random);
    }

    @Override
    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (tryToFall(level, pos, pos)) {
            return;
        }
        if (level.isEmptyBlock(pos.above())) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos side = pos.relative(dir);
            boolean canCreep = isPassable(level.getBlockState(side));
            for (int a = 1; canCreep && a < CREEP_REACH; a++) {
                if (!level.isEmptyBlock(side.below(a))
                        || !level.getBlockState(pos.below(a)).is(this)) {
                    canCreep = false;
                    break;
                }
            }
            if (canCreep && tryToFall(level, pos, side)) {
                return;
            }
        }

        if (!TaintOrigins.isSustained(level, pos)) {
            return;
        }
        TaintHelper.attemptFibreGrowth(level, pos, false);

        if (level.isEmptyBlock(pos.above())
                && random.nextInt(SWARMER_CHANCE) == 0
                && level.getEntitiesOfClass(EntityTaintSporeSwarmer.class, new AABB(pos).inflate(SWARMER_SPACING))
                        .isEmpty()) {
            EntityTaintSporeSwarmer swarmer =
                    TTEntities.TAINT_SPORE_SWARMER.get().create(level);
            if (swarmer != null) {
                swarmer.moveTo(pos.getX() + CELL_CENTRE, pos.getY(), pos.getZ() + CELL_CENTRE, 0.0F, 0.0F);
                level.removeBlock(pos, false);
                level.addFreshEntity(swarmer);
                return;
            }
        }

        if (level.getBlockState(pos.above()).is(this)) {
            boolean enclosed = true;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (!level.getBlockState(pos.relative(direction)).is(this)) {
                    enclosed = false;
                    break;
                }
            }
            if (enclosed) {
                decay(level, pos, state);
            }
        }
    }

    private boolean tryToFall(ServerLevel level, BlockPos pos, BlockPos target) {
        if (!BlockTaintFibre.isOnlyAdjacentToTaint(level, pos)) {
            return false;
        }
        if (!canFallBelow(level, target.below()) || target.getY() < level.getMinBuildHeight()) {
            return false;
        }
        EntityFallingTaint falling = new EntityFallingTaint(
                level,
                target.getX() + CELL_CENTRE,
                target.getY() + CELL_CENTRE,
                target.getZ() + CELL_CENTRE,
                level.getBlockState(pos),
                pos);
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
        if (state.is(TTBlocks.FLUX_GOO.get()) && state.getFluidState().getAmount() >= GOO_BLOCKING_AMOUNT) {
            return false;
        }
        return isPassable(state);
    }

    private static boolean isPassable(BlockState state) {
        if (state.isAir()
                || state.getBlock() instanceof BaseFireBlock
                || state.is(TTBlocks.TAINT_FIBRE.get())
                || state.canBeReplaced()) {
            return true;
        }
        FluidState fluid = state.getFluidState();
        return fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
    }
}
