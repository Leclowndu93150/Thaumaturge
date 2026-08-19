package com.leclowndu93150.thaumaturge.client.aura;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public final class ClientAuraCache {
    private static final Map<Long, Snapshot> ENTRIES = new ConcurrentHashMap<>();
    private static final Map<Long, Long> REQUESTS = new ConcurrentHashMap<>();
    private static final long STALE_TICKS = 60L;
    private static final long REQUEST_INTERVAL_TICKS = 20L;
    private static long currentTick;
    private static volatile @Nullable Snapshot latest;

    private ClientAuraCache() {}

    public static void put(ChunkPos pos, short base, float vis, float flux) {
        Snapshot snapshot = new Snapshot(base, vis, flux, currentTick);
        ENTRIES.put(ChunkPos.pack(pos.x(), pos.z()), snapshot);
        latest = snapshot;
    }

    public static @Nullable Snapshot get(ChunkPos pos) {
        return ENTRIES.get(ChunkPos.pack(pos.x(), pos.z()));
    }

    public static @Nullable Snapshot latest() {
        return latest;
    }

    public static boolean shouldRequest(ChunkPos pos) {
        long key = ChunkPos.pack(pos.x(), pos.z());
        Snapshot snap = ENTRIES.get(key);
        if (snap != null && currentTick - snap.tick() <= STALE_TICKS) {
            return false;
        }
        Long lastRequest = REQUESTS.get(key);
        if (lastRequest != null && currentTick - lastRequest < REQUEST_INTERVAL_TICKS) {
            return false;
        }
        REQUESTS.put(key, currentTick);
        return true;
    }

    public static void tick() {
        currentTick++;
    }

    public static void clear() {
        ENTRIES.clear();
        REQUESTS.clear();
        latest = null;
        currentTick = 0L;
    }

    public record Snapshot(short base, float vis, float flux, long tick) {
    }
}
