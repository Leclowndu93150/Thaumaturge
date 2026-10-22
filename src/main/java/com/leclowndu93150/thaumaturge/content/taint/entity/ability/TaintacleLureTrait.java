package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class TaintacleLureTrait implements MobTrait {
    private static final float LURE_CHANCE = 0.25F;
    private static final double SEARCH_RANGE = 32.0;
    private static final int LANDING_ATTEMPTS = 8;
    private static final int LANDING_SPREAD = 2;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    @Override
    public float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (!(target instanceof Player)
                || !(mob.level() instanceof ServerLevel level)
                || mob.getRandom().nextFloat() >= LURE_CHANCE) {
            return amount;
        }
        nearestTaintacle(level, mob).ifPresent(taintacle -> dragTo(level, target, taintacle));
        return amount;
    }

    private static Optional<AbstractTaintacle> nearestTaintacle(ServerLevel level, LivingEntity mob) {
        return level
                .getEntitiesOfClass(
                        AbstractTaintacle.class, mob.getBoundingBox().inflate(SEARCH_RANGE), LivingEntity::isAlive)
                .stream()
                .min(Comparator.comparingDouble(mob::distanceToSqr));
    }

    private static void dragTo(ServerLevel level, LivingEntity target, AbstractTaintacle taintacle) {
        double fromX = target.getX();
        double fromY = target.getY();
        double fromZ = target.getZ();
        RandomSource random = target.getRandom();
        for (int attempt = 0; attempt < LANDING_ATTEMPTS; attempt++) {
            double x = taintacle.getX() + random.nextInt(LANDING_SPREAD * 2 + 1) - LANDING_SPREAD;
            double z = taintacle.getZ() + random.nextInt(LANDING_SPREAD * 2 + 1) - LANDING_SPREAD;
            if (target.randomTeleport(x, taintacle.getY(), z, true)) {
                level.playSound(
                        null,
                        fromX,
                        fromY,
                        fromZ,
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.HOSTILE,
                        SOUND_VOLUME,
                        SOUND_PITCH);
                level.playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.HOSTILE,
                        SOUND_VOLUME,
                        SOUND_PITCH);
                return;
            }
        }
    }
}
