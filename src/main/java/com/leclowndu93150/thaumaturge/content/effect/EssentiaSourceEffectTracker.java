package com.leclowndu93150.thaumaturge.content.effect;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public final class EssentiaSourceEffectTracker {
    private static final ConcurrentHashMap<ResourceKey<Level>, ConcurrentHashMap<String, PendingPulse>> PENDING = new ConcurrentHashMap<>();
    private static final int FLUSH_TICKS = 1;

    private EssentiaSourceEffectTracker() {}

    public static void dispatch(ServerLevel level, Vec3 from, Vec3 to, int color, int typeTag, int count, float scale, int extend, double my) {
        BlockPos bpFrom = BlockPos.containing(from);
        BlockPos bpTo = BlockPos.containing(to);
        String key = bpFrom.asLong() + ":" + bpTo.asLong() + ":" + color;
        ConcurrentHashMap<String, PendingPulse> map = PENDING.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        PendingPulse existing = map.get(key);
        if (existing != null) {
            existing.tickStamp = level.getGameTime();
            existing.extend = Math.max(existing.extend, extend);
            existing.count = count;
            existing.my = my;
            return;
        }
        map.put(key, new PendingPulse(from, to, color, typeTag, count, scale, extend, my, level.getGameTime()));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) {
            return;
        }
        ConcurrentHashMap<String, PendingPulse> map = PENDING.get(server.dimension());
        if (map == null || map.isEmpty()) {
            return;
        }
        long now = server.getGameTime();
        Iterator<PendingPulse> it = map.values().iterator();
        while (it.hasNext()) {
            PendingPulse p = it.next();
            if (now - p.tickStamp >= FLUSH_TICKS) {
                EffectDispatch.broadcastEssentiaStream(server, p.from, p.to, p.color, p.typeTag, p.count, p.scale, p.extend, p.my);
                it.remove();
            }
        }
    }

    private static final class PendingPulse {
        final Vec3 from;
        final Vec3 to;
        final int color;
        final int typeTag;
        int count;
        final float scale;
        int extend;
        double my;
        long tickStamp;

        PendingPulse(Vec3 from, Vec3 to, int color, int typeTag, int count, float scale, int extend, double my, long tickStamp) {
            this.from = from;
            this.to = to;
            this.color = color;
            this.typeTag = typeTag;
            this.count = count;
            this.scale = scale;
            this.extend = extend;
            this.my = my;
            this.tickStamp = tickStamp;
        }
    }
}
