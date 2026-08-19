package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.leclowndu93150.thaumaturge.network.ClientboundWispZapPayload;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WispZapGoal extends Goal {
    private static final double ATTACK_RANGE_SQ = 16.0 * 16.0;
    private static final double CHASE_DISTANCE_SQ = ATTACK_RANGE_SQ / 2.0;
    private static final float TARGET_HEIGHT_FACTOR = 0.66F;
    private static final int ZAP_CHARGE_TICKS = 20;
    private static final int ZAP_RESET_BASE = -20;
    private static final int ZAP_RESET_SPREAD = 20;
    private static final float ZAP_PITCH = 1.1F;
    private static final double STATIONARY_SPEED = 0.1;
    private static final float STATIONARY_HIT_CHANCE = 0.66F;
    private static final float MOVING_HIT_CHANCE = 0.4F;

    private final WispEntity wisp;
    private int attackCounter;

    public WispZapGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return wisp.getTarget() != null;
    }

    @Override
    public void start() {
        attackCounter = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = wisp.getTarget();
        if (target == null) {
            return;
        }
        double distSq = wisp.distanceToSqr(target);
        if (distSq > CHASE_DISTANCE_SQ && wisp.hasLineOfSight(target)) {
            wisp.getMoveControl().setWantedPosition(target.getX(), target.getY() + target.getEyeHeight() * TARGET_HEIGHT_FACTOR, target.getZ(), 1.0);
        }
        if (distSq < ATTACK_RANGE_SQ && wisp.hasLineOfSight(target)) {
            attackCounter++;
            if (attackCounter == ZAP_CHARGE_TICKS) {
                zap(target);
                attackCounter = ZAP_RESET_BASE + wisp.getRandom().nextInt(ZAP_RESET_SPREAD);
            }
        } else if (attackCounter > 0) {
            attackCounter--;
        }
    }

    private void zap(LivingEntity target) {
        wisp.playSound(TCSounds.ZAP.get(), 1.0F, ZAP_PITCH);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(wisp, new ClientboundWispZapPayload(wisp.getId(), target.getId()));
        if (!(wisp.level() instanceof ServerLevel server)) {
            return;
        }
        float damage = (float) wisp.getAttributeValue(Attributes.ATTACK_DAMAGE);
        Vec3 motion = target.getDeltaMovement();
        boolean stationary = Math.abs(motion.x) <= STATIONARY_SPEED && Math.abs(motion.y) <= STATIONARY_SPEED && Math.abs(motion.z) <= STATIONARY_SPEED;
        DamageSource source = wisp.damageSources().mobAttack(wisp);
        if (stationary) {
            if (wisp.getRandom().nextFloat() < STATIONARY_HIT_CHANCE) {
                target.hurtServer(server, source, damage + 1.0F);
            }
        } else if (wisp.getRandom().nextFloat() < MOVING_HIT_CHANCE) {
            target.hurtServer(server, source, damage);
        }
    }
}
