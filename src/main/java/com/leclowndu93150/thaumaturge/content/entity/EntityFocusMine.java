package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class EntityFocusMine extends ThrowableProjectile implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> DATA_ARMED = SynchedEntityData.defineId(EntityFocusMine.class, EntityDataSerializers.BOOLEAN);

    private static final int LIFESPAN_TICKS = 1200;
    private static final int ARM_DELAY_TICKS = 40;
    private static final int TRIGGER_INTERVAL_TICKS = 5;
    private static final double TRIGGER_RANGE = 1.0;
    private static final double MINE_GRAVITY = 0.01;
    private static final double EMBEDDED_DAMPING = 0.25;
    private static final double EFFECT_FX_SPREAD = 0.1;
    private static final double EFFECT_FX_MOTION = 0.01;

    private @Nullable FocusPackage focusPackage;
    private boolean friendly;
    private int counter = ARM_DELAY_TICKS;
    private @Nullable List<Identifier> effects;
    private final Deque<PendingStrike> pendingStrikes = new ArrayDeque<>();
    private boolean detonated;

    public EntityFocusMine(EntityType<? extends EntityFocusMine> type, Level level) {
        super(type, level);
    }

    public EntityFocusMine(FocusPackage pack, LivingEntity caster, Trajectory trajectory, boolean friendly) {
        super(TCEntities.FOCUS_MINE.get(), caster.level());
        this.focusPackage = pack;
        this.friendly = friendly;
        this.setOwner(caster);
        this.setPos(trajectory.source().x, trajectory.source().y, trajectory.source().z);
        this.shoot(trajectory.direction().x, trajectory.direction().y, trajectory.direction().z, 0.0F, 0.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_ARMED, false);
    }

    public boolean isArmed() {
        return this.entityData.get(DATA_ARMED);
    }

    public int renderColor() {
        List<Identifier> effects = FocusPackages.effects(this.focusPackage);
        return effects.isEmpty() ? 0xFFFFFF : FocusEngine.color(effects.getFirst());
    }

    public void setArmed(boolean armed) {
        this.entityData.set(DATA_ARMED, armed);
    }

    public int getCounter() {
        return this.counter;
    }

    @Override
    protected double getDefaultGravity() {
        return MINE_GRAVITY;
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
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("armed", this.isArmed());
        output.putBoolean("friendly", this.friendly);
        FocusPackages.save(output, this.focusPackage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.friendly = input.getBooleanOr("friendly", false);
        this.setArmed(input.getBooleanOr("armed", false));
        if (this.isArmed()) {
            this.counter = 0;
        }
        this.focusPackage = FocusPackages.load(input);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.getOwner() != null) {
            this.setArmed(true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().noCollision(this)) {
            this.moveTowardsClosestSpace(this.getX(), this.getY(), this.getZ());
            this.setDeltaMovement(this.getDeltaMovement().scale(EMBEDDED_DAMPING));
        }
        if (this.tickCount > LIFESPAN_TICKS || !this.level().isClientSide() && this.getOwner() == null) {
            this.discard();
        }
        if (!this.isAlive() || !this.isArmed()) {
            return;
        }
        if (!this.pendingStrikes.isEmpty()) {
            this.strike(this.pendingStrikes.poll());
            if (this.pendingStrikes.isEmpty()) {
                this.discard();
            }
            return;
        }
        if (this.detonated) {
            return;
        }
        if (this.counter > 0) {
            this.counter--;
        }
        if (this.counter <= 0 && this.tickCount % TRIGGER_INTERVAL_TICKS == 0) {
            if (this.level().isClientSide()) {
                this.clientParticles();
            } else {
                this.trigger();
            }
        }
    }

    private void clientParticles() {
        if (this.effects == null) {
            this.effects = FocusPackages.effects(this.focusPackage);
        }
        if (this.effects.isEmpty()) {
            return;
        }
        Identifier effectId = this.effects.get(this.random.nextInt(this.effects.size()));
        if (FocusEngine.element(effectId) instanceof FocusEffect effect) {
            effect.impactParticles(this.level(),
                    new Vec3(this.getX() + this.random.nextGaussian() * EFFECT_FX_SPREAD, this.getY() + this.random.nextGaussian() * EFFECT_FX_SPREAD,
                            this.getZ() + this.random.nextGaussian() * EFFECT_FX_SPREAD),
                    new Vec3(this.random.nextGaussian() * EFFECT_FX_MOTION, this.random.nextGaussian() * EFFECT_FX_MOTION, this.random.nextGaussian() * EFFECT_FX_MOTION));
        }
    }

    private void trigger() {
        LivingEntity caster = this.getOwner() instanceof LivingEntity living ? living : null;
        for (LivingEntity candidate : FocusTargeting.livingInRange(this.level(), this.getX(), this.getY(), this.getZ(), this, TRIGGER_RANGE)) {
            if (candidate.isAlive() && (this.friendly ? FocusTargeting.isFriendly(caster, candidate) : !FocusTargeting.isFriendly(caster, candidate))) {
                Vec3 point = candidate.position().add(0.0, candidate.getBbHeight() / 2.0F, 0.0);
                this.pendingStrikes.add(new PendingStrike(candidate, point));
            }
        }
        if (!this.pendingStrikes.isEmpty()) {
            this.detonated = true;
            this.strike(this.pendingStrikes.poll());
            if (this.pendingStrikes.isEmpty()) {
                this.discard();
            }
        }
    }

    private void strike(PendingStrike pending) {
        if (this.focusPackage == null || !(this.getOwner() instanceof LivingEntity livingOwner)) {
            return;
        }
        EntityHitResult ray = new EntityHitResult(pending.target(), pending.target().position().add(0.0, pending.target().getBbHeight() / 2.0F, 0.0));
        FocusEngine.run(this.level(), this.focusPackage, livingOwner,
                new CastStreams(new Trajectory[]{new Trajectory(this.position(), pending.point().subtract(this.position()).normalize())}, new HitResult[]{ray}));
    }

    private record PendingStrike(LivingEntity target, Vec3 point) {
    }
}
