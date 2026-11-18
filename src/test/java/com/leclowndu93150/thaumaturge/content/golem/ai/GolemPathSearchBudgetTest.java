package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class GolemPathSearchBudgetTest {
    @Test
    void limitsWorkPerLevelAndResetsOnNextServerTick() {
        ServerLevel level = mock(ServerLevel.class);
        AtomicLong now = new AtomicLong(100);
        when(level.getGameTime()).thenAnswer(ignored -> now.get());
        assertTrue(GolemPathSearchBudget.tryAcquire(level));
        assertTrue(GolemPathSearchBudget.tryAcquire(level));
        assertFalse(GolemPathSearchBudget.tryAcquire(level));
        now.incrementAndGet();
        assertTrue(GolemPathSearchBudget.tryAcquire(level));
    }
}
