package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

public final class TaintPressure {
    public static final MapCodec<TaintPressure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.optionalFieldOf("saturation", 0.0F).forGetter(pressure -> pressure.saturation),
                    Codec.LONG.optionalFieldOf("last_update", 0L).forGetter(pressure -> pressure.lastUpdate),
                    Codec.LONG.optionalFieldOf("last_active_seed", 0L).forGetter(pressure -> pressure.lastActiveSeed))
            .apply(instance, TaintPressure::new));

    private float saturation;
    private long lastUpdate;
    private long lastActiveSeed;

    public TaintPressure() {
        this(0.0F, 0L, 0L);
    }

    private TaintPressure(float saturation, long lastUpdate, long lastActiveSeed) {
        this.saturation = Mth.clamp(saturation, 0.0F, 1.0F);
        this.lastUpdate = lastUpdate;
        this.lastActiveSeed = lastActiveSeed;
    }

    public float saturationAt(long gameTime, float decayPerTick) {
        long elapsed = Math.max(0L, gameTime - lastUpdate);
        return Math.max(0.0F, saturation - elapsed * decayPerTick);
    }

    public void set(float saturation, long gameTime) {
        this.saturation = Mth.clamp(saturation, 0.0F, 1.0F);
        this.lastUpdate = gameTime;
    }

    public void markActiveSeed(long gameTime) {
        this.lastActiveSeed = gameTime;
    }

    public long lastActiveSeed() {
        return lastActiveSeed;
    }
}
