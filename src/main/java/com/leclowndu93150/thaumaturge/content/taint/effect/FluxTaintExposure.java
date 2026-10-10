package com.leclowndu93150.thaumaturge.content.taint.effect;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class FluxTaintExposure {
    public static final int CONTACT_DURATION = 200;

    private static final int AMPLIFIER = 0;

    private FluxTaintExposure() {}

    public static boolean isImmune(LivingEntity living) {
        return MobTraits.isTainted(living) || living.is(EntityTypeTags.UNDEAD);
    }

    public static void expose(LivingEntity living, int duration, boolean ambient, boolean particles) {
        living.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, duration, AMPLIFIER, ambient, particles, false));
    }

    public static void exposeQuietly(LivingEntity living) {
        expose(living, CONTACT_DURATION, true, false);
    }

    public static void onStep(Level level, Entity entity, int oneIn) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living) || isImmune(living) || level.getRandom().nextInt(oneIn) != 0) {
            return;
        }
        exposeQuietly(living);
    }
}
