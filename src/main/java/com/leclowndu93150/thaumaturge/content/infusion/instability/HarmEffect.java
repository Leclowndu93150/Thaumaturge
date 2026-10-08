package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public record HarmEffect(boolean everyone, List<MobEffectInstance> effects) implements InstabilityEffect {
    public static final MapCodec<HarmEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("everyone", false).forGetter(HarmEffect::everyone),
                    ExtraCodecs.nonEmptyList(MobEffectInstance.CODEC.listOf())
                            .fieldOf("effects")
                            .forGetter(HarmEffect::effects))
            .apply(instance, HarmEffect::new));

    @Override
    public InstabilityEffectType<?> type() {
        return TTInstabilityEffects.HARM.get();
    }

    @Override
    public void apply(InstabilityContext context) {
        for (LivingEntity target : context.nearby(LivingEntity.class)) {
            target.addEffect(new MobEffectInstance(effects.get(context.random().nextInt(effects.size()))));
            if (!everyone) {
                return;
            }
        }
    }
}
