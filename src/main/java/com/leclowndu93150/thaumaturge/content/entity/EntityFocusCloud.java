package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class EntityFocusCloud extends Entity implements TraceableEntity, IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(EntityFocusCloud.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_RADIUS = 0.5F;
    private static final float CLOUD_HEIGHT = 0.5F;
    private static final int APPLY_INTERVAL_TICKS = 5;
    private static final int COOLDOWN_TICKS = 40;
    private static final int TICKS_PER_SECOND = 20;
    private static final double CLOUD_PUSH_DIVISOR = 50.0;
    private static final double PARTICLE_SPREAD_FACTOR = 0.85;
    private static final double PARTICLE_MOTION = 0.01;

    private @Nullable FocusPackage focusPackage;
    private @Nullable EntityReference<LivingEntity> owner;
    private int duration;
    private @Nullable List<Identifier> effects;

    public EntityFocusCloud(EntityType<? extends EntityFocusCloud> type, Level level) {
        super(type, level);
    }

    public EntityFocusCloud(FocusPackage pack, LivingEntity caster, Trajectory trajectory, float radius, int durationSeconds) {
        super(TCEntities.FOCUS_CLOUD.get(), caster.level());
        this.focusPackage = pack;
        this.setPos(trajectory.source().x, trajectory.source().y, trajectory.source().z);
        this.setOwner(caster);
        this.setRadius(radius);
        this.setDuration(durationSeconds);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_RADIUS, DEFAULT_RADIUS);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = EntityReference.of(owner);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    public void setRadius(float radius) {
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_RADIUS, radius);
        }
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, CLOUD_HEIGHT);
    }

    @Override
    public void refreshDimensions() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        super.refreshDimensions();
        this.setPos(x, y, z);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (DATA_RADIUS.equals(accessor)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(accessor);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
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
        output.putInt("Age", this.tickCount);
        output.putInt("Duration", this.duration);
        output.putFloat("Radius", this.getRadius());
        EntityReference.store(this.owner, output, "OwnerUUID");
        FocusPackages.save(output, this.focusPackage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.tickCount = input.getIntOr("Age", 0);
        this.duration = input.getIntOr("Duration", 0);
        this.setRadius(input.getFloatOr("Radius", DEFAULT_RADIUS));
        this.owner = EntityReference.read(input, "OwnerUUID");
        this.focusPackage = FocusPackages.load(input);
    }

    @Override
    public void tick() {
        super.tick();
        float radius = this.getRadius();
        if (!this.level().isClientSide() && (this.tickCount > this.duration * TICKS_PER_SECOND || this.getOwner() == null)) {
            this.discard();
        }
        if (!this.isAlive()) {
            return;
        }
        if (this.level().isClientSide()) {
            this.clientParticles(radius);
        } else if (this.tickCount % APPLY_INTERVAL_TICKS == 0) {
            this.applyToSurroundings(radius);
        }
    }

    private void clientParticles(float radius) {
        if (this.effects == null) {
            this.effects = FocusPackages.effects(this.focusPackage);
        }
        if (this.effects.isEmpty()) {
            return;
        }
        for (int a = 0; a < radius; a++) {
            Identifier effectId = this.effects.get(this.random.nextInt(this.effects.size()));
            this.level().addParticle(TCParticles.colorOf(TCParticles.FOCUS_CLOUD, FocusEngine.color(effectId)), this.getX() + this.random.nextGaussian() * radius / 2.0 * PARTICLE_SPREAD_FACTOR,
                    this.getY() + this.random.nextGaussian() * radius / 2.0 * PARTICLE_SPREAD_FACTOR, this.getZ() + this.random.nextGaussian() * radius / 2.0 * PARTICLE_SPREAD_FACTOR,
                    this.random.nextGaussian() * PARTICLE_MOTION, this.random.nextGaussian() * PARTICLE_MOTION, this.random.nextGaussian() * PARTICLE_MOTION);
            if (FocusEngine.element(effectId) instanceof FocusEffect effect) {
                effect.impactParticles(this.level(),
                        new Vec3(this.getX() + this.random.nextGaussian() * radius / 2.0, this.getY() + this.random.nextGaussian() * radius / 2.0,
                                this.getZ() + this.random.nextGaussian() * radius / 2.0),
                        new Vec3(this.random.nextGaussian() * PARTICLE_MOTION, this.random.nextGaussian() * PARTICLE_MOTION, this.random.nextGaussian() * PARTICLE_MOTION));
            }
        }
    }

    private void applyToSurroundings(float radius) {
        long now = this.level().getGameTime();
        FocusCloudCooldowns cooldowns = this.level().getData(TCAttachments.FOCUS_CLOUD_COOLDOWNS);
        List<Trajectory> trajectories = new ArrayList<>();
        List<HitResult> targets = new ArrayList<>();
        for (Entity entity : this.level().getEntitiesOfClass(Entity.class, new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ()).inflate(radius),
                candidate -> candidate.getId() != this.getId())) {
            if (!entity.isAlive()) {
                continue;
            }
            if (entity instanceof EntityFocusCloud otherCloud) {
                Vec3 away = entity.position().subtract(this.position());
                entity.move(MoverType.SELF, away.scale(1.0 / CLOUD_PUSH_DIVISOR));
                otherCloud.moveTowardsClosestSpace(this.getX(), this.getY(), this.getZ());
            }
            if (entity instanceof LivingEntity && cooldowns.tryClaimEntity(entity.getId(), now, COOLDOWN_TICKS)) {
                EntityHitResult ray = new EntityHitResult(entity, entity.position().add(0.0, entity.getBbHeight() / 2.0F, 0.0));
                targets.add(ray);
                trajectories.add(new Trajectory(this.position(), ray.getLocation().subtract(this.position())));
            }
        }
        for (int a = 0; a < radius; a++) {
            Vec3 direction = new Vec3(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).normalize();
            BlockHitResult blockHit = this.level().clip(new ClipContext(this.position(), this.position().add(direction.scale(radius)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
            if (blockHit.getType() == HitResult.Type.BLOCK && cooldowns.tryClaimBlock(blockHit.getBlockPos().asLong(), now, COOLDOWN_TICKS)) {
                targets.add(blockHit);
                trajectories.add(new Trajectory(this.position(), direction));
            }
        }
        LivingEntity currentOwner = this.getOwner();
        if (!targets.isEmpty() && currentOwner != null && this.focusPackage != null) {
            FocusEngine.run(this.level(), this.focusPackage, currentOwner, new CastStreams(trajectories.toArray(new Trajectory[0]), targets.toArray(new HitResult[0])));
        }
    }
}
