package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class SpinedChampionTrait extends AbstractChampionTrait {
    private static final int MIN_DAMAGE = 1;
    private static final int DAMAGE_SPREAD = 3;
    private static final float VOLUME = 0.5F;

    @Override
    public float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        if (attacker != null && !source.is(DamageTypes.THORNS) && mob.level() instanceof ServerLevel server) {
            attacker.hurt(
                    mob.damageSources().thorns(mob),
                    MIN_DAMAGE + mob.getRandom().nextInt(DAMAGE_SPREAD));
            server.playSound(
                    null,
                    attacker.getX(),
                    attacker.getY(),
                    attacker.getZ(),
                    SoundEvents.THORNS_HIT,
                    SoundSource.HOSTILE,
                    VOLUME,
                    1.0F);
        }
        return amount;
    }
}
