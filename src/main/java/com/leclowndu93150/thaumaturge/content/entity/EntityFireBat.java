package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.FireBatAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.FlyingWanderGoal;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastAuthority;
import com.leclowndu93150.thaumaturge.content.spell.engine.CastAuthorities;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
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
    private static final EntityDataAccessor<Boolean> HANGING = SynchedEntityData.defineId(EntityFireBat.class, EntityDataSerializers.BOOLEAN);
    private static final TargetingConditions HANG_DISTURBANCE = TargetingConditions.forNonCombat().range(4.0);
    private static final String HANG_KEY = "hang";
    private static final String DAMAGE_BONUS_KEY = "damBonus";
    private static final String LIFETIME_KEY = "SummonLife";
    private static final double MAX_HEALTH = 5.0;
    private static final double ATTACK_DAMAGE = 1.0;
    private static final double FOLLOW_RANGE = 12.0;
    private static final double FLYING_SPEED = 0.1;
    private static final float RELATIVE_FLYING_SPEED = 0.02F;
    private static final int ATTACK_PRIORITY = 4;
    private static final int WANDER_PRIORITY = 5;
    private static final int LOOK_PRIORITY = 7;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_PRIORITY = 2;
    private static final int PLAYER_SCAN_INTERVAL = 10;
    private static final int SPAWN_LIGHT_BOUND = 7;
    private static final int SUMMON_LIFETIME = 600;
    private static final int FLAP_INTERVAL = 10;
    private static final float SOUND_VOLUME = 0.1F;
    private static final float VOICE_PITCH_FACTOR = 0.95F;
    private static final int HANGING_AMBIENT_ONE_IN = 4;
    private static final float DROWN_DAMAGE = 1.0F;
    private static final int TAKEOFF_EVENT = 1025;
    private static final int EXPIRY_EVENT = 2004;
    private static final int HEAD_TURN_ONE_IN = 200;
    private static final int HEAD_TURN_RANGE = 360;
    private static final int START_HANGING_ONE_IN = 100;
    private static final double FLIGHT_VERTICAL_DAMPING = 0.6;

    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState restAnimationState = new AnimationState();
    public @Nullable LivingEntity owner;
    public int damBonus;

    private int lifetime;
    private @Nullable CastAuthority castAuthority;
    private @Nullable UUID authorityToken;
    private long authoritySequence;
    private long nextAuthorizedStrike;
    private boolean requiresAuthority;

    public EntityFireBat(EntityType<? extends EntityFireBat> type, Level level) {
        super(type, level);
        this.moveControl = new Ghast.GhastMoveControl(this, false, () -> false);
        this.setHanging(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.FLYING_SPEED, FLYING_SPEED);
    }

    public static boolean checkFireBatSpawnRules(EntityType<EntityFireBat> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getMaxLocalRawBrightness(pos) <= random.nextInt(SPAWN_LIGHT_BOUND) && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    public boolean isFlapping() {
        return !this.isHanging() && this.tickCount % FLAP_INTERVAL == 0;
    }

    public void summon(LivingEntity owner, @Nullable LivingEntity target, int damageBonus) {
        this.owner = owner;
        this.damBonus = damageBonus;
        this.lifetime = SUMMON_LIFETIME;
        this.setHanging(false);
        if (target != null) {
            this.setTarget(target);
        }
    }

    public boolean isSummoned() {
        return this.lifetime > 0;
    }

    public boolean authorizeSummon(ServerLevel level, CastAuthority authority) {
        requiresAuthority = true;
        castAuthority = authority;
        authorityToken = CastAuthorities.issue(level, authority).orElse(null);
        return authorityToken != null;
    }

    public boolean hasCastAuthority() {
        return requiresAuthority;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (requiresAuthority) {
            if (castAuthority == null || authorityToken == null || authoritySequence == Long.MAX_VALUE || level.getGameTime() < nextAuthorizedStrike
                    || !CastAuthorities.valid(level, castAuthority, target.position(), target) || !CastAuthorities.claim(level, castAuthority, authorityToken, authoritySequence)) {
                return false;
            }
            authoritySequence++;
            nextAuthorizedStrike = level.getGameTime() + 40;
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(ATTACK_PRIORITY, new FireBatAttackGoal(this));
        this.goalSelector.addGoal(WANDER_PRIORITY, new FlyingWanderGoal(this, false, this::isFlying));
        this.goalSelector.addGoal(LOOK_PRIORITY, new Ghast.GhastLookGoal(this));
        this.targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(PLAYER_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, PLAYER_SCAN_INTERVAL, false, false, this::isNotOwner));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HANGING, false);
    }

    public boolean isHanging() {
        return this.entityData.get(HANGING);
    }

    public void setHanging(boolean hanging) {
        this.entityData.set(HANGING, hanging);
    }

    @Override
    public void tick() {
        if (requiresAuthority && level() instanceof ServerLevel level) {
            if (castAuthority == null || authorityToken == null || !CastAuthorities.valid(level, castAuthority, position(), null)) {
                discard();
                return;
            }
            owner = level.getEntity(castAuthority.sourceId()) instanceof LivingEntity source ? source : null;
        }
        super.tick();
        steadyBody();
        updateAnimations();
        if (this.level() instanceof ServerLevel level && this.lifetime > 0 && --this.lifetime == 0) {
            level.levelEvent(null, EXPIRY_EVENT, this.blockPosition(), 0);
            this.discard();
        }
    }

    private void steadyBody() {
        if (this.isHanging()) {
            clingToCeiling();
        } else {
            dampVerticalSpeed();
        }
    }

    private void clingToCeiling() {
        this.setDeltaMovement(Vec3.ZERO);
        this.setPosRaw(this.getX(), Mth.floor(this.getY()) + 1.0 - this.getBbHeight(), this.getZ());
    }

    private void dampVerticalSpeed() {
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, FLIGHT_VERTICAL_DAMPING, 1.0));
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.setHanging(false);
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level) {
            douse(level);
            roost(level, hasSolidCeiling(level));
        }
    }

    private void douse(ServerLevel level) {
        if (this.isInWaterOrRain()) {
            this.hurtServer(level, level.damageSources().drown(), DROWN_DAMAGE);
        }
    }

    private void takeOff(ServerLevel level) {
        level.levelEvent(null, TAKEOFF_EVENT, this.blockPosition(), 0);
        this.setHanging(false);
    }

    private boolean hasSolidCeiling(ServerLevel level) {
        BlockPos above = this.blockPosition().above();
        return level.getBlockState(above).isRedstoneConductor(level, above);
    }

    private void roost(ServerLevel level, boolean ceiling) {
        if (this.isHanging()) {
            hangOrLeave(level, ceiling);
        } else if (ceiling && this.getTarget() == null && this.getRandom().nextInt(START_HANGING_ONE_IN) == 0) {
            this.setHanging(true);
        }
    }

    private void hangOrLeave(ServerLevel level, boolean ceiling) {
        if (!ceiling || level.getNearestPlayer(HANG_DISTURBANCE, this) != null) {
            takeOff(level);
            return;
        }
        if (this.getRandom().nextInt(HEAD_TURN_ONE_IN) == 0) {
            this.setYHeadRot(this.getRandom().nextInt(HEAD_TURN_RANGE));
        }
    }

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, RELATIVE_FLYING_SPEED);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * VOICE_PITCH_FACTOR;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        writeBatState(output);
    }

    private void writeBatState(ValueOutput output) {
        output.putInt(LIFETIME_KEY, this.lifetime);
        output.putByte(DAMAGE_BONUS_KEY, (byte) this.damBonus);
        output.putBoolean(HANG_KEY, this.isHanging());
        output.putBoolean("requires_authority", requiresAuthority);
        output.storeNullable("cast_authority", CastAuthority.CODEC, castAuthority);
        output.storeNullable("authority_token", UUIDUtil.CODEC, authorityToken);
        output.putLong("authority_sequence", authoritySequence);
        output.putLong("next_authorized_strike", nextAuthorizedStrike);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lifetime = input.getIntOr(LIFETIME_KEY, 0);
        this.damBonus = input.getByteOr(DAMAGE_BONUS_KEY, (byte) 0);
        this.setHanging(input.getBooleanOr(HANG_KEY, false));
        requiresAuthority = input.getBooleanOr("requires_authority", false);
        castAuthority = input.read("cast_authority", CastAuthority.CODEC).orElse(null);
        authorityToken = input.read("authority_token", UUIDUtil.CODEC).orElse(null);
        authoritySequence = input.getLongOr("authority_sequence", 0);
        nextAuthorizedStrike = input.getLongOr("next_authorized_strike", 0);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (!this.isHanging()) {
            return SoundEvents.BAT_AMBIENT;
        }
        return this.getRandom().nextInt(HANGING_AMBIENT_ONE_IN) == 0 ? SoundEvents.BAT_AMBIENT : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        SoundEvent cry = SoundEvents.BAT_DEATH;
        return cry;
    }

    @Override
    protected void doPush(Entity entity) {}

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(level, source);
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState state, BlockPos pos) {}

    private boolean isFlying() {
        return !this.isHanging();
    }

    private boolean isNotOwner(LivingEntity candidate, ServerLevel level) {
        return candidate != this.owner;
    }

    private void updateAnimations() {
        if (this.isHanging()) {
            this.flyAnimationState.stop();
            this.restAnimationState.startIfStopped(this.tickCount);
        } else {
            this.restAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        }
    }
}
