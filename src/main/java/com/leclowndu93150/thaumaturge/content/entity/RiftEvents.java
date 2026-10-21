package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintOrigins;
import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class RiftEvents {
    private static final int WISP_WEIGHT = 50;
    private static final int SEED_WEIGHT = 10;
    private static final int PHAGE_WEIGHT = 20;
    private static final int COLLAPSE_WEIGHT = 1;
    private static final float WISP_CALMING = 5.0F;
    private static final float PHAGE_CALMING = 10.0F;
    private static final float NO_CALMING = 0.0F;
    private static final double SCATTER_DEVIATION = 5.0;
    private static final int FLUX_WISP_ONE_IN = 5;
    private static final double BLOCK_CENTRE = 0.5;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final double SEED_CROWD_FRACTION = 0.8;
    private static final float SEED_FLUX_SHARE = 0.5F;
    private static final double PHAGE_REACH = 16.0;
    private static final int PHAGE_DURATION = 3000;
    private static final int PHAGE_AMPLIFIER = 2;
    private static final String PHAGE_NOTICE = "warp.thaumaturge.fluxevent.2";
    private static final List<RiftEvent> EVENTS = List.of(
            new RiftEvent(WISP_WEIGHT, WISP_CALMING, false, RiftEvents::releaseWisp),
            new RiftEvent(SEED_WEIGHT, NO_CALMING, true, RiftEvents::sowSeed),
            new RiftEvent(PHAGE_WEIGHT, PHAGE_CALMING, false, RiftEvents::spreadPhage),
            new RiftEvent(COLLAPSE_WEIGHT, NO_CALMING, false, RiftEvents::collapse));
    private static final int TOTAL_WEIGHT =
            EVENTS.stream().mapToInt(RiftEvent::weight).sum();

    private RiftEvents() {}

    static void roll(ServerLevel level, EntityFluxRift rift) {
        RiftEvent event = choose(rift.getRandom());
        if (event.blockedNearSeed() && TaintApi.isNearTaintSeed(level, rift.blockPosition())) {
            return;
        }
        if (event.outcome().fire(level, rift) && !rift.isRemoved()) {
            rift.adjustStability(event.calming());
        }
    }

    private static RiftEvent choose(RandomSource random) {
        int ticket = random.nextInt(TOTAL_WEIGHT);
        for (RiftEvent event : EVENTS) {
            ticket -= event.weight();
            if (ticket < 0) {
                return event;
            }
        }
        return EVENTS.get(EVENTS.size() - 1);
    }

    private static Vec3 scatter(EntityFluxRift rift) {
        RandomSource random = rift.getRandom();
        return rift.position()
                .add(
                        random.nextGaussian() * SCATTER_DEVIATION,
                        random.nextGaussian() * SCATTER_DEVIATION,
                        random.nextGaussian() * SCATTER_DEVIATION);
    }

    private static boolean releaseWisp(ServerLevel level, EntityFluxRift rift) {
        Vec3 spot = scatter(rift);
        if (!level.hasChunkAt(BlockPos.containing(spot))) {
            return false;
        }
        WispEntity wisp = TTEntities.WISP.get().create(level);
        if (wisp == null) {
            return false;
        }
        wisp.moveTo(spot.x, spot.y, spot.z, 0.0F, 0.0F);
        if (!level.noCollision(wisp)) {
            wisp.discard();
            return false;
        }
        if (rift.getRandom().nextInt(FLUX_WISP_ONE_IN) == 0) {
            wisp.setAspect(TTAspects.VITIUM.location());
        }
        return level.addFreshEntity(wisp);
    }

    private static boolean sowSeed(ServerLevel level, EntityFluxRift rift) {
        BlockPos cell = BlockPos.containing(scatter(rift));
        if (level.getDifficulty() == Difficulty.PEACEFUL || !level.hasChunkAt(cell)) {
            return false;
        }
        EntityTaintSeedPrime seed = TTEntities.TAINT_SEED_PRIME.get().create(level);
        if (seed == null) {
            return false;
        }
        seed.moveTo(
                cell.getX() + BLOCK_CENTRE,
                cell.getY(),
                cell.getZ() + BLOCK_CENTRE,
                rift.getRandom().nextFloat() * FULL_TURN_DEGREES,
                0.0F);
        if (!seedFits(level, rift, seed) || !level.addFreshEntity(seed)) {
            seed.discard();
            return false;
        }
        AuraHelper.polluteAura(level, rift.blockPosition(), rift.currentSize() * SEED_FLUX_SHARE, true);
        rift.discard();
        return true;
    }

    private static boolean seedFits(ServerLevel level, EntityFluxRift rift, EntityTaintSeedPrime seed) {
        AABB body = seed.getBoundingBox();
        if (level.containsAnyLiquid(body) || !level.noBlockCollision(seed, body)) {
            return false;
        }
        for (Entity entity : level.getEntities(seed, body)) {
            if (entity != rift) {
                return false;
            }
        }
        AABB crowd = body.inflate(TaintOrigins.claimRadius() * SEED_CROWD_FRACTION);
        return level.getEntitiesOfClass(AbstractTaintSeed.class, crowd).isEmpty();
    }

    private static boolean spreadPhage(ServerLevel level, EntityFluxRift rift) {
        List<LivingEntity> victims = level.getEntitiesOfClass(
                LivingEntity.class, rift.getBoundingBox().inflate(PHAGE_REACH), LivingEntity::isAlive);
        for (LivingEntity victim : victims) {
            victim.addEffect(
                    new MobEffectInstance(TTMobEffects.INFECTIOUS_VIS_EXHAUST, PHAGE_DURATION, PHAGE_AMPLIFIER));
            if (victim instanceof ServerPlayer player) {
                WarpManager.sendActionBar(player, PHAGE_NOTICE);
            }
        }
        return !victims.isEmpty();
    }

    private static boolean collapse(ServerLevel level, EntityFluxRift rift) {
        if (rift.isCollapsing()) {
            return false;
        }
        rift.beginCollapse();
        return true;
    }

    @FunctionalInterface
    private interface Outcome {
        boolean fire(ServerLevel level, EntityFluxRift rift);
    }

    private record RiftEvent(int weight, float calming, boolean blockedNearSeed, Outcome outcome) {}
}
