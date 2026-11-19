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
}
