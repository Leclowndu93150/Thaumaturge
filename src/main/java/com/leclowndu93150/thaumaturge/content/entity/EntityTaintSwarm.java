package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EntityTaintSwarm extends Monster implements ITaintedMob {
    private static final EntityDataAccessor<Boolean> SUMMONED = SynchedEntityData.defineId(EntityTaintSwarm.class, EntityDataSerializers.BOOLEAN);

    private static final int ATTACK_RANGE = 3;
    private static final int ATTACK_COOLDOWN = 25;
    private static final int WEAKNESS_DURATION = 100;
    private static final float SELF_DAMAGE_SUMMONED = 5.0F;
    private static final int SWARM_PARTICLES_PER_TICK = 3;
    private static final float SWARM_PARTICLE_SCALE = 0.22F;
    private static final float SWARM_PARTICLE_R = 0.7F;
    private static final float SWARM_PARTICLE_G = 0.0F;
    private static final float SWARM_PARTICLE_B = 1.0F;

    private int damBonus;
    private int attackTicks;

    public EntityTaintSwarm(EntityType<? extends EntityTaintSwarm> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, false);
        this.xpReward = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.FLYING_SPEED, 0.6).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        super.defineSynchedData(data);
        data.define(SUMMONED, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isSummoned() {
        return this.entityData.get(SUMMONED);
    }

    public void setSummoned(boolean summoned) {
        this.entityData.set(SUMMONED, summoned);
    }

    public void setDamBonus(int bonus) {
        this.damBonus = bonus;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (!(this.level() instanceof ServerLevel server)) {
            AABB box = this.getBoundingBox();
            for (int n = 0; n < SWARM_PARTICLES_PER_TICK; n++) {
                double x = box.minX + this.random.nextDouble() * (box.maxX - box.minX);
                double y = box.minY + this.random.nextDouble() * (box.maxY - box.minY);
                double z = box.minZ + this.random.nextDouble() * (box.maxZ - box.minZ);
                TaintFumeParticleOptions data = new TaintFumeParticleOptions(ARGB.colorFromFloat(1.0F, SWARM_PARTICLE_R, SWARM_PARTICLE_G, SWARM_PARTICLE_B), SWARM_PARTICLE_SCALE);
                this.level().addParticle(data, x, y, z, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
            }
            return;
        }
        if (isSummoned()) {
            this.hurtServer(server, server.damageSources().generic(), SELF_DAMAGE_SUMMONED);
            return;
        }
        if (attackTicks > 0) {
            attackTicks--;
        }
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }
        if (attackTicks > 0) {
            return;
        }
        double distSq = this.distanceToSqr(target);
        if (distSq < ATTACK_RANGE * ATTACK_RANGE) {
            attackTicks = ATTACK_COOLDOWN + this.random.nextInt(10);
            this.doHurtTarget(server, target);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 0, true, false, false));
        }
    }

    public float effectiveAttackDamage() {
        return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + damBonus;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Summoned", isSummoned());
        output.putInt("DamBonus", damBonus);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setSummoned(input.getBooleanOr("Summoned", false));
        damBonus = input.getIntOr("DamBonus", 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.SWARM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.SWARMATTACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.SWARMATTACK.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }
}
