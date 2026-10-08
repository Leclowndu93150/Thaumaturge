package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

record GarrisonState(boolean activated, int budget, boolean quelled) {
    public static final GarrisonState FRESH = new GarrisonState(false, 0, false);
    public static final Codec<GarrisonState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.fieldOf("activated").forGetter(GarrisonState::activated),
                    Codec.INT.fieldOf("budget").forGetter(GarrisonState::budget),
                    Codec.BOOL.fieldOf("quelled").forGetter(GarrisonState::quelled))
            .apply(instance, GarrisonState::new));

    GarrisonState activate(int budget) {
        return activated ? this : new GarrisonState(true, budget, quelled);
    }

    GarrisonState spend() {
        return new GarrisonState(activated, Math.max(0, budget - 1), quelled);
    }

    GarrisonState quell() {
        return new GarrisonState(activated, budget, true);
    }
}
