package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class SpellProjectile extends ThrowableProjectile implements IEntityWithComplexSpawn {
    private static final int LIFESPAN = 1200;
    private static final double SPAWN_AHEAD = 0.6;
    private static final int SEEK_INTERVAL = 5;
    private static final double SEEK_RANGE = 16.0;
    private static final double SEEK_CONE_COS = 0.5;
    private static final double SEEK_STEER = 0.25;
    private static final double BOUNCE_DAMPING = 0.8;
    private static final double MIN_BOUNCE_SPEED = 0.15;
    private static final float BOUNCE_VOLUME = 0.25F;
    private static final double TRAIL_JITTER = 0.02;
    private static final double BOUNCE_LIFT = 0.05;

    private final CarrierCharge charge = new CarrierCharge();
    private double gravity;
    private float splash;
    private boolean homing;
    private int bounces;
    private int pierce;
    private final Set<Integer> pierced = new HashSet<>();
    private @Nullable Entity quarry;

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    public static void launch(
            ServerLevel level,
            LivingEntity owner,
            CarrierPayload payload,
            SpellTarget origin,
            float speed,
            double gravity,
            float splash) {
        SpellProjectile projectile = new SpellProjectile(TTEntities.FOCUS_PROJECTILE.get(), level);
        projectile.charge.arm(payload);
        projectile.gravity = gravity;
        projectile.splash = splash;
        projectile.homing = payload.continuation().state().has(SpellStats.HOMING);
        projectile.bounces = Math.round(payload.continuation().state().get(SpellStats.BOUNCE));
        projectile.pierce = Math.round(payload.continuation().state().get(SpellStats.PIERCE));
        projectile.setOwner(owner);
        Vec3 start = origin.position().add(origin.direction().scale(SPAWN_AHEAD));
        projectile.setPos(start.x, start.y, start.z);
        projectile.shoot(origin.direction().x, origin.direction().y, origin.direction().z, speed, 0.0F);
        level.addFreshEntity(projectile);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected double getDefaultGravity() {
        return gravity;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.writeLook(buffer);
        buffer.writeDouble(gravity);
        buffer.writeBoolean(homing);
        buffer.writeVarInt(bounces);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.readLook(buffer);
        gravity = buffer.readDouble();
        homing = buffer.readBoolean();
        bounces = buffer.readVarInt();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        charge.save(output, registryAccess());
        output.putDouble("gravity", gravity);
        output.putFloat("splash", splash);
        output.putBoolean("homing", homing);
        output.putInt("bounces", bounces);
        output.putInt("pierce", pierce);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        charge.load(input, registryAccess());
        gravity = (input.contains("gravity") ? input.getDouble("gravity") : 0.0);
        splash = (input.contains("splash") ? input.getFloat("splash") : 0.0F);
        homing = (input.contains("homing") ? input.getBoolean("homing") : false);
        bounces = (input.contains("bounces") ? input.getInt("bounces") : 0);
        pierce = (input.contains("pierce") ? input.getInt("pierce") : 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            trail();
            return;
        }
        if (tickCount > LIFESPAN || getOwner() == null || charge.isSpent()) {
            discard();
            return;
        }
        if (homing) {
            steer();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !pierced.contains(target.getId());
    }

    @Override
    protected void onHit(HitResult hit) {
        if (hit instanceof BlockHitResult blockHit && bounces > 0) {
            bounce(blockHit);
            return;
        }
        if (!(level() instanceof ServerLevel level) || charge.isSpent()) {
            return;
        }
        Vec3 heading = getDeltaMovement().normalize();
        if (hit instanceof EntityHitResult entityHit && pierce > 0) {
            pierce--;
            pierced.add(entityHit.getEntity().getId());
            charge.resume(level, List.of(SpellTarget.entity(entityHit.getEntity(), heading)));
            return;
        }
        charge.resume(level, impact(level, hit, heading));
        discard();
    }

    private List<SpellTarget> impact(ServerLevel level, HitResult hit, Vec3 heading) {
        List<SpellTarget> targets = new ArrayList<>();
        Entity struck = hit instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
        targets.add(struck != null ? SpellTarget.entity(struck, heading) : SpellTarget.of(hit, heading));
        CarrierPayload payload = charge.payload();
        float radius = splash > 0.0F && payload != null
                ? splash + payload.continuation().state().get(SpellStats.RADIUS)
                : 0.0F;
        if (radius > 0.0F) {
            Vec3 centre = hit.getLocation();
            for (LivingEntity living : SpellTargeting.livingWithin(level, centre, radius, living -> living != struck)) {
                targets.add(SpellTarget.entity(
                        living, living.getBoundingBox().getCenter().subtract(centre)));
            }
        }
        return targets;
    }

    private void bounce(BlockHitResult hit) {
        Direction face = hit.getDirection();
        Vec3 motion = getDeltaMovement();
        Vec3 reflected = new Vec3(
                        face.getAxis() == Direction.Axis.X ? -motion.x : motion.x,
                        face.getAxis() == Direction.Axis.Y ? -motion.y : motion.y,
                        face.getAxis() == Direction.Axis.Z ? -motion.z : motion.z)
                .scale(BOUNCE_DAMPING);
        setDeltaMovement(reflected);
        setPos(hit.getLocation()
                .add(face.getStepX() * BOUNCE_LIFT, face.getStepY() * BOUNCE_LIFT, face.getStepZ() * BOUNCE_LIFT));
        bounces--;
        if (!level().isClientSide()) {
            playSound(SoundEvents.SLIME_BLOCK_HIT, BOUNCE_VOLUME, 1.5F);
            if (reflected.length() < MIN_BOUNCE_SPEED) {
                bounces = 0;
            }
        }
    }

    private void steer() {
        if (quarry == null
                || !quarry.isAlive()
                || tickCount % SEEK_INTERVAL == 0 && !SpellTargeting.canSee(level(), position(), quarry, this)) {
            quarry = tickCount % SEEK_INTERVAL == 0 ? seek() : null;
        }
        if (quarry == null) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        double speed = motion.length();
        Vec3 toward = quarry.getBoundingBox().getCenter().subtract(position()).normalize();
        setDeltaMovement(
                motion.normalize().add(toward.scale(SEEK_STEER)).normalize().scale(speed));
        hurtMarked = true;
    }

    private @Nullable Entity seek() {
        Vec3 heading = getDeltaMovement().normalize();
        Entity owner = getOwner();
        Optional<LivingEntity> found = SpellTargeting.nearest(
                level(),
                position(),
                SEEK_RANGE,
                living -> living != owner
                        && !SpellTargeting.isAlly(owner, living)
                        && SpellTargeting.withinCone(position(), heading, living, SEEK_RANGE, SEEK_CONE_COS)
                        && SpellTargeting.canSee(level(), position(), living, this));
        return found.orElse(null);
    }

    private void trail() {
        Vec3 at = position();
        Vec3 jitter = new Vec3(
                random.nextGaussian() * TRAIL_JITTER,
                random.nextGaussian() * TRAIL_JITTER,
                random.nextGaussian() * TRAIL_JITTER);
        CarrierPayload.particle(level(), charge.look(), at, jitter);
    }
}
