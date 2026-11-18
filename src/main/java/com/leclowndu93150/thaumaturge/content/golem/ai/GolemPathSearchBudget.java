package com.leclowndu93150.thaumaturge.content.golem.ai;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;

/** Main-thread, per-level admission control for expensive cross-frame task path searches. */
final class GolemPathSearchBudget {
    private static final int SEARCHES_PER_TICK = 2;
    private static final Map<ServerLevel, Usage> USE = new WeakHashMap<>();

    private GolemPathSearchBudget() {}

    static boolean tryAcquire(ServerLevel level) {
        long tick = level.getGameTime();
        Usage usage = USE.computeIfAbsent(level, ignored -> new Usage());
        if (usage.tick != tick) {
            usage.tick = tick;
            usage.count = 0;
        }
        if (usage.count >= SEARCHES_PER_TICK) return false;
        usage.count++;
        return true;
    }

    static boolean exhausted(ServerLevel level) {
        Usage usage = USE.get(level);
        return usage != null && usage.tick == level.getGameTime() && usage.count >= SEARCHES_PER_TICK;
    }

    private static final class Usage {
        long tick = Long.MIN_VALUE;
        int count;
    }
}
