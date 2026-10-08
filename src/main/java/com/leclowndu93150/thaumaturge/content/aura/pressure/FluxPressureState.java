package com.leclowndu93150.thaumaturge.content.aura.pressure;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public final class FluxPressureState {
    private final List<FluxRain> rains = new ArrayList<>();
    private final List<FluxLightning> bolts = new ArrayList<>();
    private @Nullable BlockPos pending;

    public void queue(BlockPos chunkOrigin) {
        pending = chunkOrigin.immutable();
    }

    public @Nullable BlockPos pollPending() {
        BlockPos queued = pending;
        pending = null;
        return queued;
    }

    public void addRain(FluxRain rain) {
        rains.add(rain);
    }

    public void addLightning(FluxLightning bolt) {
        bolts.add(bolt);
    }

    public boolean hasRainNear(BlockPos pos, double rangeSq) {
        for (FluxRain rain : rains) {
            if (rain.center().distSqr(pos) <= rangeSq) {
                return true;
            }
        }
        return false;
    }

    public void tick(ServerLevel level) {
        if (!rains.isEmpty()) {
            rains.removeIf(rain -> !rain.tick(level));
        }
        if (!bolts.isEmpty()) {
            bolts.removeIf(bolt -> !bolt.tick(level));
        }
    }
}
