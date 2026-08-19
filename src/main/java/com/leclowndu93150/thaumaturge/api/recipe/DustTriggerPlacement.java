package com.leclowndu93150.thaumaturge.api.recipe;

import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public record DustTriggerPlacement(int xOffset, int yOffset, int zOffset, @Nullable Direction facing) {
    public static DustTriggerPlacement origin() {
        return new DustTriggerPlacement(0, 0, 0, null);
    }
}
