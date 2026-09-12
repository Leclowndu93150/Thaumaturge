package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/** Shared TC5-style combat infection behavior for dedicated tainted fauna. */
public final class TaintMobCombat {
    private TaintMobCombat() {}

    public static void maybeApplyFluxTaint(Mob attacker, LivingEntity victim) {
        int severity =
                switch (attacker.level().getDifficulty()) {
                    case NORMAL -> 3;
                    case HARD -> 6;
                    default -> 0;
                };
        if (severity > 0 && attacker.getRandom().nextInt(severity + 1) > 2) {
            victim.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, severity * 20, 0));
        }
    }
}
