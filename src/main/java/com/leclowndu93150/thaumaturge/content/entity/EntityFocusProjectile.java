package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class EntityFocusProjectile extends ThrowableProjectile implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Integer> DATA_SPECIAL = SynchedEntityData.defineId(EntityFocusProjectile.class, EntityDataSerializers.INT);

    public static final int SPECIAL_NONE = 0;
    public static final int SPECIAL_BOUNCY = 1;
    public static final int SPECIAL_SEEK_ENEMY = 2;
    public static final int SPECIAL_SEEK_FRIENDLY = 3;

    private static final int LIFESPAN_TICKS = 1200;
    private static final double SPAWN_OFFSET_FACTOR = 2.1;
    private static final double LIGHT_GRAVITY = 0.005;
    private static final double NORMAL_GRAVITY = 0.01;
    private static final int SEEK_INTERVAL_TICKS = 5;
    private static final double SEEK_RANGE = 16.0;
    private static final float SEEK_FOV = 1.75F;
    private static final double SEEK_STEER = 0.275;
    private static final double TARGET_HEIGHT_FACTOR = 0.6;
    private static final double BOUNCE_DAMPING = 0.9;
    private static final double BOUNCE_BACKOFF = 0.05;
    private static final double BOUNCE_MIN_SPEED = 0.2;
    private static final float BOUNCE_VOLUME = 0.25F;
    private static final float FIRE_MOTE_ALPHA = 0.5F;
    private static final float FIRE_MOTE_SCALE = 7.0F;
    private static final float FIRE_MOTE_JITTER = 0.0125F;
    private static final double EFFECT_FX_SPREAD = 0.1;
    private static final double EFFECT_FX_MOTION = 0.01;
    private static final float COLOR_DIVISOR = 255.0F;

    private @Nullable FocusPackage focusPackage;
    private @Nullable Entity target;
    private boolean firstParticle;
    private float lastRenderTick;
    private @Nullable List<Identifier> effects;

    public EntityFocusProjectile(EntityType<? extends EntityFocusProjectile> type, Level level) {
        super(type, level);
    }

    public EntityFocusProjectile(FocusPackage pack, LivingEntity caster, float speed, Trajectory trajectory, int special) {
        super(TCEntities.FOCUS_PROJECTILE.get(), caster.level());
        this.focusPackage = pack;
        this.setOwner(caster);
        double width = caster.getBbWidth();
        this.setPos(trajectory.source().x + trajectory.direction().x * width * SPAWN_OFFSET_FACTOR, trajectory.source().y + trajectory.direction().y * width * SPAWN_OFFSET_FACTOR,
                trajectory.source().z + trajectory.direction().z * width * SPAWN_OFFSET_FACTOR);
        this.shoot(trajectory.direction().x, trajectory.direction().y, trajectory.direction().z, speed, 0.0F);
        this.setSpecial(special);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_SPECIAL, SPECIAL_NONE);
    }

    public void setSpecial(int special) {
        this.entityData.set(DATA_SPECIAL, special);
    }

    public int getSpecial() {
        return this.entityData.get(DATA_SPECIAL);
    }

    @Override
    protected double getDefaultGravity() {
        return this.getSpecial() > SPECIAL_BOUNCY ? LIGHT_GRAVITY : NORMAL_GRAVITY;
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
        FocusPackages.save(output, this.focusPackage);
        output.putInt("special", this.getSpecial());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSpecial(input.getIntOr("special", SPECIAL_NONE));
        this.focusPackage = FocusPackages.load(input);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.getSpecial() == SPECIAL_BOUNCY && hitResult instanceof BlockHitResult blockHit) {
            this.bounce(blockHit);
            return;
        }
        if (this.level().isClientSide()) {
            return;
        }
        HitResult reported = hitResult instanceof EntityHitResult entityHit ? new EntityHitResult(entityHit.getEntity(), this.position()) : hitResult;
        Vec3 previous = new Vec3(this.xOld, this.yOld, this.zOld);
        Vec3 motion = this.getDeltaMovement();
        if (this.focusPackage != null) {
            LivingEntity caster = this.getOwner() instanceof LivingEntity living ? living : null;
            FocusEngine.run(this.level(), this.focusPackage, caster, new CastStreams(new Trajectory[]{new Trajectory(previous, motion.normalize())}, new HitResult[]{reported}));
        }
        this.discard();
    }

    private void bounce(BlockHitResult blockHit) {
        if (this.level().getBlockState(blockHit.getBlockPos()).getCollisionShape(this.level(), blockHit.getBlockPos()).isEmpty()) {
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() - motion.x, this.getY() - motion.y, this.getZ() - motion.z);
        double mx = motion.x;
        double my = motion.y;
        double mz = motion.z;
        if (blockHit.getDirection().getStepZ() != 0) {
            mz *= -1.0;
        }
        if (blockHit.getDirection().getStepX() != 0) {
            mx *= -1.0;
        }
        if (blockHit.getDirection().getStepY() != 0) {
            my *= -BOUNCE_DAMPING;
        }
        mx *= BOUNCE_DAMPING;
        my *= BOUNCE_DAMPING;
        mz *= BOUNCE_DAMPING;
        this.setDeltaMovement(mx, my, mz);
        double speed = Math.sqrt(mx * mx + my * my + mz * mz);
        if (speed > 0.0) {
            this.setPos(this.getX() - mx / speed * BOUNCE_BACKOFF, this.getY() - my / speed * BOUNCE_BACKOFF, this.getZ() - mz / speed * BOUNCE_BACKOFF);
        }
        if (!this.level().isClientSide()) {
            this.playSound(SoundEvents.LEAD_TIED, BOUNCE_VOLUME, 1.0F);
            if (this.getDeltaMovement().length() < BOUNCE_MIN_SPEED) {
                this.discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > LIFESPAN_TICKS || !this.level().isClientSide() && this.getOwner() == null) {
            this.discard();
        }
        this.firstParticle = true;
        if (this.target == null && this.tickCount % SEEK_INTERVAL_TICKS == 0 && this.getSpecial() > SPECIAL_BOUNCY) {
            this.acquireTarget();
        }
        if (this.target != null) {
            this.steerTowardsTarget();
        }
    }

    private void acquireTarget() {
        Vec3 look = this.getDeltaMovement().normalize();
        for (LivingEntity candidate : FocusTargeting.livingInRangeSorted(this.level(), this, SEEK_RANGE)) {
            if (candidate.isAlive() && FocusTargeting.isVisibleTo(SEEK_FOV, this, look, candidate, (float) SEEK_RANGE) && FocusTargeting.canEntityBeSeen(this, candidate)) {
                boolean friendly = FocusTargeting.isFriendly(this.getOwner(), candidate);
                if (friendly && this.getSpecial() == SPECIAL_SEEK_FRIENDLY) {
                    this.target = candidate;
                    break;
                }
                if (!friendly && this.getSpecial() == SPECIAL_SEEK_ENEMY) {
                    this.target = candidate;
                    break;
                }
            }
        }
    }

    private void steerTowardsTarget() {
        double dx = this.target.getX() - this.getX();
        double dy = this.target.getBoundingBox().minY + this.target.getBbHeight() * TARGET_HEIGHT_FACTOR - this.getY();
        double dz = this.target.getZ() - this.getZ();
        Vec3 toTarget = new Vec3(dx, dy, dz).normalize();
        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        Vec3 steered = motion.normalize().add(toTarget.scale(SEEK_STEER)).normalize().scale(speed);
        this.setDeltaMovement(steered);
        if (this.tickCount % SEEK_INTERVAL_TICKS == 0 && (!this.target.isAlive() || !FocusTargeting.isVisibleTo(SEEK_FOV, this, steered.normalize(), this.target, (float) SEEK_RANGE)
                || !FocusTargeting.canEntityBeSeen(this, this.target))) {
            this.target = null;
        }
    }

    public void renderParticle(float coeff) {
        float delta = coeff - this.lastRenderTick;
        if (delta < 0.0F) {
            delta++;
        }
        if (delta <= 0.2F) {
            return;
        }
        this.lastRenderTick = coeff;
        if (this.effects == null) {
            this.effects = FocusPackages.effects(this.focusPackage);
        }
        if (this.effects.isEmpty()) {
            return;
        }
        Identifier effectId = this.effects.get(this.random.nextInt(this.effects.size()));
        int color = FocusEngine.color(effectId);
        float r = ((color >> 16) & 0xFF) / COLOR_DIVISOR;
        float g = ((color >> 8) & 0xFF) / COLOR_DIVISOR;
        float b = (color & 0xFF) / COLOR_DIVISOR;
        double x = this.xOld + (this.getX() - this.xOld) * coeff;
        double y = this.yOld + (this.getY() - this.yOld) * coeff + this.getBbHeight() / 2.0F;
        double z = this.zOld + (this.getZ() - this.zOld) * coeff;
        this.level().addParticle(Effects.fireMoteData(this.random, FIRE_MOTE_JITTER * (this.random.nextFloat() - 0.5F), FIRE_MOTE_JITTER * (this.random.nextFloat() - 0.5F),
                FIRE_MOTE_JITTER * (this.random.nextFloat() - 0.5F), r, g, b, FIRE_MOTE_ALPHA, FIRE_MOTE_SCALE), x, y, z, 0.0, 0.0, 0.0);
        if (this.firstParticle && FocusEngine.element(effectId) instanceof FocusEffect effect) {
            this.firstParticle = false;
            effect.impactParticles(this.level(),
                    new Vec3(x + this.random.nextGaussian() * EFFECT_FX_SPREAD, y + this.random.nextGaussian() * EFFECT_FX_SPREAD, z + this.random.nextGaussian() * EFFECT_FX_SPREAD),
                    new Vec3(this.random.nextGaussian() * EFFECT_FX_MOTION, this.random.nextGaussian() * EFFECT_FX_MOTION, this.random.nextGaussian() * EFFECT_FX_MOTION));
        }
    }
}
