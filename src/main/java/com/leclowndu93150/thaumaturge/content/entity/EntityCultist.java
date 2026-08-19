package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public abstract class EntityCultist extends Monster {
    public static final byte ARRIVAL_EVENT = 20;
    private static final float GEAR_DROP_CHANCE = 0.05F;
    private static final int ARRIVAL_PARTICLES = 20;

    protected EntityCultist(EntityType<? extends EntityCultist> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.setDropChance(EquipmentSlot.CHEST, GEAR_DROP_CHANCE);
        this.setDropChance(EquipmentSlot.FEET, GEAR_DROP_CHANCE);
        this.setDropChance(EquipmentSlot.HEAD, GEAR_DROP_CHANCE);
        this.setDropChance(EquipmentSlot.LEGS, GEAR_DROP_CHANCE);
    }

    public static AttributeSupplier.Builder createCultistAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    protected void setLoot(DifficultyInstance difficulty) {}

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {}

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        this.setLoot(difficulty);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return entity instanceof EntityCultist || super.considersEntityAsAlly(entity);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof EntityCultist) && super.canAttack(target);
    }

    public void spawnCultistArrivalParticles() {
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, ARRIVAL_EVENT);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == ARRIVAL_EVENT) {
            for (int i = 0; i < ARRIVAL_PARTICLES; i++) {
                double vx = this.random.nextGaussian() * 0.05;
                double vy = this.random.nextGaussian() * 0.05;
                double vz = this.random.nextGaussian() * 0.05;
                this.level().addParticle(ParticleTypes.POOF, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth() + vx * 2.0,
                        this.getY() + this.random.nextFloat() * this.getBbHeight() + vy * 2.0, this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth() + vz * 2.0, vx, vy,
                        vz);
            }
        } else {
            super.handleEntityEvent(event);
        }
    }
}
