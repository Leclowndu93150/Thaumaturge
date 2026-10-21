package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public final class SpellProjectile extends ThrowableProjectile implements IEntityWithComplexSpawn {
    private static final String GRAVITY_KEY = "gravity";
    private static final String SPLASH_KEY = "splash";
    private static final String HOMING_KEY = "homing";
    private static final String BOUNCES_KEY = "bounces";
    private static final String PIERCE_KEY = "pierce";
    private static final int MAX_AGE = 1200;
    private static final double LAUNCH_OFFSET = 0.6;
    private static final float NO_DIVERGENCE = 0.0F;
    private static final double BOUNCE_DAMPING = 0.8;
    private static final double BOUNCE_CLEARANCE = 0.05;
    private static final double BOUNCE_REST_SPEED = 0.15;
    private static final float BOUNCE_VOLUME = 0.25F;
    private static final float BOUNCE_PITCH = 1.5F;
    private static final int HOMING_INTERVAL = 5;
    private static final double HOMING_RANGE = 16.0;
    private static final double HOMING_CONE_COS = 0.5;
    private static final double HOMING_PULL = 0.25;
    private static final double TRAIL_SPREAD = 0.02;

    private final CarrierCharge charge = new CarrierCharge();
    private final Set<UUID> pierced = new HashSet<>();
    private double gravity;
    private float splash;
    private boolean homing;
    private int bounces;
    private int pierce;
    private @Nullable LivingEntity quarry;

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    public static void launch(ServerLevel level, LivingEntity owner, CarrierPayload payload, SpellTarget origin, float speed, double gravity, float splash) {
        SpellProjectile projectile = new SpellProjectile(TTEntities.FOCUS_PROJECTILE.get(), level);
        SpellState state = payload.continuation().state();
        projectile.charge.arm(level, payload);
        projectile.setOwner(owner);
        projectile.gravity = gravity;
        projectile.splash = splash;
        projectile.homing = state.has(SpellStats.HOMING);
        projectile.bounces = Math.round(state.get(SpellStats.BOUNCE));
        projectile.pierce = Math.round(state.get(SpellStats.PIERCE));
        Vec3 direction = origin.direction();
        Vec3 start = origin.position().add(direction.scale(LAUNCH_OFFSET));
        projectile.setPos(start.x, start.y, start.z);
        projectile.shoot(direction.x, direction.y, direction.z, speed, NO_DIVERGENCE);
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
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(PIERCE_KEY, pierce);
        output.putInt(BOUNCES_KEY, bounces);
        output.putBoolean(HOMING_KEY, homing);
        output.putFloat(SPLASH_KEY, splash);
        output.putDouble(GRAVITY_KEY, gravity);
        output.putInt("age", tickCount);
        output.store("pierced", UUIDUtil.CODEC.listOf(0, MAX_AGE), List.copyOf(pierced));
        charge.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        pierce = input.getIntOr(PIERCE_KEY, 0);
        bounces = input.getIntOr(BOUNCES_KEY, 0);
        homing = input.getBooleanOr(HOMING_KEY, false);
        splash = input.getFloatOr(SPLASH_KEY, 0.0F);
        gravity = input.getDoubleOr(GRAVITY_KEY, 0.0);
        tickCount = input.getIntOr("age", 0);
        pierced.clear();
        pierced.addAll(input.read("pierced", UUIDUtil.CODEC.listOf(0, MAX_AGE)).orElse(List.of()));
        charge.load(input);
    }

    @Override
    public void tick() {
        if (level() instanceof ServerLevel server) {
            if (!(getOwner() instanceof LivingEntity master) || tickCount > MAX_AGE || !charge.valid(server)) {
                discard();
                return;
            }
            if (homing) {
                steer(server, master);
            }
        }
        super.tick();
        if (level().isClientSide()) {
            emitTrail();
        }
    }

    private void emitTrail() {
        double dx = random.nextGaussian() * TRAIL_SPREAD;
        double dy = random.nextGaussian() * TRAIL_SPREAD;
        double dz = random.nextGaussian() * TRAIL_SPREAD;
        CarrierPayload.particle(level(), charge.look(), position(), new Vec3(dx, dy, dz));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !pierced.contains(entity.getUUID());
    }

    @Override
    protected void onHit(HitResult result) {
        if (bounces > 0 && result instanceof BlockHitResult block && block.getType() == HitResult.Type.BLOCK) {
            bounce(block);
            return;
        }
        if (charge.isSpent() || !(level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 heading = getDeltaMovement().normalize();
        Entity struck = result instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
        if (struck != null && pierce > 0) {
            pierce--;
            pierced.add(struck.getUUID());
            charge.resume(server, List.of(SpellTarget.entity(struck, heading)));
            return;
        }
        List<SpellTarget> targets = new ArrayList<>();
        targets.add(struck != null ? SpellTarget.entity(struck, heading) : SpellTarget.of(result, heading));
        targets.addAll(splashTargets(server, result.getLocation(), struck));
        charge.resume(server, targets);
        discard();
    }

    private List<SpellTarget> splashTargets(ServerLevel server, Vec3 centre, @Nullable Entity struck) {
        CarrierPayload payload = charge.payload();
        if (!(splash > 0.0F) || payload == null) {
            return List.of();
        }
        double radius = splash + payload.continuation().state().get(SpellStats.RADIUS);
        List<SpellTarget> caught = new ArrayList<>();
        for (LivingEntity living : SpellTargeting.livingWithin(server, centre, radius, candidate -> candidate != struck)) {
            Vec3 outward = living.getBoundingBox().getCenter().subtract(centre);
            caught.add(SpellTarget.entity(living, outward));
        }
        return caught;
    }

    private void bounce(BlockHitResult hit) {
        Direction face = hit.getDirection();
        Direction.Axis axis = face.getAxis();
        Vec3 incoming = getDeltaMovement();
        double along = incoming.get(axis);
        Vec3 outgoing = incoming.with(axis, along - 2.0 * along).scale(BOUNCE_DAMPING);
        Vec3 resting = hit.getLocation().relative(face, BOUNCE_CLEARANCE);
        setPos(resting.x, resting.y, resting.z);
        setDeltaMovement(outgoing);
        bounces--;
        if (level() instanceof ServerLevel server) {
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.SLIME_BLOCK_HIT, SoundSource.NEUTRAL, BOUNCE_VOLUME, BOUNCE_PITCH);
            if (outgoing.length() < BOUNCE_REST_SPEED) {
                bounces = 0;
            }
            hurtMarked = true;
        }
    }

    private void steer(ServerLevel server, LivingEntity master) {
        Vec3 velocity = getDeltaMovement();
        if (velocity.lengthSqr() == 0.0) {
            return;
        }
        if (quarry != null && !quarry.isAlive()) {
            quarry = null;
        }
        if (tickCount % HOMING_INTERVAL == 0) {
            refreshQuarry(server, master, velocity);
        }
        if (quarry != null) {
            bendToward(quarry.getBoundingBox().getCenter(), velocity);
        }
    }

    private void refreshQuarry(ServerLevel server, LivingEntity master, Vec3 velocity) {
        if (quarry != null && !SpellTargeting.canSee(server, position(), quarry, this)) {
            quarry = null;
        }
        if (quarry == null) {
            quarry = acquire(server, master, velocity).orElse(null);
        }
    }

    private void bendToward(Vec3 goal, Vec3 velocity) {
        Vec3 pull = goal.subtract(position()).normalize().scale(HOMING_PULL);
        Vec3 heading = velocity.normalize().add(pull).normalize();
        setDeltaMovement(heading.scale(velocity.length()));
        hurtMarked = true;
    }

    private Optional<LivingEntity> acquire(ServerLevel server, LivingEntity master, Vec3 velocity) {
        Vec3 from = position();
        Vec3 axis = velocity.normalize();
        return SpellTargeting.nearest(server, from, HOMING_RANGE, candidate -> {
            if (candidate == master || SpellTargeting.isAlly(master, candidate)) {
                return false;
            }
            return SpellTargeting.withinCone(from, axis, candidate, HOMING_RANGE, HOMING_CONE_COS) && SpellTargeting.canSee(server, from, candidate, this);
        });
    }
}
