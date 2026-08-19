package com.leclowndu93150.thaumaturge.content.research.link;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LinkBinding(UUID player, String name) {
    public static final Codec<LinkBinding> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(UUIDUtil.CODEC.fieldOf("player").forGetter(LinkBinding::player), Codec.STRING.fieldOf("name").forGetter(LinkBinding::name)).apply(builder, LinkBinding::new));

    public static final StreamCodec<ByteBuf, LinkBinding> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, LinkBinding::player, ByteBufCodecs.STRING_UTF8, LinkBinding::name,
            LinkBinding::new);
}
