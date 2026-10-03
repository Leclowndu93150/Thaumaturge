package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class ArmoredChampionTrait extends AbstractChampionTrait {
    private static final float DAMAGE_FACTOR = 19.0F / 25.0F;

    @Override
    public float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        return attacker != null && !source.is(DamageTypeTags.BYPASSES_ARMOR) ? amount * DAMAGE_FACTOR : amount;
    }
}
