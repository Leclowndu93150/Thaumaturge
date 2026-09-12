package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.content.entity.EntityFallingTaint;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSporeSwarmer;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
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
import net.minecraft.world.phys.AABB;

public final class BlockTaintCrust extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintCrust> CODEC = simpleCodec(BlockTaintCrust::new);

    private static final int CREEP_REACH = 4;
    private static final int GOO_BLOCKING_AMOUNT = 4;
    private static final int SWARMER_CHANCE = 200;
    private static final int OUTSIDE_BIOME_GOO_CHANCE = 20;

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
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (!TaintBiomeManager.isTainted(level, pos) && random.nextInt(OUTSIDE_BIOME_GOO_CHANCE) == 0) {
            die(level, pos, state);
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
            boolean canCreep = true;
            for (int a = 1; a < CREEP_REACH; a++) {
                if (!level.isEmptyBlock(pos.relative(dir).below(a))
                        || !level.getBlockState(pos.below(a)).is(this)) {
                    canCreep = false;
                    break;
                }
            }
            if (canCreep && tryToFall(level, pos, pos.relative(dir))) {
                return;
            }
        }

        if (!TaintBiomeManager.isTainted(level, pos)) {
            return;
        }
        TaintHelper.spreadFibres(level, pos, false);

        // TC4 exposed crust occasionally uprooted itself into a Swarmer.
        if (level.isEmptyBlock(pos.above())
                && random.nextInt(SWARMER_CHANCE) == 0
                && level.getEntitiesOfClass(EntityTaintSporeSwarmer.class, new AABB(pos).inflate(16.0))
                        .isEmpty()) {
            level.removeBlock(pos, false);
            EntityTaintSporeSwarmer swarmer =
                    TCEntities.TAINT_SPORE_SWARMER.get().create(level);
            if (swarmer != null) {
                swarmer.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                level.addFreshEntity(swarmer);
            }
            return;
        }

        // Fully enclosed crust collapsed back into concentrated Flux Goo.
        if (level.getBlockState(pos.above()).is(this)) {
            boolean enclosed = true;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (!level.getBlockState(pos.relative(direction)).is(this)) {
                    enclosed = false;
                    break;
                }
            }
            if (enclosed) {
                level.setBlock(pos, TCBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
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
                level, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, level.getBlockState(pos), pos);
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
