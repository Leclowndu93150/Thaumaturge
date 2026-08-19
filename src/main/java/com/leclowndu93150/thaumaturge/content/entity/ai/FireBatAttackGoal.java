package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityFireBat;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public final class FireBatAttackGoal extends Goal {
    private static final float TARGET_HEIGHT_FACTOR = 0.66F;
    private static final float MIN_ATTACK_REACH = 2.5F;
    private static final float REACH_WIDTH_FACTOR = 1.1F;
    private static final int COOLDOWN_BASE = 20;
    private static final int COOLDOWN_SPREAD = 20;
    private static final float EXPLODE_ONE_IN = 10.0F;
    private static final float EXPLOSION_POWER = 1.5F;

    private final EntityFireBat bat;
    private int attackCooldown;

    public FireBatAttackGoal(EntityFireBat bat) {
        this.bat = bat;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return bat.getTarget() != null && !bat.isHanging();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = bat.getTarget();
        if (target == null) {
            return;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        bat.getMoveControl().setWantedPosition(target.getX(), target.getY() + target.getEyeHeight() * TARGET_HEIGHT_FACTOR, target.getZ(), 1.0);
        if (!bat.hasLineOfSight(target)) {
            return;
        }
        float reach = Math.max(MIN_ATTACK_REACH, target.getBbWidth() * REACH_WIDTH_FACTOR);
        boolean verticalOverlap = target.getBoundingBox().maxY > bat.getBoundingBox().minY && target.getBoundingBox().minY < bat.getBoundingBox().maxY;
        if (attackCooldown <= 0 && bat.distanceTo(target) < reach && verticalOverlap) {
            attack(target);
        }
    }

    private void attack(LivingEntity target) {
        attackCooldown = COOLDOWN_BASE + bat.getRandom().nextInt(COOLDOWN_SPREAD);
        if (bat.level() instanceof ServerLevel server) {
            if (bat.getRandom().nextInt((int) EXPLODE_ONE_IN) == 0) {
                target.invulnerableTime = 0;
                server.explode(bat, bat.getX(), bat.getY(), bat.getZ(), EXPLOSION_POWER, false, Level.ExplosionInteraction.NONE);
                bat.discard();
                return;
            }
            bat.doHurtTarget(server, target);
        }
        bat.playSound(SoundEvents.BAT_HURT, 0.5F, 0.9F + bat.getRandom().nextFloat() * 0.2F);
    }
}
