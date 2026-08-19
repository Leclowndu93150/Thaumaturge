package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ScanGlyphParticleOptions(int color, int delay, boolean additive) implements ParticleOptions {

    public static final MapCodec<ScanGlyphParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(ScanGlyphParticleOptions::color), Codec.INT.fieldOf("delay").forGetter(ScanGlyphParticleOptions::delay),
                    Codec.BOOL.fieldOf("additive").forGetter(ScanGlyphParticleOptions::additive)).apply(inst, ScanGlyphParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScanGlyphParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, ScanGlyphParticleOptions::color, ByteBufCodecs.VAR_INT,
            ScanGlyphParticleOptions::delay, ByteBufCodecs.BOOL, ScanGlyphParticleOptions::additive, ScanGlyphParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.SCAN_GLYPH.get();
    }
}
