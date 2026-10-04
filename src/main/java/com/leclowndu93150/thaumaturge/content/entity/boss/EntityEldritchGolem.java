package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.LeadingAim;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.content.world.mound.BlockLoot;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityEldritchGolem extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(EntityEldritchGolem.class, EntityDataSerializers.BOOLEAN);
    private static final String HEADLESS_KEY = "headless";
    private static final byte SWING_EVENT = 4;
    private static final double SPEED = 0.3;
    private static final double STRIKE = 10.0;
    private static final double HEALTH = 400.0;
    private static final double PLATING = 6.0;
    private static final double CHARGE_PACE = 1.1;
    private static final double WANDER_PACE = 0.8;
    private static final float GAZE_RANGE = 8.0F;
    private static final double BEAM_MIN_DISTANCE = 3.0;
    private static final double BEAM_PACE = 1.0;
    private static final int BEAM_INTERVAL = 5;
    private static final float BEAM_RANGE = 24.0F;
    private static final float BEAM_SPEED = 0.66F;
    private static final float BEAM_SPREAD = 5.0F;
    private static final float AIM_TURN = 30.0F;
    private static final int AWAKENING = 100;
    private static final float AWAKENING_MEND = 2.0F;
    private static final int SWING_RECOVERY = 10;
    private static final float SWING_SHARE = 0.75F;
    private static final double SWING_LIFT = 0.2;
    private static final float HEADLESS_FLING = 1.5F;
    private static final double FLING_RISE = 0.1;
    private static final float DECAPITATION_BLAST = 2.0F;
    private static final float HEAD_OFFSET = 0.75F;
    private static final float TRAMPLE_HARDNESS = 0.15F;
    private static final int STOMP_ODDS = 5;
    private static final double STRIDE_SQ = 2.5000003E-7F;
    private static final double DUST_LIFT = 0.1;
    private static final double DUST_SPRAY = 4.0;
    private static final double DUST_RISE = 0.5;

    private final GolemBeamCore beamCore = new GolemBeamCore(this);
    private boolean beamArmed;
    private int swingCooldown;

    public EntityEldritchGolem(EntityType<? extends EntityEldritchGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MOVEMENT_SPEED, SPEED).add(Attributes.ATTACK_DAMAGE, STRIKE).add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.ARMOR, PLATING);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, CHARGE_PACE, false));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, WANDER_PACE));
        goalSelector.addGoal(7, new RandomStrollGoal(this, WANDER_PACE));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, GAZE_RANGE));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(HEADLESS, false);
    }

    public boolean isHeadless() {
        return entityData.get(HEADLESS);
    }

    public int swingCooldown() {
        return swingCooldown;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        spawnTimer = AWAKENING;
        ChampionHelper.makeChampion(this, true);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void generateName() {
        MobTraits.champion(this).ifPresent(trait -> setCustomName(Component.translatable("entity.thaumaturge.eldritch_golem.name.custom", MobTraitNames.of(trait))));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        if (getSpawnTimer() > 0) {
            heal(AWAKENING_MEND);
        }
        if (isHeadless()) {
            beamCore.tick(server);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (swingCooldown > 0) {
            swingCooldown--;
        }
        boolean stomping = getDeltaMovement().horizontalDistanceSqr() > STRIDE_SQ && random.nextInt(STOMP_ODDS) == 0;
        BlockPos feet = blockPosition();
        BlockState underfoot = level().getBlockState(feet);
        if (level().isClientSide()) {
            if (stomping && !underfoot.isAir()) {
                kickUpDust(underfoot);
            }
            return;
        }
        if (stomping && underfoot.getBlock() instanceof BlockLoot) {
            level().destroyBlock(feet, true);
            underfoot = level().getBlockState(feet);
        }
        float hardness = underfoot.getDestroySpeed(level(), feet);
        if (!underfoot.isAir() && hardness >= 0.0F && hardness <= TRAMPLE_HARDNESS) {
            level().destroyBlock(feet, true);
        }
    }

    private void kickUpDust(BlockState underfoot) {
        level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, underfoot), getX() + (random.nextFloat() - 0.5) * getBbWidth(), getBoundingBox().minY + DUST_LIFT,
                getZ() + (random.nextFloat() - 0.5) * getBbWidth(), DUST_SPRAY * (random.nextFloat() - 0.5), DUST_RISE, (random.nextFloat() - 0.5) * DUST_SPRAY);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!isHeadless() && damage > getHealth() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            loseHead(level);
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    private void loseHead(ServerLevel level) {
        entityData.set(HEADLESS, true);
        spawnTimer = AWAKENING;
        float bearing = getYRot() % 360.0F * Mth.DEG_TO_RAD;
        level.explode(this, getX() + Mth.cos(bearing) * HEAD_OFFSET, getEyeY(), getZ() + Mth.sin(bearing) * HEAD_OFFSET, DECAPITATION_BLAST, false, Level.ExplosionInteraction.NONE);
        armBeam();
    }

    private void armBeam() {
        if (!beamArmed) {
            beamArmed = true;
            goalSelector.addGoal(2, new LongRangeAttackGoal(this, BEAM_MIN_DISTANCE, BEAM_PACE, BEAM_INTERVAL, BEAM_INTERVAL, BEAM_RANGE));
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (swingCooldown > 0) {
            return false;
        }
        swingCooldown = SWING_RECOVERY;
        level.broadcastEntityEvent(this, SWING_EVENT);
        boolean landed = target.hurtServer(level, damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * SWING_SHARE);
        if (landed) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, SWING_LIFT, 0.0));
            if (isHeadless()) {
                float bearing = getYRot() * Mth.DEG_TO_RAD;
                target.push(-Mth.sin(bearing) * HEADLESS_FLING, FLING_RISE, Mth.cos(bearing) * HEADLESS_FLING);
            }
        }
        return landed;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (!beamCore.ready() || !hasLineOfSight(target)) {
            return;
        }
        beamCore.spend(random);
        Vec3 heart = target.getBoundingBox().getCenter();
        getLookControl().setLookAt(heart.x, heart.y, heart.z, AIM_TURN, AIM_TURN);
        Vec3 look = getLookAngle();
        EntityGolemOrb orb = new EntityGolemOrb(level(), this, target, false);
        orb.setPos(orb.getX() + look.x, orb.getY(), orb.getZ() + look.z);
        Vec3 aim = LeadingAim.at(this, target);
        orb.shoot(aim.x, aim.y, aim.z, BEAM_SPEED, BEAM_SPREAD);
        playSound(TCSounds.EGATTACK.get(), 1.0F, 1.0F + random.nextFloat() * 0.1F);
        level().addFreshEntity(orb);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event != SWING_EVENT) {
            super.handleEntityEvent(event);
            return;
        }
        swingCooldown = SWING_RECOVERY;
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(HEADLESS_KEY, isHeadless());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(HEADLESS, input.getBooleanOr(HEADLESS_KEY, false));
        if (isHeadless()) {
            armBeam();
        }
    }
}
