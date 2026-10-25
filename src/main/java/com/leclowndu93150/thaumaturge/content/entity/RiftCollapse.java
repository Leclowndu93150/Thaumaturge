package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

final class RiftCollapse {
    private static final float RELEASE_AMOUNT = 1.0F;
    private static final int EXPLOSION_ODDS = 10;
    private static final double EXPLOSION_DEVIATION = 2.0;
    private static final float EXPLOSION_POWER = 0.5F;
    private static final int FINAL_SIZE = 1;
    private static final double PEARL_PERCENT = 100.0;
    private static final int PEARL_DAMAGE_MIN = 4;
    private static final int PEARL_DAMAGE_SPREAD = 4;
    private static final double EFFECT_RADIUS = 32.0;
    private static final double EFFECT_RADIUS_SQR = EFFECT_RADIUS * EFFECT_RADIUS;
    private static final int TICKS_PER_SECOND = 20;

    boolean tick(ServerLevel level, EntityFluxRift rift) {
        RandomSource random = rift.getRandom();
        shrink(rift);
        release(level, rift, random);
        detonate(level, rift, random);
        boolean done = rift.currentSize() <= FINAL_SIZE;
        if (done) {
            finish(level, rift, random);
        }
        return done;
    }

    private void shrink(EntityFluxRift rift) {
        rift.resize(rift.currentSize() - 1);
    }

    private void release(ServerLevel level, EntityFluxRift rift, RandomSource random) {
        BlockPos here = rift.blockPosition();
        if (random.nextBoolean()) {
            AuraHelper.addVis(level, here, RELEASE_AMOUNT);
        } else {
            AuraHelper.polluteAura(level, here, RELEASE_AMOUNT, false);
        }
    }

    private void detonate(ServerLevel level, EntityFluxRift rift, RandomSource random) {
        if (random.nextInt(EXPLOSION_ODDS) != 0) {
            return;
        }
        double dx = random.nextGaussian() * EXPLOSION_DEVIATION;
        double dy = random.nextGaussian() * EXPLOSION_DEVIATION;
        double dz = random.nextGaussian() * EXPLOSION_DEVIATION;
        float power = random.nextFloat() * EXPLOSION_POWER;
        level.explode(
                rift, rift.getX() + dx, rift.getY() + dy, rift.getZ() + dz, power, Level.ExplosionInteraction.NONE);
    }

    private void finish(ServerLevel level, EntityFluxRift rift, RandomSource random) {
        int strength = (int) Math.sqrt(rift.collapseSize());
        applyEffects(level, rift, RiftCollapseProfiles.forTier(rift.stabilityTier()));
        dropLoot(level, rift, random, strength);
        level.explode(rift, rift.getX(), rift.getY(), rift.getZ(), 0.0F, Level.ExplosionInteraction.NONE);
        rift.discard();
    }

    private void dropLoot(ServerLevel level, EntityFluxRift rift, RandomSource random, int strength) {
        if (random.nextDouble() * PEARL_PERCENT < strength) {
            int wear = random.nextInt(PEARL_DAMAGE_SPREAD) + PEARL_DAMAGE_MIN;
            ItemStack pearl = new ItemStack(TTItems.PRIMORDIAL_PEARL.get());
            pearl.setDamageValue(wear);
            rift.spawnAtLocation(pearl);
        }
        for (int i = 0; i < strength; i++) {
            rift.spawnAtLocation(new ItemStack(TTItems.VOID_SEED.get()));
        }
    }

    private void applyEffects(ServerLevel level, EntityFluxRift rift, RiftCollapseProfile profile) {
        if (profile.isInert()) {
            return;
        }
        AABB reach = rift.getBoundingBox().inflate(EFFECT_RADIUS);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, reach)) {
            float factor = 1.0F - (float) (living.distanceToSqr(rift) / EFFECT_RADIUS_SQR);
            inflict(living, TTMobEffects.FLUX_TAINT, scaledAmount(factor, profile.taintSeconds()));
            inflict(living, MobEffects.WEAKNESS, scaledAmount(factor, profile.weaknessSeconds()));
            if (living instanceof ServerPlayer player) {
                int warp = scaledAmount(factor, profile.warpPoints());
                if (warp > 0) {
                    WarpHelper.addWarp(player, warp, WarpType.NORMAL);
                    WarpHelper.addWarp(player, warp, WarpType.TEMPORARY);
                }
            }
        }
    }

    private static int scaledAmount(float factor, float maximum) {
        return Mth.floor(factor * maximum);
    }

    private static void inflict(LivingEntity target, Holder<MobEffect> effect, int seconds) {
        if (seconds > 0) {
            target.addEffect(new MobEffectInstance(effect, seconds * TICKS_PER_SECOND, 0));
        }
    }
}
