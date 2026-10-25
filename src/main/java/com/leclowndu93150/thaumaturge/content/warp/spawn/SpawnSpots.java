package com.leclowndu93150.thaumaturge.content.warp.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public final class SpawnSpots {
    private static final int MIN_AXIS_SHIFT = 7;
    private static final int AXIS_SHIFT_SPREAD = 18;
    private static final int DIRECTION_CHOICES = 3;

    private SpawnSpots() {}

    public static BlockPos ringCell(BlockPos origin, RandomSource random) {
        return origin.offset(axisShift(random), axisShift(random), axisShift(random));
    }

    private static int axisShift(RandomSource random) {
        return (MIN_AXIS_SHIFT + random.nextInt(AXIS_SHIFT_SPREAD)) * (random.nextInt(DIRECTION_CHOICES) - 1);
    }
}
