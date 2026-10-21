package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SpellLook;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class SpellCloud extends AbstractSpellCarrier {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(SpellCloud.class, EntityDataSerializers.FLOAT);
    private static final String RADIUS_KEY = "radius";
    private static final float DEFAULT_RADIUS = 1.0F;
    private static final float HEIGHT = 0.5F;
    private static final double HALF_HEIGHT = 0.25;
    private static final int SAMPLES_PER_RADIUS = 3;
    private static final int PULSE_INTERVAL = 5;
    private static final long REHIT_COOLDOWN = 40L;
    private static final double PUFF_SPREAD = 0.45;
    private static final double PUFF_HEIGHT_SPREAD = 0.225;
    private static final double PUFF_DRIFT = 0.01;
    private static final int LOOK_PUFF_ODDS = 3;
    private static final int MAX_COOLDOWNS = 4096;

    private final Map<UUID, Long> entityCooldowns = new HashMap<>();
    private final Map<Long, Long> blockCooldowns = new HashMap<>();

    private record EntityCooldown(UUID target, long until) {
        private static final Codec<EntityCooldown> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(UUIDUtil.CODEC.fieldOf("target").forGetter(EntityCooldown::target), Codec.LONG.fieldOf("until").forGetter(EntityCooldown::until)).apply(instance, EntityCooldown::new));
    }

    private record BlockCooldown(long target, long until) {
        private static final Codec<BlockCooldown> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codec.LONG.fieldOf("target").forGetter(BlockCooldown::target), Codec.LONG.fieldOf("until").forGetter(BlockCooldown::until)).apply(instance, BlockCooldown::new));
    }

    public SpellCloud(EntityType<? extends SpellCloud> type, Level level) {
        super(type, level);
    }

    public static void spawn(ServerLevel level, LivingEntity owner, CarrierPayload payload, Vec3 at, float radius, int lifetimeTicks) {
        SpellCloud cloud = new SpellCloud(TTEntities.FOCUS_CLOUD.get(), level);
        cloud.bind(owner, payload, lifetimeTicks);
        cloud.entityData.set(RADIUS, radius);
        cloud.setPos(at.x, at.y - HALF_HEIGHT, at.z);
        level.addFreshEntity(cloud);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, DEFAULT_RADIUS);
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
    protected void saveCarrierData(ValueOutput output) {
        output.putFloat(RADIUS_KEY, radius());
        long now = level().getGameTime();
        output.store("entity_cooldowns", EntityCooldown.CODEC.listOf(0, MAX_COOLDOWNS),
                entityCooldowns.entrySet().stream().filter(entry -> entry.getValue() > now).map(entry -> new EntityCooldown(entry.getKey(), entry.getValue())).toList());
        output.store("block_cooldowns", BlockCooldown.CODEC.listOf(0, MAX_COOLDOWNS),
                blockCooldowns.entrySet().stream().filter(entry -> entry.getValue() > now).map(entry -> new BlockCooldown(entry.getKey(), entry.getValue())).toList());
    }

    @Override
    protected void loadCarrierData(ValueInput input) {
        entityData.set(RADIUS, input.getFloatOr(RADIUS_KEY, DEFAULT_RADIUS));
        entityCooldowns.clear();
        blockCooldowns.clear();
        long now = level().getGameTime();
        for (EntityCooldown cooldown : input.read("entity_cooldowns", EntityCooldown.CODEC.listOf(0, MAX_COOLDOWNS)).orElse(List.of())) {
            if (cooldown.until() > now) {
                entityCooldowns.merge(cooldown.target(), Math.min(cooldown.until(), now + REHIT_COOLDOWN), Math::max);
            }
        }
        for (BlockCooldown cooldown : input.read("block_cooldowns", BlockCooldown.CODEC.listOf(0, MAX_COOLDOWNS)).orElse(List.of())) {
            if (cooldown.until() > now) {
                blockCooldowns.merge(cooldown.target(), Math.min(cooldown.until(), now + REHIT_COOLDOWN), Math::max);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            if (expired()) {
                discard();
            } else if (tickCount % PULSE_INTERVAL == 0) {
                pulse(server);
            }
        } else {
            emitPuffs();
        }
    }

    private void pulse(ServerLevel server) {
        long now = server.getGameTime();
        entityCooldowns.values().removeIf(until -> until <= now);
        blockCooldowns.values().removeIf(until -> until <= now);
        Vec3 centre = position().add(0.0, HALF_HEIGHT, 0.0);
        float radius = radius();
        List<SpellTarget> targets = new ArrayList<>(gatherEntityTargets(server, centre, radius, now));
        targets.addAll(gatherBlockTargets(server, centre, radius, now));
        if (!targets.isEmpty()) {
            charge.resume(server, targets);
        }
    }

    private List<SpellTarget> gatherEntityTargets(ServerLevel server, Vec3 centre, float radius, long now) {
        List<LivingEntity> fresh = SpellTargeting.livingWithin(server, centre, radius, candidate -> !entityCooldowns.containsKey(candidate.getUUID()));
        List<SpellTarget> targets = new ArrayList<>(fresh.size());
        for (LivingEntity living : fresh) {
            if (entityCooldowns.size() >= MAX_COOLDOWNS) {
                break;
            }
            entityCooldowns.put(living.getUUID(), now + REHIT_COOLDOWN);
            targets.add(aimAt(living, centre));
        }
        return targets;
    }

    private static SpellTarget aimAt(LivingEntity living, Vec3 centre) {
        return SpellTarget.entity(living, living.getBoundingBox().getCenter().subtract(centre));
    }

    private List<SpellTarget> gatherBlockTargets(ServerLevel server, Vec3 centre, float radius, long now) {
        int samples = SAMPLES_PER_RADIUS * Math.max(1, Math.round(radius));
        List<SpellTarget> targets = new ArrayList<>(samples);
        while (samples-- > 0) {
            SpellTarget hit = probe(server, centre, radius, now);
            if (hit != null) {
                targets.add(hit);
            }
        }
        return targets;
    }

    private @Nullable SpellTarget probe(ServerLevel server, Vec3 centre, float radius, long now) {
        Vec3 offset = randomPointInSphere().scale(radius);
        Vec3 landing = centre.add(offset);
        BlockPos cell = BlockPos.containing(landing);
        long key = cell.asLong();
        if (blockCooldowns.size() >= MAX_COOLDOWNS || blockCooldowns.containsKey(key) || server.getBlockState(cell).isAir()) {
            return null;
        }
        blockCooldowns.put(key, now + REHIT_COOLDOWN);
        Direction facing = Direction.getApproximateNearest(offset).getOpposite();
        return SpellTarget.of(new BlockHitResult(landing, facing, cell, false), offset);
    }

    private double signedUnit() {
        return random.nextDouble() * 2.0 - 1.0;
    }

    private Vec3 randomPointInSphere() {
        Vec3 candidate = new Vec3(signedUnit(), signedUnit(), signedUnit());
        while (candidate.lengthSqr() > 1.0) {
            candidate = new Vec3(signedUnit(), signedUnit(), signedUnit());
        }
        return candidate;
    }

    private double gaussianSpread(double extent, double factor) {
        return random.nextGaussian() * extent * factor;
    }

    private void emitPuffs() {
        float radius = radius();
        SpellLook look = charge.look();
        ColorParticleOption tint = TTParticles.colorOf(TTParticles.FOCUS_CLOUD, ARGB.opaque(look.color()));
        for (int remaining = Math.max(1, Math.round(radius)); remaining > 0; remaining--) {
            double px = getX() + gaussianSpread(radius, PUFF_SPREAD);
            double py = getY() + HALF_HEIGHT + gaussianSpread(radius, PUFF_HEIGHT_SPREAD);
            double pz = getZ() + gaussianSpread(radius, PUFF_SPREAD);
            Vec3 drift = new Vec3(gaussianSpread(1.0, PUFF_DRIFT), gaussianSpread(1.0, PUFF_DRIFT), gaussianSpread(1.0, PUFF_DRIFT));
            level().addParticle(tint, px, py, pz, drift.x, drift.y, drift.z);
            if (random.nextInt(LOOK_PUFF_ODDS) == 0) {
                CarrierPayload.particle(level(), look, new Vec3(px, py, pz), drift);
            }
        }
    }
}
