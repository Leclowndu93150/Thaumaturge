package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.level.Level;

public final class EntityGiantBrainyZombie extends EntityBrainyZombie {
    private static final EntityDataAccessor<Float> DATA_ANGER = SynchedEntityData.defineId(EntityGiantBrainyZombie.class, EntityDataSerializers.FLOAT);

    private static final int XP_REWARD = 15;
    private static final float LEAP_STRENGTH = 0.4F;
    private static final float MAX_ANGER = 2.0F;
    private static final float ANGER_PER_HIT = 0.1F;
    private static final float ANGER_DECAY = 0.002F;
    private static final double BASE_ATTACK = 7.0;
    private static final double ANGER_ATTACK_BONUS = 5.0;

    public EntityGiantBrainyZombie(EntityType<? extends EntityGiantBrainyZombie> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityBrainyZombie.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, BASE_ATTACK);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, LEAP_STRENGTH));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_ANGER, 0.0F);
    }

    public float getAnger() {
        return this.entityData.get(DATA_ANGER);
    }

    public void setAnger(float anger) {
        this.entityData.set(DATA_ANGER, anger);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        float anger = getAnger();
        if (anger > 1.0F) {
            setAnger(anger - ANGER_DECAY);
        }
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(1.0 + getAnger());
        }
        AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(BASE_ATTACK + (getAnger() - 1.0F) * ANGER_ATTACK_BONUS);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        setAnger(Math.min(MAX_ANGER, getAnger() + ANGER_PER_HIT));
        return super.hurtServer(level, source, damage);
    }
}
