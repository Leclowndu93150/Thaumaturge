package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class EntitySpellBat extends Monster implements TraceableEntity, IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> DATA_FRIENDLY = SynchedEntityData.defineId(EntitySpellBat.class, EntityDataSerializers.BOOLEAN);

    private static final int LIFESPAN_TICKS = 600;
    private static final int ATTACK_COOLDOWN_TICKS = 40;
    private static final double TARGET_RANGE = 12.0;
    private static final float ATTACK_REACH_MIN = 2.5F;
    private static final float ATTACK_REACH_WIDTH_FACTOR = 1.1F;
    private static final float TARGET_EYE_FACTOR = 0.66F;
    private static final double VERTICAL_DRAG = 0.6;
    private static final double WANDER_STEER = 0.1;
    private static final double WANDER_SPEED_XZ = 0.5;
    private static final double WANDER_SPEED_Y = 0.7;
    private static final float FORWARD_IMPULSE = 0.5F;
    private static final int RETARGET_ONE_IN = 30;
    private static final double FLIGHT_TARGET_REACHED = 2.0;
    private static final float BAT_VOLUME = 0.1F;
    private static final float BAT_PITCH_FACTOR = 0.95F;
    private static final float ATTACK_SOUND_VOLUME = 0.5F;
    private static final float ATTACK_SOUND_PITCH_BASE = 0.9F;
    private static final float ATTACK_SOUND_PITCH_SPREAD = 0.2F;
    private static final float ATTACK_SELF_DAMAGE = 1.0F;
    private static final double EFFECT_FX_SPREAD = 0.125;
    private static final int DEFAULT_COLOR = 0xFFFFFF;
    private static final float COLOR_DIVISOR = 255.0F;

    private static final float TICKS_PER_FLAP = 10.0F;

    public final AnimationState flyAnimationState = new AnimationState();

    public int damBonus;

    private @Nullable FocusPackage focusPackage;
    private @Nullable EntityReference<LivingEntity> owner;
    private @Nullable BlockPos currentFlightTarget;
    private int attackTime;
    private @Nullable List<Identifier> effects;
    private int color = DEFAULT_COLOR;

    public EntitySpellBat(EntityType<? extends EntitySpellBat> type, Level level) {
        super(type, level);
    }

    public EntitySpellBat(FocusPackage pack, LivingEntity caster, boolean friendly) {
        super(TCEntities.SPELL_BAT.get(), caster.level());
        this.focusPackage = pack;
        this.setOwner(caster);
        this.setFriendly(friendly);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_FRIENDLY, false);
    }

    public boolean isFriendly() {
        return this.entityData.get(DATA_FRIENDLY);
    }

    public void setFriendly(boolean friendly) {
        this.entityData.set(DATA_FRIENDLY, friendly);
    }

    public int getColor() {
        return this.color;
    }

    public boolean isFlapping() {
        return (float) this.tickCount % TICKS_PER_FLAP == 0.0F;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = EntityReference.of(owner);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        FocusPackages.write(buffer, this.focusPackage);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        this.focusPackage = FocusPackages.read(additionalData);
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
        return SoundEvents.BAT_AMBIENT;
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
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
        if (!this.isFriendly()) {
            super.doPush(entity);
        }
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        LivingEntity currentOwner = this.getOwner();
        return currentOwner != null ? currentOwner.getTeam() : super.getTeam();
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        LivingEntity currentOwner = this.getOwner();
        if (other == currentOwner) {
            return true;
        }
        if (currentOwner == null) {
            return super.considersEntityAsAlly(other);
        }
        return currentOwner.isAlliedTo(other) || other.isAlliedTo(currentOwner);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {}

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && (this.tickCount > LIFESPAN_TICKS || this.getOwner() == null)) {
            this.discard();
        }
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, movement.y * VERTICAL_DRAG, movement.z);
        this.flyAnimationState.startIfStopped(this.tickCount);
        if (this.isAlive() && this.level().isClientSide()) {
            this.clientParticles();
        }
    }

    private void clientParticles() {
        if (this.effects == null) {
            this.effects = FocusPackages.effects(this.focusPackage);
            this.color = averageEffectColor(this.effects);
        }
        if (this.effects.isEmpty()) {
            return;
        }
        Identifier effectId = this.effects.get(this.random.nextInt(this.effects.size()));
        if (FocusEngine.element(effectId) instanceof FocusEffect effect) {
            effect.impactParticles(this.level(), new Vec3(this.getX() + this.random.nextGaussian() * EFFECT_FX_SPREAD,
                    this.getY() + this.getBbHeight() / 2.0F + this.random.nextGaussian() * EFFECT_FX_SPREAD, this.getZ() + this.random.nextGaussian() * EFFECT_FX_SPREAD), Vec3.ZERO);
        }
    }

    private static int averageEffectColor(List<Identifier> effects) {
        if (effects.isEmpty()) {
            return DEFAULT_COLOR;
        }
        int r = 0;
        int g = 0;
        int b = 0;
        for (Identifier effectId : effects) {
            int c = FocusEngine.color(effectId);
            r += (c >> 16) & 0xFF;
            g += (c >> 8) & 0xFF;
            b += c & 0xFF;
        }
        r /= effects.size();
        g /= effects.size();
        b /= effects.size();
        return (r << 16) | (g << 8) | b;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.attackTime > 0) {
            this.attackTime--;
        }
        LivingEntity attackTarget = this.getTarget();
        if (attackTarget == null) {
            this.wanderFlight(level);
        } else {
            this.flyTowards(attackTarget.getX(), attackTarget.getY() + attackTarget.getEyeHeight() * TARGET_EYE_FACTOR, attackTarget.getZ());
        }
        attackTarget = this.getTarget();
        if (attackTarget == null) {
            this.setTarget(this.findTargetToAttack());
        } else if (attackTarget.isAlive()) {
            float distance = attackTarget.distanceTo(this);
            if (this.isAlive() && this.hasLineOfSight(attackTarget)) {
                this.attackEntity(attackTarget, distance);
            }
        } else {
            this.setTarget(null);
        }
        if (!this.isFriendly() && this.getTarget() instanceof Player player && player.getAbilities().invulnerable) {
            this.setTarget(null);
        }
    }

    private void wanderFlight(ServerLevel level) {
        if (this.currentFlightTarget != null && (!level.isEmptyBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() <= level.getMinY())) {
            this.currentFlightTarget = null;
        }
        if (this.currentFlightTarget == null || this.random.nextInt(RETARGET_ONE_IN) == 0 || this.currentFlightTarget.closerToCenterThan(this.position(), FLIGHT_TARGET_REACHED)) {
            this.currentFlightTarget = BlockPos.containing(this.getX() + this.random.nextInt(7) - this.random.nextInt(7), this.getY() + this.random.nextInt(6) - 2.0,
                    this.getZ() + this.random.nextInt(7) - this.random.nextInt(7));
        }
        this.flyTowards(this.currentFlightTarget.getX() + 0.5, this.currentFlightTarget.getY() + 0.1, this.currentFlightTarget.getZ() + 0.5);
    }

    private void flyTowards(double x, double y, double z) {
        double dx = x - this.getX();
        double dy = y - this.getY();
        double dz = z - this.getZ();
        Vec3 movement = this.getDeltaMovement();
        Vec3 steered = movement.add((Math.signum(dx) * WANDER_SPEED_XZ - movement.x) * WANDER_STEER, (Math.signum(dy) * WANDER_SPEED_Y - movement.y) * WANDER_STEER,
                (Math.signum(dz) * WANDER_SPEED_XZ - movement.z) * WANDER_STEER);
        this.setDeltaMovement(steered);
        float yRotD = (float) (Mth.atan2(steered.z, steered.x) * 180.0F / (float) Math.PI) - 90.0F;
        float rotDiff = Mth.wrapDegrees(yRotD - this.getYRot());
        this.zza = FORWARD_IMPULSE;
        this.setYRot(this.getYRot() + rotDiff);
    }

    private void attackEntity(Entity target, float distance) {
        if (this.attackTime > 0 || distance >= Math.max(ATTACK_REACH_MIN, target.getBbWidth() * ATTACK_REACH_WIDTH_FACTOR) || target.getBoundingBox().maxY <= this.getBoundingBox().minY
                || target.getBoundingBox().minY >= this.getBoundingBox().maxY) {
            return;
        }
        this.attackTime = ATTACK_COOLDOWN_TICKS;
        if (!this.level().isClientSide()) {
            LivingEntity currentOwner = this.getOwner();
            if (this.focusPackage != null && currentOwner != null) {
                EntityHitResult ray = new EntityHitResult(target, target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0));
                Trajectory trajectory = new Trajectory(this.position(), ray.getLocation().subtract(this.position()));
                FocusEngine.run(this.level(), this.focusPackage, currentOwner, new CastStreams(new Trajectory[]{trajectory}, new HitResult[]{ray}));
            }
            this.setHealth(this.getHealth() - ATTACK_SELF_DAMAGE);
        }
        this.playSound(SoundEvents.BAT_HURT, ATTACK_SOUND_VOLUME, ATTACK_SOUND_PITCH_BASE + this.random.nextFloat() * ATTACK_SOUND_PITCH_SPREAD);
    }

    private @Nullable LivingEntity findTargetToAttack() {
        LivingEntity currentOwner = this.getOwner();
        double best = Double.MAX_VALUE;
        LivingEntity result = null;
        for (LivingEntity candidate : FocusTargeting.livingInRange(this.level(), this.getX(), this.getY(), this.getZ(), this, TARGET_RANGE)) {
            if (!candidate.isAlive()) {
                continue;
            }
            boolean friendly = FocusTargeting.isFriendly(currentOwner, candidate);
            boolean matches = this.isFriendly() ? friendly : !friendly && !this.isAlliedTo(candidate);
            if (matches) {
                double distanceSq = this.distanceToSqr(candidate);
                if (distanceSq < best) {
                    best = distanceSq;
                    result = candidate;
                }
            }
        }
        return result;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        EntityReference.store(this.owner, output, "OwnerUUID");
        output.putBoolean("friendly", this.isFriendly());
        FocusPackages.save(output, this.focusPackage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.owner = EntityReference.read(input, "OwnerUUID");
        this.setFriendly(input.getBooleanOr("friendly", false));
        this.focusPackage = FocusPackages.load(input);
    }
}
