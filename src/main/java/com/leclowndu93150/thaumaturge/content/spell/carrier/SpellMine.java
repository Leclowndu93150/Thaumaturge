package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public final class SpellMine extends ThrowableProjectile implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> ARMED =
            SynchedEntityData.defineId(SpellMine.class, EntityDataSerializers.BOOLEAN);
    private static final int LIFESPAN = 1200;
    private static final int ARM_DELAY = 40;
    private static final int SCAN_INTERVAL = 5;
    private static final double TRIGGER_RANGE = 1.0;
    private static final double GRAVITY = 0.01;
    private static final double EMBED_DAMPING = 0.25;
    private static final double THROW_SPEED = 0.3;
    private static final double SPARK_SPREAD = 0.1;
    private static final String TRAP_KEY = "trap";

    private final CarrierCharge charge = new CarrierCharge();
    private boolean allies;
    private int armedTick;
    private List<LivingEntity> victims = List.of();
    private int released;

    public SpellMine(EntityType<? extends SpellMine> type, Level level) {
        super(type, level);
    }

    public static void place(
            ServerLevel level, LivingEntity owner, CarrierPayload payload, SpellTarget origin, boolean allies) {
        SpellMine mine = new SpellMine(TTEntities.FOCUS_MINE.get(), level);
        mine.charge.arm(payload);
        mine.allies = allies;
        mine.setOwner(owner);
        mine.setPos(origin.position().x, origin.position().y, origin.position().z);
        mine.shoot(origin.direction().x, origin.direction().y, origin.direction().z, (float) THROW_SPEED, 0.0F);
        level.addFreshEntity(mine);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ARMED, false);
    }

    public boolean armed() {
        return entityData.get(ARMED);
    }

    public int color() {
        return charge.look().color();
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.writeLook(buffer);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.readLook(buffer);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        charge.save(output, registryAccess());
        TTNbt.store(output, TRAP_KEY, Trap.CODEC, registries, new Trap(armed(), allies));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        charge.load(input, registryAccess());
        Trap trap = TTNbt.read(input, TRAP_KEY, Trap.CODEC, registries).orElse(Trap.UNSET);
        entityData.set(ARMED, trap.armed());
        allies = trap.allies();
        armedTick = -ARM_DELAY;
    }

    private record Trap(boolean armed, boolean allies) {
        static final Trap UNSET = new Trap(false, false);
        static final Codec<Trap> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codec.BOOL.optionalFieldOf("armed", false).forGetter(Trap::armed),
                        Codec.BOOL.optionalFieldOf("allies", false).forGetter(Trap::allies))
                .apply(i, Trap::new));
    }

    @Override
    protected void onHit(HitResult hit) {
        if (!level().isClientSide() && getOwner() != null && !armed()) {
            entityData.set(ARMED, true);
            armedTick = tickCount;
        }
    }

    @Override
    public void tick() {
        super.tick();
        unstick();
        if (level() instanceof ServerLevel server) {
            serverTick(server);
        } else if (armed() && tickCount % SCAN_INTERVAL == 0) {
            glow();
        }
    }

    private void glow() {
        Vec3 jitter = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(SPARK_SPREAD);
        CarrierPayload.particle(level(), charge.look(), position().add(jitter), Vec3.ZERO);
    }

    private void unstick() {
        if (!level().noCollision(this)) {
            moveTowardsClosestSpace(getX(), getY(), getZ());
            setDeltaMovement(getDeltaMovement().scale(EMBED_DAMPING));
        }
    }

    private void serverTick(ServerLevel server) {
        Entity owner = getOwner();
        if (tickCount > LIFESPAN || owner == null || charge.isSpent()) {
            discard();
        } else if (!victims.isEmpty()) {
            release(server);
        } else if (armed() && tickCount - armedTick >= ARM_DELAY && tickCount % SCAN_INTERVAL == 0) {
            victims = SpellTargeting.livingWithin(server, position(), TRIGGER_RANGE, living -> triggers(owner, living));
            if (!victims.isEmpty()) {
                release(server);
            }
        }
    }

    private boolean triggers(Entity owner, LivingEntity living) {
        return allies == SpellTargeting.isAlly(owner, living);
    }

    private void release(ServerLevel server) {
        if (released >= victims.size() || charge.isSpent()) {
            discard();
            return;
        }
        LivingEntity victim = victims.get(released++);
        if (victim.isAlive()) {
            Vec3 aim = victim.getBoundingBox().getCenter();
            charge.resume(server, List.of(SpellTarget.entity(victim, aim.subtract(position()))));
        }
    }
}
