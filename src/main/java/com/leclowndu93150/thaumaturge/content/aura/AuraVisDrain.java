package com.leclowndu93150.thaumaturge.content.aura;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;

public final class AuraVisDrain {
    private static final double EPSILON = 0.0001D;

    private AuraVisDrain() {}

    public static double acrossAnchors(double requested, List<BlockPos> anchors, Drainer drainer) {
        if (requested <= EPSILON) {
            return 0.0D;
        }
        List<BlockPos> availableAnchors = new ArrayList<>(anchors);
        double remaining = requested;
        while (remaining > EPSILON && !availableAnchors.isEmpty()) {
            double progress = 0.0D;
            double share = remaining / availableAnchors.size();
            for (Iterator<BlockPos> iterator = availableAnchors.iterator(); iterator.hasNext(); ) {
                BlockPos anchor = iterator.next();
                float amount = (float) Math.min(remaining, share);
                float drained = Math.max(0.0F, Math.min(amount, drainer.drain(anchor, amount)));
                remaining -= drained;
                progress += drained;
                if (drained + EPSILON < amount) {
                    iterator.remove();
                }
                if (remaining <= EPSILON) {
                    return requested;
                }
            }
            if (progress <= EPSILON) {
                break;
            }
        }
        return requested - remaining;
    }

    @FunctionalInterface
    public interface Drainer {
        float drain(BlockPos anchor, float amount);
    }
}
