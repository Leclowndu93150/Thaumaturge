package com.leclowndu93150.thaumaturge.content.entity.eldritch;

import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.entity.shield.AbsorptionRegen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class GuardianShield {
    private static final double SHIELD_FRACTION = 0.5;

    private GuardianShield() {}

    public static double capacityFor(double maxHealth) {
        return maxHealth * SHIELD_FRACTION;
    }

    public static float capacity(LivingEntity guardian) {
        return (float) capacityFor(guardian.getAttributeBaseValue(Attributes.MAX_HEALTH));
    }

    public static void grant(LivingEntity guardian) {
        if (inOuterLands(guardian)) {
            guardian.setAbsorptionAmount(capacity(guardian));
        }
    }

    public static void tick(LivingEntity guardian) {
        if (inOuterLands(guardian)) {
            AbsorptionRegen.mend(guardian, capacity(guardian));
        }
    }

    private static boolean inOuterLands(LivingEntity guardian) {
        return guardian.level().dimension() == OuterLands.DIMENSION;
    }
}
