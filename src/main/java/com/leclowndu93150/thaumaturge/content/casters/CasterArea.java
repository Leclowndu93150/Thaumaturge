package com.leclowndu93150.thaumaturge.content.casters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CasterArea(int x, int y, int z, int dim) {
    public static final int DEFAULT_SIZE = 3;
    public static final CasterArea DEFAULT = new CasterArea(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE, 0);

    public static final Codec<CasterArea> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("x").forGetter(CasterArea::x), Codec.INT.fieldOf("y").forGetter(CasterArea::y),
            Codec.INT.fieldOf("z").forGetter(CasterArea::z), Codec.INT.fieldOf("dim").forGetter(CasterArea::dim)).apply(instance, CasterArea::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CasterArea> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, CasterArea::x, ByteBufCodecs.VAR_INT, CasterArea::y,
            ByteBufCodecs.VAR_INT, CasterArea::z, ByteBufCodecs.VAR_INT, CasterArea::dim, CasterArea::new);
}
