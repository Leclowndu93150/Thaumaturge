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

public record GolemEmoteParticleOptions(int color, int icon, int age, float scale) implements ParticleOptions {

    public static final int ICON_HEART = 0;
    public static final int ICON_STAY = 1;
    public static final int ICON_FAIL = 2;
    public static final int ICON_CONFUSED = 3;
    public static final int ICON_TASK = 4;

    public static final MapCodec<GolemEmoteParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(Codec.INT.fieldOf("color").forGetter(GolemEmoteParticleOptions::color), Codec.INT.fieldOf("icon").forGetter(GolemEmoteParticleOptions::icon),
                    Codec.INT.fieldOf("age").forGetter(GolemEmoteParticleOptions::age), Codec.FLOAT.fieldOf("scale").forGetter(GolemEmoteParticleOptions::scale))
            .apply(inst, GolemEmoteParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GolemEmoteParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, GolemEmoteParticleOptions::color, ByteBufCodecs.VAR_INT,
            GolemEmoteParticleOptions::icon, ByteBufCodecs.VAR_INT, GolemEmoteParticleOptions::age, ByteBufCodecs.FLOAT, GolemEmoteParticleOptions::scale, GolemEmoteParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.GOLEM_EMOTE.get();
    }
}
