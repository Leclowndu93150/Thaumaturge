package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class EntityEldritchCrab extends Monster implements IEldritchMob {
    private static final EntityDataAccessor<Boolean> DATA_HELM = SynchedEntityData.defineId(EntityEldritchCrab.class, EntityDataSerializers.BOOLEAN);

    private static final double HELM_SPEED = 0.275;
    private static final double BARE_SPEED = 0.3;
    private static final double HELM_ARMOR = 5.0;
    private static final float HELM_CHANCE = 0.33F;
    private static final float LEAP_STRENGTH = 0.63F;
    private static final double LATCH_RANGE_SQ = 4.0;
    private static final float DISMOUNT_CHANCE = 0.2F;
    private static final float HELM_BREAK_HEALTH_FRACTION = 0.5F;
    private static final float HARD_GROUP_EFFECT_CHANCE = 0.1F;
    private static final int BREAK_PARTICLES = 8;

    private int attackTime;

    public EntityEldritchCrab(EntityType<? extends EntityEldritchCrab> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, BARE_SPEED);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, LEAP_STRENGTH));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_HELM, false);
    }

    public boolean hasHelm() {
        return this.entityData.get(DATA_HELM);
    }

    public void setHelm(boolean helm) {
        this.entityData.set(DATA_HELM, helm);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(helm ? HELM_SPEED : BARE_SPEED);
        this.getAttribute(Attributes.ARMOR).setBaseValue(helm ? HELM_ARMOR : 0.0);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        if (level.getDifficulty() == Difficulty.HARD) {
            this.setHelm(true);
        } else {
            this.setHelm(this.random.nextFloat() < HELM_CHANCE);
        }
        RandomSource rand = level.getRandom();
        if (level.getDifficulty() == Difficulty.HARD && rand.nextFloat() < HARD_GROUP_EFFECT_CHANCE * difficulty.getSpecialMultiplier()) {
            var effect = randomGroupEffect(rand);
            this.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION));
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    private static Holder<MobEffect> randomGroupEffect(RandomSource rand) {
        return switch (rand.nextInt(4)) {
            case 0 -> MobEffects.SPEED;
            case 1 -> MobEffects.STRENGTH;
            case 2 -> MobEffects.REGENERATION;
            default -> MobEffects.INVISIBILITY;
        };
    }

    @Override
    public void tick() {
        super.tick();
        this.attackTime--;
        if (this.tickCount < 20) {
            this.fallDistance = 0.0F;
        }
        if (this.level().isClientSide()) {
            return;
        }
        var target = this.getTarget();
        if (this.getVehicle() == null && target != null && !target.isVehicle() && !this.onGround() && !this.hasHelm() && target.isAlive() && this.getY() - target.getY() >= target.getBbHeight() / 2.0F
                && this.distanceToSqr(target) < LATCH_RANGE_SQ) {
            this.startRiding(target);
        }
        if (this.getVehicle() != null && this.isAlive() && this.attackTime <= 0) {
            this.attackTime = 10 + this.random.nextInt(10);
            this.doHurtTarget((ServerLevel) this.level(), this.getVehicle());
            if (this.random.nextFloat() < DISMOUNT_CHANCE) {
                this.stopRiding();
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (super.doHurtTarget(level, target)) {
            this.playSound(TCSounds.CRABCLAW.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
            return true;
        }
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean result = super.hurtServer(level, source, damage);
        if (this.hasHelm() && this.getHealth() / this.getMaxHealth() <= HELM_BREAK_HEALTH_FRACTION) {
            this.setHelm(false);
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, TCItems.CRIMSON_PLATE_CHEST.get()), this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), BREAK_PARTICLES, 0.1,
                    0.1, 0.1, 0.05);
        }
        return result;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHelm(input.getBooleanOr("helm", false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("helm", this.hasHelm());
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.CRABTALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.CRABDEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return entity instanceof EntityEldritchCrab || super.considersEntityAsAlly(entity);
    }
}
