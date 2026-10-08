package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record PlantContactEffect(Holder<MobEffect> effect, int duration, int oneIn) {
    public static final Codec<PlantContactEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    MobEffect.CODEC.fieldOf("effect").forGetter(PlantContactEffect::effect),
                    ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(PlantContactEffect::duration),
                    ExtraCodecs.POSITIVE_INT.fieldOf("one_in").forGetter(PlantContactEffect::oneIn))
            .apply(instance, PlantContactEffect::new));

    public void touch(Level level, Entity entity) {
        if (!level.isClientSide()
                && entity instanceof LivingEntity living
                && level.getRandom().nextInt(oneIn) == 0) {
            living.addEffect(new MobEffectInstance(effect, duration));
        }
    }
}
