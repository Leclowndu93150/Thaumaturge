package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.Level;

public record ExplodeEffect(FloatProvider power) implements InstabilityEffect {
    public static final MapCodec<ExplodeEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(FloatProvider.CODEC.fieldOf("power").forGetter(ExplodeEffect::power))
                    .apply(instance, ExplodeEffect::new));

    @Override
    public InstabilityEffectType<?> type() {
        return TTInstabilityEffects.EXPLODE.get();
    }

    @Override
    public void apply(InstabilityContext context) {
        BlockPos matrix = context.matrix();
        context.level()
                .explode(
                        null,
                        matrix.getX() + 0.5,
                        matrix.getY() + 0.5,
                        matrix.getZ() + 0.5,
                        power.sample(context.random()),
                        Level.ExplosionInteraction.NONE);
    }
}
