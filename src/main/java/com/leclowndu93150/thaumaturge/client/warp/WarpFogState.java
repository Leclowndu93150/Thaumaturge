package com.leclowndu93150.thaumaturge.client.warp;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;

public final class WarpFogState {
    public static final int MIST_DURATION_TICKS = 2400;
    public static final int MIST_SHORT_DURATION_TICKS = 200;

    private static int warpMistTicks;
    private static int guardianMistTicks;

    private WarpFogState() {}

    public static void startMist() {
        warpMistTicks = MIST_DURATION_TICKS;
    }

    public static void startShortMist() {
        guardianMistTicks = Math.max(guardianMistTicks, MIST_SHORT_DURATION_TICKS);
    }

    public static void tick() {
        warpMistTicks = Math.max(0, warpMistTicks - 1);
        guardianMistTicks = Math.max(0, guardianMistTicks - 1);
    }

    public static float warpMistIntensity() {
        return intensity(warpMistTicks);
    }

    public static float guardianMistIntensity() {
        return intensity(guardianMistTicks);
    }

    public static float visibleIntensity() {
        return Math.max(
                ThaumaturgeClientConfig.warpMistFog() ? warpMistIntensity() : 0.0F,
                ThaumaturgeClientConfig.eldritchGuardianFog() ? guardianMistIntensity() : 0.0F);
    }

    public static void reset() {
        warpMistTicks = 0;
        guardianMistTicks = 0;
    }

    private static float intensity(int ticks) {
        return Math.min(1.0F, ticks / 100.0F);
    }
}
