package com.leclowndu93150.thaumaturge.content.infusion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InfusionClientAnimatorTest {
    @Test
    void swayCountsUpWhileCraftingAndSettlesAtTheConfiguredLimit() {
        assertEquals(1, InfusionClientAnimator.craftTicks(0, true));
        assertEquals(48, InfusionClientAnimator.craftTicks(51, false));
        assertEquals(0, InfusionClientAnimator.craftTicks(1, false));
        assertEquals(0, InfusionClientAnimator.craftTicks(-3, false));
    }

    @Test
    void startupEasesTowardTheTargetAndSnapsAtTheEnd() {
        assertEquals(0.55F, InfusionClientAnimator.startUp(0.5F, true), 1.0E-6F);
        assertEquals(0.45F, InfusionClientAnimator.startUp(0.5F, false), 1.0E-6F);
        assertEquals(1.0F, InfusionClientAnimator.startUp(0.9995F, true), 1.0E-6F);
        assertEquals(0.0F, InfusionClientAnimator.startUp(0.0005F, false), 1.0E-6F);
    }

    @Test
    void restoredStartupMatchesAnUninterruptedAnimation() {
        float continuous = 0.0F;
        for (int tick = 0; tick < 70; tick++) {
            assertEquals(continuous, InfusionClientAnimator.startUpAt(tick));
            continuous = InfusionClientAnimator.startUp(continuous, true);
        }
        assertEquals(0.0F, InfusionClientAnimator.startUpAt(-1));
        assertEquals(1.0F, InfusionClientAnimator.startUpAt(10_000L));
    }
}
