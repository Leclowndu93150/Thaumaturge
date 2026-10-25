package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

final class RiftEvents {
    private static final double WISP_SPREAD = 5.0;
    private static final int WISP_VITIUM_ODDS = 5;
    private static final double SEED_SPREAD = 5.0;
    private static final double SEED_CENTER = 0.5;
    private static final int FULL_TURN_DEGREES = 360;
    private static final double PHAGE_RADIUS = 16.0;
    private static final int PHAGE_DURATION = 3000;
    private static final int PHAGE_AMPLIFIER = 2;
    private static final int WISP_WEIGHT = 50;
    private static final int SEED_WEIGHT = 10;
    private static final int PHAGE_WEIGHT = 20;
    private static final int COLLAPSE_WEIGHT = 1;
    private static final float WISP_STABILITY_GAIN = 5.0F;
    private static final float PHAGE_STABILITY_GAIN = 10.0F;
    private static final String NOTICE_MIND = "warp.thaumaturge.fluxevent.2";

    private static final List<EventSpec> TABLE = List.of(
            new EventSpec(WISP_WEIGHT, WISP_STABILITY_GAIN, true, RiftEvents::wisp),
            new EventSpec(SEED_WEIGHT, 0.0F, false, RiftEvents::taintSeed),
            new EventSpec(PHAGE_WEIGHT, PHAGE_STABILITY_GAIN, true, RiftEvents::phage),
            new EventSpec(COLLAPSE_WEIGHT, 0.0F, true, RiftEvents::collapse));

    private RiftEvents() {}

    static void roll(ServerLevel level, EntityFluxRift rift) {
        int total = 0;
        for (EventSpec event : TABLE) {
            total += event.weight();
        }
        int roll = rift.getRandom().nextInt(total);
        for (EventSpec event : TABLE) {
            roll -= event.weight();
            if (roll < 0) {
                run(level, rift, event);
                return;
            }
        }
    }

    private static void run(ServerLevel level, EntityFluxRift rift, EventSpec event) {
        if (!event.allowedNearTaint() && TaintApi.isNearTaintSeed(level, rift.blockPosition())) {
            return;
        }
        if (event.action().run(level, rift)) {
            rift.adjustStability(event.stabilityGain());
        }
    }

    private static boolean wisp(ServerLevel level, EntityFluxRift rift) {
        WispEntity wisp = TTEntities.WISP.get().create(level);
        if (wisp == null) {
            return false;
        }
        RandomSource random = rift.getRandom();
        wisp.moveTo(
                rift.getX() + random.nextGaussian() * WISP_SPREAD,
                rift.getY() + random.nextGaussian() * WISP_SPREAD,
                rift.getZ() + random.nextGaussian() * WISP_SPREAD,
                0.0F,
                0.0F);
        if (!level.hasChunkAt(wisp.blockPosition())) {
            wisp.discard();
            return false;
        }
        if (random.nextInt(WISP_VITIUM_ODDS) == 0) {
            wisp.setAspect(TTAspects.VITIUM.location());
        }
        if (!level.noCollision(wisp)) {
            wisp.discard();
            return false;
        }
        return level.addFreshEntity(wisp);
    }

    private static boolean taintSeed(ServerLevel level, EntityFluxRift rift) {
        EntityTaintSeedPrime seed = TTEntities.TAINT_SEED_PRIME.get().create(level);
        if (seed == null) {
            return false;
        }
        RandomSource random = rift.getRandom();
        seed.moveTo(
                (int) (rift.getX() + random.nextGaussian() * SEED_SPREAD) + SEED_CENTER,
                (int) (rift.getY() + random.nextGaussian() * SEED_SPREAD),
                (int) (rift.getZ() + random.nextGaussian() * SEED_SPREAD) + SEED_CENTER,
                random.nextInt(FULL_TURN_DEGREES),
                0.0F);
        if (!level.hasChunkAt(seed.blockPosition())) {
            seed.discard();
            return false;
        }
        if (!level.noCollision(seed) || !level.addFreshEntity(seed)) {
            seed.discard();
            return false;
        }
        AuraHelper.polluteAura(level, rift.blockPosition(), rift.currentSize() / 2.0F, true);
        rift.discard();
        return true;
    }

    private static boolean phage(ServerLevel level, EntityFluxRift rift) {
        boolean affected = false;
        for (LivingEntity living : level.getEntitiesOfClass(
                LivingEntity.class, rift.getBoundingBox().inflate(PHAGE_RADIUS))) {
            living.addEffect(
                    new MobEffectInstance(TTMobEffects.INFECTIOUS_VIS_EXHAUST, PHAGE_DURATION, PHAGE_AMPLIFIER));
            if (living instanceof ServerPlayer player) {
                WarpManager.sendActionBar(player, NOTICE_MIND);
            }
            affected = true;
        }
        return affected;
    }

    private static boolean collapse(ServerLevel level, EntityFluxRift rift) {
        if (rift.isCollapsing()) {
            return false;
        }
        rift.beginCollapse();
        return false;
    }

    private interface RiftEventAction {
        boolean run(ServerLevel level, EntityFluxRift rift);
    }

    private record EventSpec(int weight, float stabilityGain, boolean allowedNearTaint, RiftEventAction action) {}
}
