package com.leclowndu93150.thaumaturge.content.golem.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;

/** Main-thread, per-level admission control for expensive cross-frame path searches. */
final class GolemPathSearchBudget {
    private static final int SEARCHES_PER_TICK = 2;
    private static final int WAIT_TIMEOUT_TICKS = 20;
    private static final Map<ServerLevel, Usage> USE = new WeakHashMap<>();

    private GolemPathSearchBudget() {}

    static boolean tryAcquire(ServerLevel level, UUID requester) {
        long tick = level.getGameTime();
        Usage usage = USE.computeIfAbsent(level, ignored -> new Usage());
        usage.advance(tick);

        // Reserving one search for previously deferred golems prevents fixed entity
        // tick order from repeatedly giving all searches to the same two golems.
        boolean waiting = usage.waiting.containsKey(requester);
        int freeSlots = usage.waiting.isEmpty() || waiting ? SEARCHES_PER_TICK : SEARCHES_PER_TICK - 1;
        if (usage.count >= freeSlots) {
            usage.waiting.put(requester, tick);
            usage.deferredThisTick.put(requester, tick);
            return false;
        }
        usage.count++;
        usage.waiting.remove(requester);
        usage.deferredThisTick.remove(requester);
        return true;
    }

    /** Distinguishes a throttled search from genuinely unreachable geometry. */
    static boolean wasDeferred(ServerLevel level, UUID requester) {
        Usage usage = USE.get(level);
        return usage != null
                && usage.tick == level.getGameTime()
                && usage.deferredThisTick.getOrDefault(requester, Long.MIN_VALUE) == usage.tick;
    }

    private static final class Usage {
        long tick = Long.MIN_VALUE;
        int count;
        final Map<UUID, Long> waiting = new HashMap<>();
        final Map<UUID, Long> deferredThisTick = new HashMap<>();

        void advance(long now) {
            if (tick == now) return;
            tick = now;
            count = 0;
            deferredThisTick.clear();
            Iterator<Map.Entry<UUID, Long>> entries = waiting.entrySet().iterator();
            while (entries.hasNext()) {
                if (now - entries.next().getValue() > WAIT_TIMEOUT_TICKS) entries.remove();
            }
        }
    }
}
