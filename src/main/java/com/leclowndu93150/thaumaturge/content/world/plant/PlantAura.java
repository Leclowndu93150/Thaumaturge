package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record PlantAura(List<ParticleOptions> particles, float chance, float height, float spread, float drift) {
    public static final Codec<PlantAura> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ParticleTypes.CODEC.listOf().fieldOf("particles").forGetter(PlantAura::particles),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(PlantAura::chance),
                    Codec.FLOAT.fieldOf("height").forGetter(PlantAura::height),
                    Codec.FLOAT.fieldOf("spread").forGetter(PlantAura::spread),
                    Codec.FLOAT.fieldOf("drift").forGetter(PlantAura::drift))
            .apply(instance, PlantAura::new));

    public void emit(Level level, Vec3 stem, RandomSource random) {
        if (random.nextFloat() >= chance) {
            return;
        }
        for (ParticleOptions particle : particles) {
            level.addParticle(
                    particle,
                    stem.x + random.triangle(0.0, spread),
                    stem.y + height + random.triangle(0.0, spread * 0.5),
                    stem.z + random.triangle(0.0, spread),
                    random.triangle(0.0, drift),
                    random.triangle(0.0, drift),
                    random.triangle(0.0, drift));
        }
    }
}
