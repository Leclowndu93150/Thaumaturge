package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class GolemPathSearchBudgetTest {
    @Test
    void limitsWorkPerLevelAndResetsOnNextServerTick() {
        ServerLevel level = mock(ServerLevel.class);
        AtomicLong now = new AtomicLong(100);
        when(level.getGameTime()).thenAnswer(ignored -> now.get());
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        assertTrue(GolemPathSearchBudget.tryAcquire(level, first));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, second));
        assertFalse(GolemPathSearchBudget.tryAcquire(level, third));
        assertTrue(GolemPathSearchBudget.wasDeferred(level, third));
        now.incrementAndGet();
        assertFalse(GolemPathSearchBudget.wasDeferred(level, third));
        // A deferred golem is guaranteed capacity even if the previous winners
        // are ticked first again on the next server tick.
        assertTrue(GolemPathSearchBudget.tryAcquire(level, first));
        assertFalse(GolemPathSearchBudget.tryAcquire(level, second));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, third));
    }

    @Test
    void reservedSearchSlotGoesToOldestWaiterRatherThanFirstTickingWaiter() {
        ServerLevel level = mock(ServerLevel.class);
        AtomicLong now = new AtomicLong(900);
        when(level.getGameTime()).thenAnswer(ignored -> now.get());
        UUID earlyTicker = UUID.randomUUID();
        UUID secondTicker = UUID.randomUUID();
        UUID oldestWaiter = UUID.randomUUID();
        UUID newerWaiter = UUID.randomUUID();
        assertTrue(GolemPathSearchBudget.tryAcquire(level, earlyTicker));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, secondTicker));
        assertFalse(GolemPathSearchBudget.tryAcquire(level, oldestWaiter));
        assertFalse(GolemPathSearchBudget.tryAcquire(level, newerWaiter));

        now.incrementAndGet();
        assertTrue(GolemPathSearchBudget.tryAcquire(level, earlyTicker));
        // Even though it ticks before the older waiter, it cannot steal the reserve.
        assertFalse(GolemPathSearchBudget.tryAcquire(level, newerWaiter));
        assertFalse(GolemPathSearchBudget.tryAcquire(level, secondTicker));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, oldestWaiter));
        now.incrementAndGet();
        assertTrue(GolemPathSearchBudget.tryAcquire(level, earlyTicker));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, newerWaiter));
    }

    @Test
    void idleDeferredRequestsExpireInsteadOfBlockingOtherGolems() {
        ServerLevel level = mock(ServerLevel.class);
        AtomicLong now = new AtomicLong(200);
        when(level.getGameTime()).thenAnswer(ignored -> now.get());
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        assertTrue(GolemPathSearchBudget.tryAcquire(level, first));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, second));
        assertFalse(GolemPathSearchBudget.tryAcquire(level, third));
        now.addAndGet(21);
        assertTrue(GolemPathSearchBudget.tryAcquire(level, first));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, second));
    }

    @Test
    void competingSearchesRetainTwoMillisecondFairSlices() {
        ServerLevel level = mock(ServerLevel.class);
        AtomicLong now = new AtomicLong(700);
        when(level.getGameTime()).thenAnswer(call -> now.get());
        UUID early = UUID.randomUUID();
        UUID late = UUID.randomUUID();
        UUID deferred = UUID.randomUUID();
        assertTrue(GolemPathSearchBudget.tryAcquire(level, early));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, late));
        assertFalse(GolemPathSearchBudget.tryAcquire(level, deferred));
        now.incrementAndGet();
        assertTrue(GolemPathSearchBudget.tryAcquire(level, early));
        assertTrue(GolemPathSearchBudget.availableNanos(level) == 2_000_000);
    }

    @Test
    void pendingWorkIsNotReportedAsUnreachableAndHasRealTimeLimit() {
        ServerLevel level = mock(ServerLevel.class);
        AtomicLong now = new AtomicLong(500);
        when(level.getGameTime()).thenAnswer(call -> now.get());
        UUID golem = UUID.randomUUID();
        GolemPathSearchBudget.markDeferred(level, golem);
        assertTrue(GolemPathSearchBudget.wasDeferred(level, golem));
        assertTrue(GolemPathSearchBudget.tryAcquire(level, golem));
        // Multiple callers in the same tick must not exhaust the per-golem slot.
        assertTrue(GolemPathSearchBudget.tryAcquire(level, golem));
        GolemPathSearchBudget.recordWork(level, 4_000_000);
        assertTrue(GolemPathSearchBudget.availableNanos(level) == 0);
        now.incrementAndGet();
        assertFalse(GolemPathSearchBudget.wasDeferred(level, golem));
        // One active search may use all otherwise idle global headroom.
        assertTrue(GolemPathSearchBudget.availableNanos(level) == 4_000_000);
    }
}
