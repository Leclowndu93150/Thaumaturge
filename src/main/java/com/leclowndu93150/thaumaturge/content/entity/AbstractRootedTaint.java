package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractRootedTaint extends Monster {
    private static final byte EVENT_STRIKE = 16;
    private static final int STRIKE_TICKS = 20;
    private static final int MELEE_PRIORITY = 2;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int HUNT_PRIORITY = 2;
    private static final double REST_HORIZONTAL_EPSILON = 1.0E-6;
    private static final double SUPPORT_PROBE_DEPTH = 0.01;

    private int strikeTicks;

    protected AbstractRootedTaint(EntityType<? extends AbstractRootedTaint> type, Level level) {
        super(type, level);
    }

    protected static AttributeSupplier.Builder createRootedAttributes(
            double maxHealth, double attackDamage, double followRange) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, followRange)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    protected abstract void startStrikeAnimation();

    protected abstract void tickStrikeAnimation();

    protected void onStrike(ServerLevel level) {}

    protected void rootedServerStep(ServerLevel level) {}

    public int strikeTicks() {
        return this.strikeTicks;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(HUNT_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(level() instanceof ServerLevel level)) return false;
        level.broadcastEntityEvent(this, EVENT_STRIKE);
        onStrike(level);
        return super.doHurtTarget(target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_STRIKE) {
            this.strikeTicks = STRIKE_TICKS;
            startStrikeAnimation();
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel server) {
            rootedServerStep(server);
            return;
        }
        if (this.strikeTicks > 0) {
            this.strikeTicks--;
        }
        tickStrikeAnimation();
    }

    @Override
    public void travel(Vec3 input) {
        if (!isRestingOnSupport()) {
            super.travel(input);
        }
    }

    private boolean isRestingOnSupport() {
        Vec3 motion = this.getDeltaMovement();
        if (!this.onGround() || motion.y > 0.0 || motion.x * motion.x + motion.z * motion.z > REST_HORIZONTAL_EPSILON) {
            return false;
        }
        BlockPos below = BlockPos.containing(this.getX(), this.getY() - SUPPORT_PROBE_DEPTH, this.getZ());
        return !this.level()
                .getBlockState(below)
                .getCollisionShape(this.level(), below)
                .isEmpty();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {}

    @Override
    public void push(Entity entity) {}

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.GORE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.TENTACLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.TENTACLE.get();
    }
}
