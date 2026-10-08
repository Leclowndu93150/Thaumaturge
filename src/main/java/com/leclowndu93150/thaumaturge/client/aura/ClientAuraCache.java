package com.leclowndu93150.thaumaturge.client.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
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
        ENTRIES.put(pos.toLong(), snapshot);
        latest = snapshot;
    }

    public static @Nullable Snapshot get(ChunkPos pos) {
        return ENTRIES.get(pos.toLong());
    }

    public static @Nullable Snapshot latest() {
        return latest;
    }

    public static boolean shouldRequest(ChunkPos pos) {
        long key = pos.toLong();
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

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        currentTick++;
    }

    public static void clear() {
        ENTRIES.clear();
        REQUESTS.clear();
        latest = null;
        currentTick = 0L;
    }

    public record Snapshot(short base, float vis, float flux, long tick) {}
}
