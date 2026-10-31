package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
    private static final String GRAVITY_KEY = "gravity";
    private static final String SPLASH_KEY = "splash";
    private static final String HOMING_KEY = "homing";
    private static final String BOUNCES_KEY = "bounces";
    private static final String PIERCE_KEY = "pierce";
    private static final int MAX_AGE = 1200;
    private static final double LAUNCH_OFFSET = 0.6;
    private static final float NO_DIVERGENCE = 0.0F;
    private static final int HOMING_INTERVAL = 5;
    private static final double HOMING_RANGE = 16.0;
    private static final double HOMING_CONE_COS = 0.5;
    private static final double MAX_TURN_PER_TICK = Math.toRadians(9.0);
    private static final double QUARRY_AIM_HEIGHT = 0.75;
    private static final double PARALLEL_EPSILON = 1.0E-6;
    private static final double BOUNCE_SPEED_KEPT = 0.9;
    private static final double BOUNCE_VERTICAL_KEPT = 0.85;
    private static final double BOUNCE_CLEARANCE = 0.05;
    private static final double BOUNCE_REST_SPEED = 0.05;
    private static final float BOUNCE_VOLUME = 0.4F;
    private static final float BOUNCE_PITCH = 1.3F;
    private static final double TRAIL_SPREAD = 0.01;

    private final CarrierCharge charge = new CarrierCharge();
    private double gravity;
    private float splash;
    private boolean homing;
    private int bounces;
    private int pierce;
    private final IntOpenHashSet pierced = new IntOpenHashSet();
    private @Nullable LivingEntity quarry;

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
        Vec3 start = origin.position().add(origin.direction().scale(LAUNCH_OFFSET));
        projectile.setPos(start.x, start.y, start.z);
        projectile.shoot(origin.direction().x, origin.direction().y, origin.direction().z, speed, NO_DIVERGENCE);
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
        super.addAdditionalSaveData(output);
        output.putInt(PIERCE_KEY, pierce);
        output.putInt(BOUNCES_KEY, bounces);
        output.putBoolean(HOMING_KEY, homing);
        output.putFloat(SPLASH_KEY, splash);
        output.putDouble(GRAVITY_KEY, gravity);
        charge.save(output, registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        super.readAdditionalSaveData(input);
        pierce = input.contains(PIERCE_KEY) ? input.getInt(PIERCE_KEY) : 0;
        bounces = input.contains(BOUNCES_KEY) ? input.getInt(BOUNCES_KEY) : 0;
        homing = input.contains(HOMING_KEY) && input.getBoolean(HOMING_KEY);
        splash = input.contains(SPLASH_KEY) ? input.getFloat(SPLASH_KEY) : 0.0F;
        gravity = input.contains(GRAVITY_KEY) ? input.getDouble(GRAVITY_KEY) : 0.0;
        charge.load(input, registryAccess());
    }

    @Override
    public void tick() {
        if (level() instanceof ServerLevel server) {
            if (!(getOwner() instanceof LivingEntity owner) || tickCount > MAX_AGE || charge.isSpent()) {
                discard();
                return;
            }
            if (homing) {
                steer(server, owner);
            }
        }
        super.tick();
        if (level().isClientSide()) {
            trail();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !pierced.contains(target.getId());
    }

    @Override
    protected void onHit(HitResult hit) {
        if (bounces > 0
                && hit instanceof BlockHitResult blockHit
                && blockHit.getType() == HitResult.Type.BLOCK
                && bounce(blockHit)) {
            return;
        }
        if (charge.isSpent() || !(level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 heading = getDeltaMovement().normalize();
        Entity struck = hit instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
        if (struck != null && pierce > 0) {
            pierce--;
            pierced.add(struck.getId());
            charge.resume(level, List.of(SpellTarget.entity(struck, heading)));
            return;
        }
        List<SpellTarget> targets = new ArrayList<>();
        targets.add(struck != null ? SpellTarget.entity(struck, heading) : SpellTarget.of(hit, heading));
        targets.addAll(splashTargets(level, LevelSpaces.toWorld(level, hit.getLocation()), struck));
        charge.resume(level, targets);
        discard();
    }

    private List<SpellTarget> splashTargets(ServerLevel level, Vec3 centre, @Nullable Entity struck) {
        CarrierPayload payload = charge.payload();
        if (!(splash > 0.0F) || payload == null) {
            return List.of();
        }
        double radius = splash + payload.continuation().state().get(SpellStats.RADIUS);
        List<SpellTarget> targets = new ArrayList<>();
        for (LivingEntity living :
                SpellTargeting.livingWithin(level, centre, radius, candidate -> candidate != struck)) {
            Vec3 outward = living.getBoundingBox().getCenter().subtract(centre);
            targets.add(SpellTarget.entity(living, outward));
        }
        return targets;
    }

    private boolean bounce(BlockHitResult hit) {
        Vec3 normal = LevelSpaces.faceNormal(level(), hit.getBlockPos(), hit.getDirection());
        Vec3 velocity = getDeltaMovement();
        Vec3 mirrored = velocity.subtract(normal.scale(2.0 * velocity.dot(normal)));
        Vec3 reflected = new Vec3(mirrored.x, mirrored.y * BOUNCE_VERTICAL_KEPT, mirrored.z).scale(BOUNCE_SPEED_KEPT);
        if (reflected.length() < BOUNCE_REST_SPEED) {
            bounces = 0;
            return false;
        }
        bounces--;
        setDeltaMovement(reflected);
        Vec3 clear = LevelSpaces.toWorld(level(), hit.getLocation()).add(normal.scale(BOUNCE_CLEARANCE));
        setPos(clear.x, clear.y, clear.z);
        hurtMarked = true;
        if (!level().isClientSide()) {
            level().playSound(
                            null,
                            clear.x,
                            clear.y,
                            clear.z,
                            SoundEvents.SLIME_JUMP_SMALL,
                            SoundSource.NEUTRAL,
                            BOUNCE_VOLUME,
                            BOUNCE_PITCH);
        }
        return true;
    }

    private void steer(ServerLevel server, LivingEntity owner) {
        if (quarry != null && !quarry.isAlive()) {
            quarry = null;
        }
        if (tickCount % HOMING_INTERVAL == 0) {
            refreshQuarry(server, owner, getDeltaMovement());
        }
        if (quarry != null) {
            bendToward(quarry, getDeltaMovement());
        }
    }

    private void bendToward(LivingEntity target, Vec3 velocity) {
        double speed = velocity.length();
        Vec3 aim = new Vec3(target.getX(), target.getY() + target.getBbHeight() * QUARRY_AIM_HEIGHT, target.getZ())
                .subtract(position());
        if (speed < PARALLEL_EPSILON || aim.lengthSqr() < PARALLEL_EPSILON) {
            return;
        }
        Vec3 heading = velocity.scale(1.0 / speed);
        Vec3 wanted = aim.normalize();
        double angle = Math.acos(Mth.clamp(heading.dot(wanted), -1.0, 1.0));
        if (angle <= MAX_TURN_PER_TICK) {
            setDeltaMovement(wanted.scale(speed));
            hurtMarked = true;
            return;
        }
        Vec3 pivot = heading.cross(wanted);
        if (pivot.lengthSqr() < PARALLEL_EPSILON) {
            return;
        }
        Vec3 sideways = pivot.normalize().cross(heading);
        Vec3 turned = heading.scale(Math.cos(MAX_TURN_PER_TICK)).add(sideways.scale(Math.sin(MAX_TURN_PER_TICK)));
        setDeltaMovement(turned.normalize().scale(speed));
        hurtMarked = true;
    }

    private void refreshQuarry(ServerLevel server, LivingEntity owner, Vec3 velocity) {
        if (quarry != null && !SpellTargeting.canSee(server, position(), quarry, this)) {
            quarry = null;
        }
        if (quarry == null) {
            quarry = acquire(server, owner, velocity).orElse(null);
        }
    }

    private Optional<LivingEntity> acquire(ServerLevel server, LivingEntity owner, Vec3 velocity) {
        Vec3 from = position();
        Vec3 axis = velocity.normalize();
        return SpellTargeting.nearest(server, from, HOMING_RANGE, candidate -> {
            if (candidate == owner || SpellTargeting.isAlly(owner, candidate)) {
                return false;
            }
            return SpellTargeting.withinCone(from, axis, candidate, HOMING_RANGE, HOMING_CONE_COS)
                    && SpellTargeting.canSee(server, from, candidate, this);
        });
    }

    private void trail() {
        Vec3 at = position();
        Vec3 jitter = new Vec3(
                random.nextGaussian() * TRAIL_SPREAD,
                random.nextGaussian() * TRAIL_SPREAD,
                random.nextGaussian() * TRAIL_SPREAD);
        CarrierPayload.particle(level(), charge.look(), at, jitter);
    }
}
