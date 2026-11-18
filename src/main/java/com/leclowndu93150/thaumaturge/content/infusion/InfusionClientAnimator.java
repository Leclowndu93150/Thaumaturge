package com.leclowndu93150.thaumaturge.content.infusion;

final class InfusionClientAnimator {
    private static final float FULLY_AWAKE = 1.0F;
    private static final float WAKE_GROWTH = 0.1F;
    private static final float WAKE_CREEP = 0.001F;
    private static final float SLEEP_DECAY = 0.1F;
    private static final float SLEEP_SNAP = 0.001F;
    private static final int SWAY_SETTLED_TICKS = 50;
    private static final int SWAY_WIND_DOWN = 2;

    private InfusionClientAnimator() {}

    static int craftTicks(int ticks, boolean crafting) {
        if (!crafting) {
            return Math.max(0, Math.min(ticks, SWAY_SETTLED_TICKS) - SWAY_WIND_DOWN);
        }
        return ticks + 1;
    }

    static float startUp(float current, boolean active) {
        if (active && current < FULLY_AWAKE) {
            return Math.min(FULLY_AWAKE, current + Math.max(current * WAKE_GROWTH, WAKE_CREEP));
        }
        if (!active && current > 0.0F) {
            float faded = current - current * SLEEP_DECAY;
            return faded < SLEEP_SNAP ? 0.0F : faded;
        }
        return current;
    }

    static float startUpAt(long ticks) {
        float startUp = 0.0F;
        for (long tick = 0; tick < ticks && startUp < FULLY_AWAKE; tick++) {
            startUp = startUp(startUp, true);
        }
        return startUp;
    }
}
