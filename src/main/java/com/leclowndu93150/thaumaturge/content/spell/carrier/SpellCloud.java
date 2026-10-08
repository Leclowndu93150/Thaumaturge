package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SpellCloud extends AbstractSpellCarrier {
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(SpellCloud.class, EntityDataSerializers.FLOAT);
    private static final float HEIGHT = 0.5F;
    private static final int PULSE_INTERVAL = 5;
    private static final int REHIT_COOLDOWN = 40;
    private static final double MIST_SPREAD = 0.45;
    private static final double MIST_DRIFT = 0.01;
    private static final int GLINT_ONE_IN = 3;

    private final Map<Integer, Long> cooldowns = new HashMap<>();
    private final Map<BlockPos, Long> blockCooldowns = new HashMap<>();

    public SpellCloud(EntityType<? extends SpellCloud> type, Level level) {
        super(type, level);
    }

    public static void spawn(
            ServerLevel level, LivingEntity owner, CarrierPayload payload, Vec3 at, float radius, int lifetime) {
        SpellCloud cloud = new SpellCloud(TTEntities.FOCUS_CLOUD.get(), level);
        cloud.bind(owner, payload, lifetime);
        cloud.entityData.set(RADIUS, radius);
        cloud.setPos(at.x, at.y - HEIGHT / 2.0, at.z);
        level.addFreshEntity(cloud);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 1.0F);
    }

    public float radius() {
        return entityData.get(RADIUS);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(radius() * 2.0F, HEIGHT);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (RADIUS.equals(accessor)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(accessor);
    }

    @Override
    protected void saveCarrierData(CompoundTag output) {
        output.putFloat("radius", radius());
    }

    @Override
    protected void loadCarrierData(CompoundTag input) {
        entityData.set(RADIUS, (input.contains("radius") ? input.getFloat("radius") : 1.0F));
    }

    @Override
    public void tick() {
        super.tick();
        float radius = radius();
        if (level().isClientSide()) {
            mist(radius);
            return;
        }
        if (expired()) {
            discard();
            return;
        }
        if (tickCount % PULSE_INTERVAL == 0 && level() instanceof ServerLevel level) {
            pulse(level, radius);
        }
    }

    private void pulse(ServerLevel level, float radius) {
        long now = level.getGameTime();
        cooldowns.values().removeIf(until -> until <= now);
        blockCooldowns.values().removeIf(until -> until <= now);
        Vec3 centre = position().add(0.0, HEIGHT / 2.0, 0.0);
        List<SpellTarget> targets = new ArrayList<>();
        for (LivingEntity living :
                SpellTargeting.livingWithin(level, centre, radius, living -> !cooldowns.containsKey(living.getId()))) {
            cooldowns.put(living.getId(), now + REHIT_COOLDOWN);
            targets.add(SpellTarget.entity(
                    living, living.getBoundingBox().getCenter().subtract(centre)));
        }
        for (int ray = 0; ray < Math.max(1, Math.round(radius)); ray++) {
            Vec3 direction = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
            BlockHitResult hit = level.clip(new ClipContext(
                    centre,
                    centre.add(direction.scale(radius)),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    this));
            BlockPos struck = hit.getBlockPos().immutable();
            if (hit.getType() == HitResult.Type.BLOCK && !blockCooldowns.containsKey(struck)) {
                blockCooldowns.put(struck, now + REHIT_COOLDOWN);
                targets.add(SpellTarget.of(hit, direction));
            }
        }
        if (!targets.isEmpty()) {
            charge.resume(level, targets);
        }
    }

    private void mist(float radius) {
        for (int puff = 0; puff < Math.max(1, Math.round(radius)); puff++) {
            double x = getX() + random.nextGaussian() * radius * MIST_SPREAD;
            double y = getY() + HEIGHT / 2.0 + random.nextGaussian() * radius * MIST_SPREAD / 2.0;
            double z = getZ() + random.nextGaussian() * radius * MIST_SPREAD;
            Vec3 drift = new Vec3(
                    random.nextGaussian() * MIST_DRIFT,
                    random.nextGaussian() * MIST_DRIFT,
                    random.nextGaussian() * MIST_DRIFT);
            level().addParticle(
                            TTParticles.colorOf(
                                    TTParticles.FOCUS_CLOUD, charge.look().color()),
                            x,
                            y,
                            z,
                            drift.x,
                            drift.y,
                            drift.z);
            if (random.nextInt(GLINT_ONE_IN) == 0) {
                CarrierPayload.particle(level(), charge.look(), new Vec3(x, y, z), drift);
            }
        }
    }
}
