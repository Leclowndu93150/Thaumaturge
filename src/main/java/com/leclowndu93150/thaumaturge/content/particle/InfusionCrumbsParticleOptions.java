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
import net.minecraft.world.item.ItemStackTemplate;

public record InfusionCrumbsParticleOptions(ItemStackTemplate stack, double tx, double ty, double tz, double sx, double sy, double sz) implements ParticleOptions {
    public static final MapCodec<InfusionCrumbsParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(ItemStackTemplate.CODEC.fieldOf("item").forGetter(InfusionCrumbsParticleOptions::stack), Codec.DOUBLE.fieldOf("tx").forGetter(InfusionCrumbsParticleOptions::tx),
                    Codec.DOUBLE.fieldOf("ty").forGetter(InfusionCrumbsParticleOptions::ty), Codec.DOUBLE.fieldOf("tz").forGetter(InfusionCrumbsParticleOptions::tz),
                    Codec.DOUBLE.fieldOf("sx").forGetter(InfusionCrumbsParticleOptions::sx), Codec.DOUBLE.fieldOf("sy").forGetter(InfusionCrumbsParticleOptions::sy),
                    Codec.DOUBLE.fieldOf("sz").forGetter(InfusionCrumbsParticleOptions::sz)).apply(inst, InfusionCrumbsParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionCrumbsParticleOptions> STREAM_CODEC = StreamCodec.composite(ItemStackTemplate.STREAM_CODEC, InfusionCrumbsParticleOptions::stack,
            ByteBufCodecs.DOUBLE, InfusionCrumbsParticleOptions::tx, ByteBufCodecs.DOUBLE, InfusionCrumbsParticleOptions::ty, ByteBufCodecs.DOUBLE, InfusionCrumbsParticleOptions::tz,
            ByteBufCodecs.DOUBLE, InfusionCrumbsParticleOptions::sx, ByteBufCodecs.DOUBLE, InfusionCrumbsParticleOptions::sy, ByteBufCodecs.DOUBLE, InfusionCrumbsParticleOptions::sz,
            InfusionCrumbsParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.INFUSION_CRUMBS.get();
    }
}
