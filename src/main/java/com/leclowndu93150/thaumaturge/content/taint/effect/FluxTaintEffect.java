package com.leclowndu93150.thaumaturge.content.taint.effect;

import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageSources;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class FluxTaintEffect extends MobEffect {
    private static final int COLOR = 0xFF0080;
    private static final int BASE_PERIOD = 40;
    private static final int MAX_AMPLIFIER_SHIFT = 5;
    private static final float HEAL_AMOUNT = 1.0F;
    private static final float DAMAGE_AMOUNT = 1.0F;

    public FluxTaintEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        int period = Math.max(1, BASE_PERIOD >> Math.min(amplification, MAX_AMPLIFIER_SHIFT));
        return tickCount % period == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        if (MobTraits.isTainted(mob)) {
            mob.heal(HEAL_AMOUNT);
        } else if (!mob.is(EntityTypeTags.UNDEAD)) {
            mob.hurtServer(level, TTDamageSources.taint(level), DAMAGE_AMOUNT);
        }
        return true;
    }
}
