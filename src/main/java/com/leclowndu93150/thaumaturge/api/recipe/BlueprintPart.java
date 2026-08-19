package com.leclowndu93150.thaumaturge.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BlueprintPart(BlueprintSource source, BlueprintTarget target, int priority) {
    public static final MapCodec<BlueprintPart> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(BlueprintSource.CODEC.fieldOf("source").forGetter(BlueprintPart::source),
            BlueprintTarget.CODEC.optionalFieldOf("target", BlueprintTarget.Keep.INSTANCE).forGetter(BlueprintPart::target),
            Codec.INT.optionalFieldOf("priority", 50).forGetter(BlueprintPart::priority)).apply(i, BlueprintPart::new));

    public static final Codec<BlueprintPart> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintPart> STREAM_CODEC = StreamCodec.composite(BlueprintSource.STREAM_CODEC, BlueprintPart::source, BlueprintTarget.STREAM_CODEC,
            BlueprintPart::target, ByteBufCodecs.VAR_INT, BlueprintPart::priority, BlueprintPart::new);
}
