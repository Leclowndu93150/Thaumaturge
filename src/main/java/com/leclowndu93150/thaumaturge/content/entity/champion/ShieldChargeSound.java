package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class ShieldChargeSound {
    private static final float VOLUME = 0.66F;
    private static final float BASE_PITCH = 1.1F;
    private static final float PITCH_SPREAD = 0.1F;

    private ShieldChargeSound() {}

    public static void playIfShielded(LivingEntity entity) {
        if (entity.getAbsorptionAmount() > 0.0F) {
            entity.level()
                    .playSound(
                            null,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            TCSounds.RUNICSHIELDCHARGE.get(),
                            SoundSource.HOSTILE,
                            VOLUME,
                            BASE_PITCH + entity.getRandom().nextFloat() * PITCH_SPREAD);
        }
    }
}
