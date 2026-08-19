package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.BurstParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import net.minecraft.util.RandomSource;

public final class WispEffects {
    private static final float MOTE_ALPHA = 0.5F;
    private static final float MOTE_SCALE_SPREAD = 0.25F;
    private static final float MOTE_END_SCALE = 0.05F;

    private WispEffects() {}

    public static BurstParticleOptions burst(float size) {
        return new BurstParticleOptions(size);
    }

    public static WispFlameParticleOptions mote(RandomSource random, int color) {
        return new WispFlameParticleOptions(color, MOTE_ALPHA, 1.0F + random.nextFloat() * MOTE_SCALE_SPREAD, MOTE_END_SCALE, 0);
    }
}
