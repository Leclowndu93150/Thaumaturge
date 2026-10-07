package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record InfusionCrumbsParticleOptions(
        ItemStack stack, double tx, double ty, double tz, double sx, double sy, double sz) implements ParticleOptions {
    public static final MapCodec<InfusionCrumbsParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ItemStack.CODEC.fieldOf("item").forGetter(InfusionCrumbsParticleOptions::stack),
                    Codec.DOUBLE.fieldOf("tx").forGetter(InfusionCrumbsParticleOptions::tx),
                    Codec.DOUBLE.fieldOf("ty").forGetter(InfusionCrumbsParticleOptions::ty),
                    Codec.DOUBLE.fieldOf("tz").forGetter(InfusionCrumbsParticleOptions::tz),
                    Codec.DOUBLE.fieldOf("sx").forGetter(InfusionCrumbsParticleOptions::sx),
                    Codec.DOUBLE.fieldOf("sy").forGetter(InfusionCrumbsParticleOptions::sy),
                    Codec.DOUBLE.fieldOf("sz").forGetter(InfusionCrumbsParticleOptions::sz))
            .apply(inst, InfusionCrumbsParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionCrumbsParticleOptions> STREAM_CODEC =
            StreamCodec.of(
                    (buf, o) -> {
                        ItemStack.STREAM_CODEC.encode(buf, o.stack());
                        buf.writeDouble(o.tx());
                        buf.writeDouble(o.ty());
                        buf.writeDouble(o.tz());
                        buf.writeDouble(o.sx());
                        buf.writeDouble(o.sy());
                        buf.writeDouble(o.sz());
                    },
                    buf -> new InfusionCrumbsParticleOptions(
                            ItemStack.STREAM_CODEC.decode(buf),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble()));

    @Override
    public ParticleType<?> getType() {
        return TTParticles.INFUSION_CRUMBS.get();
    }
}
