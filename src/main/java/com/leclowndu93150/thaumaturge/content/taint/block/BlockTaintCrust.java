package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.content.entity.EntityFallingTaint;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSporeSwarmer;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluid;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public final class BlockTaintCrust extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintCrust> CODEC = simpleCodec(BlockTaintCrust::new);

    private static final int BIOME_DECAY_ONE_IN = 20;
    private static final int SWARMER_ONE_IN = 200;
    private static final double SWARMER_SPACING = 16.0;
    private static final double HALF = 0.5;
    private static final Direction[] ENCLOSING_SIDES = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    private static final int LOG_SEARCH_REACH = 1;
    private static final int GOO_LEVELS_THAT_HOLD = 4;
    private static final int TOPPLE_DROP_DEPTH = 3;

    public BlockTaintCrust(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintCrust> codec() {
        return CODEC;
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, FluxGooFluid.gooBlockState(PhysicalFlux.MAX_QUANTA));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (!TaintBiomeManager.isTainted(level, pos)) {
            if (random.nextInt(BIOME_DECAY_ONE_IN) == 0) {
                decay(level, pos, state);
            } else {
                shed(level, pos, state, random);
            }
            return;
        }
        if (shed(level, pos, state, random)) {
            return;
        }
        TaintHelper.attemptFibreGrowth(level, pos, false);
        if (!rollSwarmer(level, pos, random) && isEnclosed(level, pos)) {
            decay(level, pos, state);
        }
    }

    private static boolean shed(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (!TaintHelper.isRootless(level, pos)) {
            return false;
        }
        if (letsCrustThrough(level, pos.below())) {
            startFall(level, pos, pos, state);
            return true;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        BlockPos side = pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        if (!canToppleTo(level, pos, side)) {
            return false;
        }
        startFall(level, pos, side, state);
        return true;
    }

    private static boolean canToppleTo(ServerLevel level, BlockPos pos, BlockPos side) {
        if (!letsCrustThrough(level, side) || !letsCrustThrough(level, side.below())) {
            return false;
        }
        for (int depth = 1; depth <= TOPPLE_DROP_DEPTH; depth++) {
            if (!isCrust(level, pos.below(depth)) || !level.getBlockState(side.below(depth)).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static boolean letsCrustThrough(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(TTBlocks.FLUX_GOO) && PhysicalFlux.amount(level, pos) >= GOO_LEVELS_THAT_HOLD) {
            return false;
        }
        return isPassable(state) && !hasLogNearby(level, pos);
    }

    private static boolean isPassable(BlockState state) {
        if (state.isAir() || state.canBeReplaced() || state.is(BlockTags.FIRE) || state.is(TTBlocks.TAINT_FIBRE)) {
            return true;
        }
        FluidState fluid = state.getFluidState();
        return state.getBlock() instanceof LiquidBlock && (fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA));
    }

    private static boolean hasLogNearby(Level level, BlockPos pos) {
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-LOG_SEARCH_REACH, -LOG_SEARCH_REACH, -LOG_SEARCH_REACH), pos.offset(LOG_SEARCH_REACH, LOG_SEARCH_REACH, LOG_SEARCH_REACH))) {
            if (level.getBlockState(near).is(BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }

    private static void startFall(ServerLevel level, BlockPos origin, BlockPos start, BlockState state) {
        level.addFreshEntity(new EntityFallingTaint(level, start.getX() + HALF, start.getY(), start.getZ() + HALF, state, origin));
    }

    private static boolean rollSwarmer(ServerLevel level, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.above()).isAir() && random.nextInt(SWARMER_ONE_IN) == 0 && trySpawnSwarmer(level, pos);
    }

    private static boolean isEnclosed(Level level, BlockPos pos) {
        return Arrays.stream(ENCLOSING_SIDES).allMatch(side -> isCrust(level, pos.relative(side)));
    }

    private static boolean isCrust(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TTBlocks.TAINT_CRUST);
    }

    private static boolean trySpawnSwarmer(ServerLevel level, BlockPos pos) {
        if (!level.getEntitiesOfClass(EntityTaintSporeSwarmer.class, new AABB(pos).inflate(SWARMER_SPACING)).isEmpty()) {
            return false;
        }
        EntityTaintSporeSwarmer swarmer = TTEntities.TAINT_SPORE_SWARMER.get().create(level, EntitySpawnReason.NATURAL);
        if (swarmer == null) {
            return false;
        }
        replaceWithSwarmer(level, pos, swarmer);
        return true;
    }

    private static void replaceWithSwarmer(ServerLevel level, BlockPos pos, EntityTaintSporeSwarmer swarmer) {
        swarmer.snapTo(pos.getX() + HALF, pos.getY(), pos.getZ() + HALF, 0.0F, 0.0F);
        level.removeBlock(pos, false);
        level.addFreshEntity(swarmer);
    }
}
