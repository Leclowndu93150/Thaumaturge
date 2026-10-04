package com.leclowndu93150.thaumaturge.content.essentia.bellows;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public final class BellowsStroke {
    public static final float FULL = 1.0F;

    private static final int CYCLE_TICKS = 32;
    private static final int SQUEEZE_TICKS = 9;
    private static final float EMPTY = 0.4F;

    private BellowsStroke() {}

    public static float inflation(long gameTime, float partialTick, BlockPos pos) {
        float stroke = cyclePosition(gameTime, pos) + partialTick;
        if (stroke < SQUEEZE_TICKS) {
            float squeezed = stroke / SQUEEZE_TICKS;
            return Mth.lerp(1.0F - (1.0F - squeezed) * (1.0F - squeezed), FULL, EMPTY);
        }
        float refilled = (stroke - SQUEEZE_TICKS) / (CYCLE_TICKS - SQUEEZE_TICKS);
        return Mth.lerp(refilled * refilled * (3.0F - 2.0F * refilled), EMPTY, FULL);
    }

    public static boolean startsSqueeze(long gameTime, BlockPos pos) {
        return cyclePosition(gameTime, pos) == 0;
    }

    private static int cyclePosition(long gameTime, BlockPos pos) {
        return (int) Math.floorMod(gameTime + Mth.getSeed(pos), (long) CYCLE_TICKS);
    }
}
