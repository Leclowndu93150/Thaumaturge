package com.leclowndu93150.thaumaturge.client.taint;

import net.minecraft.util.Mth;

public final class TaintEnvironmentClientState {
    private static float target;
    private static float displayed;

    private TaintEnvironmentClientState() {}

    public static void accept(float pressure) {
        target = Mth.clamp(pressure, 0.0F, 1.0F);
    }

    public static void tick() {
        displayed = Mth.lerp(0.05F, displayed, target);
    }

    public static float pressure() {
        return displayed;
    }

    public static void reset() {
        target = 0.0F;
        displayed = 0.0F;
    }
}
