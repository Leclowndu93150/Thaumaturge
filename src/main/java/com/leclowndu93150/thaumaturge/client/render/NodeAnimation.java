package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/** Animation envelopes for the node's independently moving shells. */
public final class NodeAnimation {
    private static final int MAX_NORMAL_NODE_VISUAL_AMOUNT = 800;

    private NodeAnimation() {}

    public static float charge(int amount) {
        return Math.min(Math.max(0, amount), MAX_NORMAL_NODE_VISUAL_AMOUNT) / 50.0F;
    }

    public static float charge(int amount, boolean jarred) {
        float charge = charge(amount);
        return jarred ? Math.min(charge, 1.0F) : charge;
    }

    public static float shellRadius(float charge, float ticks, int shell) {
        float small = Mth.lerp(charge, 0.21F, 0.46F);
        float large = Mth.lerp(charge, 0.21F, 0.96F);
        return Mth.lerp(pulse(ticks, 82.0F + 11.0F * (shell % 5), shell * 0.29F), small, large);
    }

    public static float shellAngle(float ticks, int shell) {
        return (float) (cycle(ticks, 520.0F + 47.0F * (shell % 8), shell * 0.17F) * Mth.TWO_PI);
    }

    public static float coreRadius(float meanCharge) {
        return Mth.lerp(meanCharge, 0.115F, 0.425F);
    }

    public static float opacity(float base, @Nullable NodeModifier modifier, float ticks) {
        float gain = modifier == null
                ? 1.0F
                : switch (modifier) {
                    case BRIGHT -> 1.4F;
                    case PALE -> 0.7F;
                    case FADING -> Mth.lerp(pulse(ticks, 46.0F, 0.35F), 0.12F, 0.56F);
                };
        return Mth.clamp(base * gain, 0.0F, 1.0F);
    }

    private static float pulse(float ticks, float period, float phase) {
        float triangle = (float) (1.0 - Math.abs(2.0 * cycle(ticks, period, phase) - 1.0));
        return triangle * triangle * (3.0F - 2.0F * triangle);
    }

    private static double cycle(float ticks, float period, float phase) {
        double turns = ticks / (double) period + phase;
        return turns - Math.floor(turns);
    }
}
