package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;

public final class TaintacleAmbush {
    private static final float FOOT_SHIFT = 1.0F;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH_BASE = 0.9F;
    private static final float SOUND_PITCH_SPREAD = 0.2F;

    private TaintacleAmbush() {}

    public static boolean tryAmbush(ServerLevel level, Mob hunter, double minReach, double maxReach) {
        LivingEntity prey = hunter.getTarget();
        if (prey == null || !prey.isAlive() || !inReach(hunter, prey, minReach, maxReach) || !hunter.getSensing().hasLineOfSight(prey)) {
            return false;
        }
        boolean eldritch = level.getBiome(prey.blockPosition()).is(TTBiomes.ELDRITCH);
        if (!eldritch && !standsInTaint(level, prey)) {
            return false;
        }
        boolean raised = raiseUnder(level, prey, hunter.getRandom());
        if (eldritch) {
            rootFibre(level, prey.blockPosition());
        }
        return raised;
    }

    private static boolean inReach(Mob hunter, LivingEntity prey, double minReach, double maxReach) {
        double distanceSqr = hunter.distanceToSqr(prey);
        return distanceSqr > minReach * minReach && distanceSqr < maxReach * maxReach;
    }

    private static boolean standsInTaint(ServerLevel level, LivingEntity prey) {
        return TaintHelper.isTaintBlock(level.getBlockState(prey.blockPosition())) || TaintHelper.isTaintBlock(level.getBlockState(prey.getOnPos()));
    }

    private static boolean raiseUnder(ServerLevel level, LivingEntity prey, RandomSource random) {
        EntityTaintacleSmall tentacle = TTEntities.TAINTACLE_SMALL.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (tentacle == null) {
            return false;
        }
        double x = prey.getX() + footShift(random);
        double z = prey.getZ() + footShift(random);
        tentacle.snapTo(x, prey.getY(), z, random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
        if (!level.noBlockCollision(tentacle, tentacle.getBoundingBox()) || !level.addFreshEntity(tentacle)) {
            tentacle.discard();
            return false;
        }
        float pitch = SOUND_PITCH_BASE + random.nextFloat() * SOUND_PITCH_SPREAD;
        level.playSound(null, x, prey.getY(), z, TTSounds.TENTACLE.get(), SoundSource.HOSTILE, SOUND_VOLUME, pitch);
        return true;
    }

    private static void rootFibre(ServerLevel level, BlockPos pos) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || !level.getBlockState(pos).isAir() || !TaintHelper.hasSturdyNeighbour(level, pos)) {
            return;
        }
        level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
    }

    private static double footShift(RandomSource random) {
        return (random.nextFloat() - random.nextFloat()) * FOOT_SHIFT;
    }
}
