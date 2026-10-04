package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

final class GolemBeamCore {
    private static final int FULL = 150;
    private static final int SHOT_COST = 15;
    private static final int SHOT_COST_SPREAD = 5;
    private static final int PULSE_INTERVAL = 5;
    private static final float ARC_REACH = 2.0F;
    private static final float ARC_REACH_SPREAD = 2.0F;
    private static final int ARC_SINK = 5;
    private static final int ARC_PULSES = 8;
    private static final int ARC_PULSE_SPREAD = 5;
    private static final int ARC_COLOR = 0xA6FFFF;
    private static final float HUM_VOLUME = 0.8F;
    private static final float HUM_WOBBLE = 0.05F;

    private final Mob golem;
    private int charge;
    private boolean recharging;
    private BlockPos arcFoot = BlockPos.ZERO;
    private int arcPulses;

    GolemBeamCore(Mob golem) {
        this.golem = golem;
    }

    boolean ready() {
        return !recharging && charge > 0;
    }

    void spend(RandomSource random) {
        charge -= SHOT_COST + random.nextInt(SHOT_COST_SPREAD);
    }

    void tick(ServerLevel level) {
        recharging |= charge <= 0;
        if (!recharging) {
            return;
        }
        charge++;
        if (golem.tickCount % PULSE_INTERVAL == 0) {
            pulse(level);
        }
        recharging = charge != FULL;
    }

    private void pulse(ServerLevel level) {
        if (arcPulses > 0) {
            arcPulses--;
            Effects.arcLightning(level, golem.position().add(0.0, golem.getBbHeight() / 2.0, 0.0)).to(Vec3.atBottomCenterOf(arcFoot.above())).color(ARC_COLOR).send();
            return;
        }
        RandomSource random = golem.getRandom();
        float reach = ARC_REACH + random.nextFloat() * ARC_REACH_SPREAD;
        double bearing = Math.toRadians(random.nextInt(360));
        BlockPos.MutableBlockPos spot = BlockPos.containing(golem.getX() + reach * Math.cos(bearing), golem.getY(), golem.getZ() + reach * Math.sin(bearing)).mutable();
        for (int depth = 0; depth < ARC_SINK && level.isEmptyBlock(spot); depth++) {
            spot.move(Direction.DOWN);
        }
        if (!level.isEmptyBlock(spot) && level.isEmptyBlock(spot.above())) {
            arcFoot = spot.immutable();
            arcPulses = ARC_PULSES + random.nextInt(ARC_PULSE_SPREAD);
            golem.playSound(TCSounds.JACOBS.get(), HUM_VOLUME, 1.0F + (random.nextFloat() - random.nextFloat()) * HUM_WOBBLE);
        }
    }
}
