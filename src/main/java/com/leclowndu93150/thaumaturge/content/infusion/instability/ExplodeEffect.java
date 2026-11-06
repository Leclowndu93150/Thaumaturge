package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
        Vec3 centre = LevelSpaces.worldCenter(context.level(), context.matrix());
        context.level()
                .explode(
                        null,
                        centre.x,
                        centre.y,
                        centre.z,
                        power.sample(context.random()),
                        Level.ExplosionInteraction.NONE);
    }
}
