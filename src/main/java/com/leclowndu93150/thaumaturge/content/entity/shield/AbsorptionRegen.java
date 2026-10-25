package com.leclowndu93150.thaumaturge.content.entity.shield;

import net.minecraft.world.entity.LivingEntity;

public final class AbsorptionRegen {
    private static final int MEND_INTERVAL = 25;
    private static final float MEND_STEP = 1.0F;

    private AbsorptionRegen() {}

    public static void mend(LivingEntity entity, float ceiling) {
        if (entity.tickCount % MEND_INTERVAL != 0 || entity.invulnerableTime > 0) {
            return;
        }
        float absorption = entity.getAbsorptionAmount();
        if (absorption >= ceiling) {
            return;
        }
        entity.setAbsorptionAmount(Math.min(ceiling, absorption + MEND_STEP));
    }
}
