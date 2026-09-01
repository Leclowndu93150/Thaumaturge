package com.leclowndu93150.thaumaturge.content.taint.flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Lightweight bridge between TC4-style physical Flux and modern numerical aura Flux.
 *
 * <p>Physical Goo/Gas does not add Flux every tick. Instead, active physical Flux blocks report
 * their finite quanta here and establish a capped local aura-Flux floor. If the aura is cleaner
 * than that floor, {@code AuraTickHandler} slowly seeps it upward. Stale reports expire quickly,
 * so cleaning or moving the physical pollution also removes its sustained aura pressure.
 */
public final class PhysicalFluxAuraContamination {
    private static final long STALE_AFTER_TICKS = 80L;
    private static final float GOO_TARGET_PER_QUANTUM = 0.5F;
    private static final float GAS_TARGET_PER_QUANTUM = 0.25F;
    private static final float ABSOLUTE_TARGET_CAP = 30.0F;
    private static final float BASE_TARGET_CAP_RATIO = 0.30F;

    private static final Map<ResourceKey<Level>, Map<Long, Map<Long, Observation>>> OBSERVATIONS =
            new ConcurrentHashMap<>();

    private PhysicalFluxAuraContamination() {}

    private record Observation(int amount, boolean gas, long gameTime) {}

    public static void observeGoo(ServerLevel level, BlockPos pos, int amount) {
        observe(level, pos, amount, false);
    }

    public static void observeGas(ServerLevel level, BlockPos pos, int amount) {
        observe(level, pos, amount, true);
    }

    private static void observe(ServerLevel level, BlockPos pos, int amount, boolean gas) {
        if (amount <= 0) {
            return;
        }
        long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        OBSERVATIONS
                .computeIfAbsent(level.dimension(), ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkKey, ignored -> new ConcurrentHashMap<>())
                .put(
                        pos.asLong(),
                        new Observation(Math.min(PhysicalFlux.MAX_QUANTA, amount), gas, level.getGameTime()));
    }

    public static void forgetChunk(ServerLevel level, ChunkPos chunkPos) {
        Map<Long, Map<Long, Observation>> dimension = OBSERVATIONS.get(level.dimension());
        if (dimension == null) {
            return;
        }
        dimension.remove(chunkPos.toLong());
        if (dimension.isEmpty()) {
            OBSERVATIONS.remove(level.dimension(), dimension);
        }
    }

    /** Returns the sustained local Aura Flux target created by recently observed Goo/Gas. */
    public static float targetFlux(ServerLevel level, ChunkPos chunkPos, float auraBase) {
        Map<Long, Map<Long, Observation>> dimension = OBSERVATIONS.get(level.dimension());
        if (dimension == null) {
            return 0.0F;
        }
        long chunkKey = chunkPos.toLong();
        Map<Long, Observation> samples = dimension.get(chunkKey);
        if (samples == null || samples.isEmpty()) {
            return 0.0F;
        }

        long now = level.getGameTime();
        float target = 0.0F;
        for (Map.Entry<Long, Observation> entry : samples.entrySet()) {
            Observation observation = entry.getValue();
            if (now - observation.gameTime() > STALE_AFTER_TICKS) {
                samples.remove(entry.getKey(), observation);
                continue;
            }
            target += observation.amount() * (observation.gas() ? GAS_TARGET_PER_QUANTUM : GOO_TARGET_PER_QUANTUM);
        }

        if (samples.isEmpty()) {
            dimension.remove(chunkKey, samples);
        }
        if (dimension.isEmpty()) {
            OBSERVATIONS.remove(level.dimension(), dimension);
        }

        float cap = Math.min(ABSOLUTE_TARGET_CAP, Math.max(0.0F, auraBase) * BASE_TARGET_CAP_RATIO);
        return Math.min(target, cap);
    }
}
