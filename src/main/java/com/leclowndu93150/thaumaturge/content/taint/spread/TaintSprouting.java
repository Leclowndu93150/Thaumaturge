package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class TaintSprouting {
    private static final float MIN_SATURATION = 0.85F;
    private static final int SATELLITE_ATTEMPTS = 8;
    private static final int SATELLITE_HORIZONTAL_REACH = 4;
    private static final int SATELLITE_VERTICAL_REACH = 1;
    private static final float SPROUT_FLUX = 5.0F;
    private static final float SPROUT_PRESSURE = 0.08F;
    private static final double CELL_CENTRE = 0.5;
    private static final double CROWD_FRACTION = 0.8;
    private static final int FULL_TURN_DEGREES = 360;

    private TaintSprouting() {}

    public static boolean trySatellite(ServerLevel level, BlockPos origin, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL
                || ThaumaturgeCommonConfig.WUSS_MODE.get()
                || TaintBlooms.isProtected(level, origin)
                || TaintEcology.getSaturation(level, origin) < MIN_SATURATION) {
            return false;
        }
        for (int attempt = 0; attempt < SATELLITE_ATTEMPTS; attempt++) {
            BlockPos spot = origin.offset(
                    offset(random, SATELLITE_HORIZONTAL_REACH),
                    offset(random, SATELLITE_VERTICAL_REACH),
                    offset(random, SATELLITE_HORIZONTAL_REACH));
            if (level.hasChunkAt(spot) && trySprout(level, spot, false)) {
                return true;
            }
        }
        return false;
    }

    public static boolean trySprout(ServerLevel level, BlockPos ground, boolean fringe) {
        if (!isSproutingGround(level, ground)
                || level.getDifficulty() == Difficulty.PEACEFUL
                || AuraHelper.getFlux(level, ground) < SPROUT_FLUX) {
            return false;
        }
        if (fringe && !TaintOrigins.isOnFringe(level, ground)) {
            return false;
        }
        EntityTaintSeed seed = createSeedAt(level, ground.above());
        if (seed == null || !level.addFreshEntity(seed)) {
            return false;
        }
        TaintEcology.addPressure(level, ground, SPROUT_PRESSURE);
        return true;
    }

    public static boolean plant(ServerLevel level, BlockPos standing, float reserve) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || level.getDifficulty() == Difficulty.PEACEFUL
                || TaintBlooms.isProtected(level, standing)) {
            return false;
        }
        EntityTaintSeed seed = createSeedAt(level, standing);
        if (seed == null) {
            return false;
        }
        seed.setTaintReserve(reserve);
        if (!level.addFreshEntity(seed)) {
            return false;
        }
        TaintEcology.addPressure(level, standing, SPROUT_PRESSURE);
        return true;
    }

    private static @Nullable EntityTaintSeed createSeedAt(ServerLevel level, BlockPos standing) {
        EntityTaintSeed seed = TTEntities.TAINT_SEED.get().create(level);
        if (seed == null) {
            return null;
        }
        seed.moveTo(
                standing.getX() + CELL_CENTRE,
                standing.getY(),
                standing.getZ() + CELL_CENTRE,
                (float) level.getRandom().nextInt(FULL_TURN_DEGREES),
                0.0F);
        return hasRoom(level, seed) ? seed : null;
    }

    private static boolean isSproutingGround(ServerLevel level, BlockPos ground) {
        BlockState state = level.getBlockState(ground);
        return (state.is(TTBlocks.TAINT_SOIL) || state.is(TTBlocks.TAINT_ROCK))
                && level.getBlockState(ground.above()).isAir();
    }

    private static boolean hasRoom(ServerLevel level, AbstractTaintSeed seed) {
        AABB box = seed.getBoundingBox();
        boolean crowded = !level.getEntitiesOfClass(
                        AbstractTaintSeed.class, box.inflate(TaintOrigins.claimRadius() * CROWD_FRACTION))
                .isEmpty();
        return level.noBlockCollision(seed, box) && !crowded;
    }

    private static int offset(RandomSource random, int reach) {
        return random.nextInt(reach * 2 + 1) - reach;
    }
}
