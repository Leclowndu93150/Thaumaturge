package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import com.leclowndu93150.thaumaturge.content.entity.champion.ShieldChargeSound;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.Nullable;

public final class WardedChampionTrait extends AbstractChampionTrait {
    private static final int RECHARGE_INTERVAL_TICKS = 25;
    private static final float RECHARGE_AMOUNT = 1.0F;

    @Override
    protected void championModifiers(LivingEntity mob, MobTraitModifiers modifiers) {
        double ward = ward(mob);
        if (mob.getAttributeBaseValue(Attributes.MAX_ABSORPTION) < ward) {
            modifiers.setBase(Attributes.MAX_ABSORPTION, ward);
        }
    }

    @Override
    protected void onChampionAdded(LivingEntity mob) {
        mob.setAbsorptionAmount(mob.getAbsorptionAmount() + ward(mob));
    }

    @Override
    public void tick(LivingEntity mob) {
        if (mob.invulnerableTime <= 0
                && mob.tickCount % RECHARGE_INTERVAL_TICKS == 0
                && mob.getAbsorptionAmount() < ward(mob)) {
            mob.setAbsorptionAmount(mob.getAbsorptionAmount() + RECHARGE_AMOUNT);
        }
    }

    @Override
    public float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        ShieldChargeSound.playIfShielded(mob);
        return amount;
    }

    private static int ward(LivingEntity mob) {
        return (int) mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
    }
}
