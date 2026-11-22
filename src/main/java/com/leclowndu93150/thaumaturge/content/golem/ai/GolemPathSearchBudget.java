package com.leclowndu93150.thaumaturge.content.golem.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;

/** Main-thread, per-level admission control for expensive cross-frame path searches. */
final class GolemPathSearchBudget {
    private static final int SEARCHES_PER_TICK = 2;
    private static final int WAIT_TIMEOUT_TICKS = 20;
    // Unlike the old request-count-only budget, this also limits real main-thread time.
    // A path attempt can span ticks; no tick should run hundreds of A* expansions.
    private static final long MAX_WORK_NANOS = 4_000_000L;
    private static final long MAX_GOLEM_WORK_NANOS = 2_000_000L;
    private static final Map<ServerLevel, Usage> USE = new WeakHashMap<>();

    private GolemPathSearchBudget() {}

    static boolean tryAcquire(ServerLevel level, UUID requester) {
        long tick = level.getGameTime();
        Usage usage = USE.computeIfAbsent(level, ignored -> new Usage());
        usage.advance(tick);

        if (usage.granted.containsKey(requester)) return true;
        // Entity tick order is stable. The previous reservation let *any* queued
        // golem take the reserved slot, so the same early-ticking golem could win
        // indefinitely while older requests waited hundreds of ticks. Reserve the
        // last slot for the oldest still-active request, in insertion (FIFO) order.
        // A fresh requester can use the first slot, but cannot steal the reserve.
        UUID oldestWaiting = usage.waiting.isEmpty()
                ? null
                : usage.waiting.keySet().iterator().next();
        boolean reservedForAnother = oldestWaiting != null && !oldestWaiting.equals(requester);
        int availableSlots = reservedForAnother ? SEARCHES_PER_TICK - 1 : SEARCHES_PER_TICK;
        if (usage.count >= availableSlots || usage.workNanos >= MAX_WORK_NANOS) {
            // Updating a LinkedHashMap value leaves the original queue position intact.
            usage.waiting.put(requester, tick);
            usage.deferredThisTick.put(requester, tick);
            return false;
        }
        usage.count++;
        usage.granted.put(requester, tick);
        usage.waiting.remove(requester);
        usage.deferredThisTick.remove(requester);
        return true;
    }

    /** An in-progress search is pending, not a failed route. */
    static void markDeferred(ServerLevel level, UUID requester) {
        Usage usage = USE.computeIfAbsent(level, ignored -> new Usage());
        usage.advance(level.getGameTime());
        usage.deferredThisTick.put(requester, usage.tick);
    }

    static long availableNanos(ServerLevel level) {
        Usage usage = USE.computeIfAbsent(level, ignored -> new Usage());
        usage.advance(level.getGameTime());
        // The per-level ceiling is already 4 ms. If exactly one search is
        // active, let it use otherwise idle headroom rather than spreading a
        // 200 ms search over 100 ticks. Once another requester is queued,
        // return to the 2 ms per-golem slice so the FIFO reserve stays fair.
        long perGolem = usage.waiting.isEmpty() ? MAX_WORK_NANOS : MAX_GOLEM_WORK_NANOS;
        return Math.max(0L, Math.min(perGolem, MAX_WORK_NANOS - usage.workNanos));
    }

    static void recordWork(ServerLevel level, long nanos) {
        Usage usage = USE.get(level);
        if (usage != null) usage.workNanos += Math.max(0L, nanos);
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
        long workNanos;
        final Map<UUID, Long> granted = new HashMap<>();
        final Map<UUID, Long> waiting = new LinkedHashMap<>();
        final Map<UUID, Long> deferredThisTick = new HashMap<>();

        void advance(long now) {
            if (tick == now) return;
            tick = now;
            count = 0;
            workNanos = 0;
            granted.clear();
            deferredThisTick.clear();
            Iterator<Map.Entry<UUID, Long>> entries = waiting.entrySet().iterator();
            while (entries.hasNext()) {
                if (now - entries.next().getValue() > WAIT_TIMEOUT_TICKS) entries.remove();
            }
        }
    }
}
