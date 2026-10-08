package com.leclowndu93150.thaumaturge.content.research.pool;

import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class AspectPoolData {
    public static final MapCodec<AspectPoolData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.unboundedMap(LegacyIds.IDENTIFIER_CODEC, Codec.INT)
                            .fieldOf("pool")
                            .forGetter(d -> d.pool),
                    Codec.INT.optionalFieldOf("completed_notes", 0).forGetter(AspectPoolData::completedNotes))
            .apply(instance, AspectPoolData::of));

    public static final StreamCodec<RegistryFriendlyByteBuf, AspectPoolData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(LinkedHashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT),
            d -> d.pool,
            ByteBufCodecs.VAR_INT,
            AspectPoolData::completedNotes,
            AspectPoolData::of);

    private final LinkedHashMap<ResourceLocation, Integer> pool;
    private int completedNotes;
    private long lastGrantSoundTime = Long.MIN_VALUE;
    private boolean syncPending;

    public AspectPoolData() {
        this.pool = new LinkedHashMap<>();
    }

    private AspectPoolData(LinkedHashMap<ResourceLocation, Integer> pool, int completedNotes) {
        this.pool = pool;
        this.completedNotes = completedNotes;
    }

    private static AspectPoolData of(Map<ResourceLocation, Integer> pool, int completedNotes) {
        return new AspectPoolData(new LinkedHashMap<>(pool), completedNotes);
    }

    public Map<ResourceLocation, Integer> pool() {
        return pool;
    }

    public boolean isDiscovered(ResourceLocation aspect) {
        return pool.containsKey(aspect);
    }

    public int amount(ResourceLocation aspect) {
        return pool.getOrDefault(aspect, 0);
    }

    public void discover(ResourceLocation aspect) {
        pool.putIfAbsent(aspect, 0);
    }

    public void add(ResourceLocation aspect, int amount) {
        if (amount >= 0) {
            pool.merge(aspect, amount, Integer::sum);
        } else {
            int current = amount(aspect);
            if (current > 0) {
                pool.put(aspect, Math.max(0, current + amount));
            }
        }
    }

    public boolean tryClaimGrantSound(long gameTime) {
        if (lastGrantSoundTime == gameTime) {
            return false;
        }
        lastGrantSoundTime = gameTime;
        return true;
    }

    public boolean isEmpty() {
        return pool.isEmpty();
    }

    public int completedNotes() {
        return completedNotes;
    }

    public void incrementCompletedNotes() {
        completedNotes++;
    }

    public static AspectPoolData snapshotOf(AspectPoolData source) {
        AspectPoolData snapshot = new AspectPoolData();
        snapshot.copyFrom(source);
        return snapshot;
    }

    public void mergeDiscoveriesFrom(AspectPoolData other) {
        for (ResourceLocation aspect : other.pool.keySet()) {
            discover(aspect);
        }
    }

    public void copyFrom(AspectPoolData other) {
        this.pool.clear();
        this.pool.putAll(other.pool);
        this.completedNotes = other.completedNotes;
    }

    public void markSyncPending() {
        syncPending = true;
    }

    public boolean takeSyncPending() {
        boolean pending = syncPending;
        syncPending = false;
        return pending;
    }
}
