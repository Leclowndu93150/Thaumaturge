package com.leclowndu93150.thaumaturge.content.entity.cult;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

public final class CultRally {
    public static final int INTERVAL = 5;

    private static final double RADIUS = 8.0;
    private static final int REGENERATION_TICKS = 60;
    private static final int REGENERATION_AMPLIFIER = 1;

    private CultRally() {}

    public static void rally(Mob rallier, ServerLevel level) {
        for (EntityCultist cultist : level.getEntitiesOfClass(
                EntityCultist.class, rallier.getBoundingBox().inflate(RADIUS), CultRally::lacksRegeneration)) {
            if (cultist != rallier) {
                cultist.addEffect(
                        new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_TICKS, REGENERATION_AMPLIFIER));
            }
        }
    }

    private static boolean lacksRegeneration(EntityCultist cultist) {
        return cultist.isAlive() && !cultist.hasEffect(MobEffects.REGENERATION);
    }
}
