package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.time.Month;
import java.time.MonthDay;
import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public final class CultistSquatGoal extends Goal {
    private static final MonthDay APRIL_FOOLS = MonthDay.of(Month.APRIL, 1);
    private static final String NO_WITNESS = "";
    private static final int SQUAT_ONE_IN = 1200;
    private static final int SQUAT_TICKS = 30;
    private static final int RELEASE_TICK = 20;
    private static final int SPIDER_CAP = 4;
    private static final double CAP_RANGE = 8.0;
    private static final double REAR_OFFSET = 0.45;
    private static final double HIP_HEIGHT = 0.55;
    private static final double PUSH = 0.18;
    private static final double LIFT = 0.08;
    private static final int PARTICLES = 8;
    private static final double PARTICLE_SPREAD = 0.1;
    private static final double PARTICLE_SPEED = 0.05;
    private static final float SQUISH_VOLUME = 1.0F;
    private static final float SQUISH_PITCH = 0.5F;

    private final Mob cultist;
    private int squatTicks;

    public CultistSquatGoal(Mob cultist) {
        this.cultist = cultist;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return cultist.getTarget() == null
                && cultist.getRandom().nextInt(SQUAT_ONE_IN) == 0
                && ThaumaturgeServerConfig.APRIL_FOOLS.get()
                && MonthDay.now().equals(APRIL_FOOLS)
                && cultist.level() instanceof ServerLevel level
                && nearbySpiders(level) < SPIDER_CAP;
    }

    @Override
    public boolean canContinueToUse() {
        return squatTicks < SQUAT_TICKS && cultist.getTarget() == null;
    }

    @Override
    public void start() {
        squatTicks = 0;
        cultist.getNavigation().stop();
        cultist.setPose(Pose.CROUCHING);
    }

    @Override
    public void stop() {
        cultist.setPose(Pose.STANDING);
    }

    @Override
    public void tick() {
        squatTicks++;
        if (squatTicks == RELEASE_TICK && cultist.level() instanceof ServerLevel level) {
            release(level);
        }
    }

    private int nearbySpiders(ServerLevel level) {
        return level.getEntitiesOfClass(
                        EntityMindSpider.class,
                        cultist.getBoundingBox().inflate(CAP_RANGE),
                        EntityMindSpider::isHarmless)
                .size();
    }

    private void release(ServerLevel level) {
        Vec3 back = Vec3.directionFromRotation(0.0F, cultist.yBodyRot).scale(-1.0);
        Vec3 at = cultist.position().add(back.scale(REAR_OFFSET)).add(0.0, HIP_HEIGHT, 0.0);
        EntityMindSpider spider = TTEntities.MIND_SPIDER.get().create(level);
        if (spider == null) {
            return;
        }
        spider.setViewer(NO_WITNESS);
        spider.setHarmless(true);
        spider.moveTo(at.x, at.y, at.z, cultist.yBodyRot + 180.0F, 0.0F);
        spider.setDeltaMovement(back.scale(PUSH).add(0.0, LIFT, 0.0));
        level.addFreshEntity(spider);
        level.sendParticles(
                ParticleTypes.ITEM_SLIME,
                at.x,
                at.y,
                at.z,
                PARTICLES,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                PARTICLE_SPREAD,
                PARTICLE_SPEED);
        level.playSound(
                null,
                cultist.blockPosition(),
                SoundEvents.SLIME_SQUISH_SMALL,
                SoundSource.HOSTILE,
                SQUISH_VOLUME,
                SQUISH_PITCH);
    }
}
