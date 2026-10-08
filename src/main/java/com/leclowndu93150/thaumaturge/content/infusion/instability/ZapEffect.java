package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.LivingEntity;

public record ZapEffect(boolean everyone, IntProvider damage) implements InstabilityEffect {
    public static final MapCodec<ZapEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("everyone", false).forGetter(ZapEffect::everyone),
                    IntProvider.CODEC.fieldOf("damage").forGetter(ZapEffect::damage))
            .apply(instance, ZapEffect::new));

    @Override
    public InstabilityEffectType<?> type() {
        return TTInstabilityEffects.ZAP.get();
    }

    @Override
    public void apply(InstabilityContext context) {
        boolean played = false;
        for (LivingEntity target : context.nearby(LivingEntity.class)) {
            if (!played) {
                context.zapSound();
                played = true;
            }
            context.arcTo(target.position().add(0.0, target.getBbHeight() / 2.0, 0.0));
            target.hurt(context.level().damageSources().magic(), damage.sample(context.random()));
            if (!everyone) {
                return;
            }
        }
    }
}
