package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.FireBatAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.FlyingWanderGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EntityFireBat extends Monster {
    private static final int SPAWN_LIGHT_CAP = 7;
    private static final EntityDataAccessor<Boolean> DATA_HANGING = SynchedEntityData.defineId(EntityFireBat.class, EntityDataSerializers.BOOLEAN);

    private static final int BAT_TAKEOFF_LEVEL_EVENT = 1025;
    private static final double WAKE_PLAYER_RANGE = 4.0;
    private static final int HANG_ONE_IN = 100;
    private static final int HEAD_TURN_ONE_IN = 200;
    private static final float VERTICAL_DRAG = 0.6F;
    private static final float FLYING_FRICTION_IMPULSE = 0.02F;
    private static final float BAT_VOLUME = 0.1F;
    private static final float BAT_PITCH_FACTOR = 0.95F;

    private static final float TICKS_PER_FLAP = 10.0F;

    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState restAnimationState = new AnimationState();

    public @Nullable LivingEntity owner;
    public int damBonus;
    private int summonLife;

    private static final int SUMMON_LIFESPAN_TICKS = 600;

    public boolean isFlapping() {
        return !isHanging() && (float) this.tickCount % TICKS_PER_FLAP == 0.0F;
    }

    public void summon(@Nullable LivingEntity summoner, @Nullable LivingEntity target, int bonus) {
        this.owner = summoner;
        this.damBonus = bonus;
        this.summonLife = SUMMON_LIFESPAN_TICKS;
        this.setHanging(false);
        if (target != null) {
            this.setTarget(target);
        }
    }

    public boolean isSummoned() {
        return summonLife > 0;
    }

    public static boolean checkFireBatSpawnRules(EntityType<EntityFireBat> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos) > random.nextInt(SPAWN_LIGHT_CAP)) {
            return false;
        }
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    public EntityFireBat(EntityType<? extends EntityFireBat> type, Level level) {
        super(type, level);
        this.moveControl = new Ghast.GhastMoveControl(this, false, () -> false);
        this.setHanging(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.FOLLOW_RANGE, 12.0).add(Attributes.FLYING_SPEED, 0.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new FireBatAttackGoal(this));
        this.goalSelector.addGoal(5, new FlyingWanderGoal(this, false, () -> !isHanging()));
        this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (target, level) -> target != this.owner));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_HANGING, false);
    }

    public boolean isHanging() {
        return this.entityData.get(DATA_HANGING);
    }

    public void setHanging(boolean hanging) {
        this.entityData.set(DATA_HANGING, hanging);
    }

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, FLYING_FRICTION_IMPULSE);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {}

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {}

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected float getSoundVolume() {
        return BAT_VOLUME;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * BAT_PITCH_FACTOR;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return isHanging() && this.random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isHanging()) {
            setHanging(false);
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.summonLife > 0 && --this.summonLife == 0) {
            this.level().levelEvent(2004, this.blockPosition(), 0);
            this.discard();
            return;
        }
        if (isHanging()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setPosRaw(this.getX(), Mth.floor(this.getY()) + 1.0 - this.getBbHeight(), this.getZ());
        } else {
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(movement.x, movement.y * VERTICAL_DRAG, movement.z);
        }
        this.setupAnimationStates();
    }

    private void setupAnimationStates() {
        if (isHanging()) {
            this.flyAnimationState.stop();
            this.restAnimationState.startIfStopped(this.tickCount);
        } else {
            this.restAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        if (this.isInWaterOrRain()) {
            this.hurtServer(server, this.damageSources().drown(), 1.0F);
        }
        BlockPos pos = this.blockPosition();
        BlockPos above = pos.above();
        if (isHanging()) {
            boolean canKeepHanging = this.level().getBlockState(above).isRedstoneConductor(this.level(), above);
            if (!canKeepHanging || this.level().getNearestPlayer(this, WAKE_PLAYER_RANGE) != null) {
                setHanging(false);
                this.level().levelEvent(null, BAT_TAKEOFF_LEVEL_EVENT, pos, 0);
            } else if (this.random.nextInt(HEAD_TURN_ONE_IN) == 0) {
                this.yHeadRot = this.random.nextInt(360);
            }
        } else if (this.getTarget() == null && this.random.nextInt(HANG_ONE_IN) == 0 && this.level().getBlockState(above).isRedstoneConductor(this.level(), above)) {
            setHanging(true);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("hang", isHanging());
        output.putByte("damBonus", (byte) this.damBonus);
        output.putInt("SummonLife", this.summonLife);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setHanging(input.getBooleanOr("hang", false));
        this.damBonus = input.getByteOr("damBonus", (byte) 0);
        this.summonLife = input.getIntOr("SummonLife", 0);
    }
}
