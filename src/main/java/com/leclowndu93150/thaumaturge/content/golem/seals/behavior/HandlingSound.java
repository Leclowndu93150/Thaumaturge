package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

final class HandlingSound {
    static final float HIGH = 2.0F;
    static final float LOW = 1.0F;
    private static final float VOLUME = 0.125F;
    private static final float WOBBLE = 0.7F;

    private HandlingSound() {}

    static void play(IGolemAPI golem, float octave) {
        RandomSource random = golem.level().getRandom();
        golem.asEntity().playSound(SoundEvents.ITEM_PICKUP, VOLUME, ((random.nextFloat() - random.nextFloat()) * WOBBLE + 1.0F) * octave);
    }
}
