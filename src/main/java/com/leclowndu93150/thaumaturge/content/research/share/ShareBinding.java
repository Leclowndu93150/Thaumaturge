package com.leclowndu93150.thaumaturge.content.research.share;

import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShareBinding(UUID player, String name, AspectPoolData discoveredAspects, PlayerKnowledge knowledge) {
    public static final Codec<ShareBinding> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(UUIDUtil.CODEC.fieldOf("player").forGetter(ShareBinding::player), Codec.STRING.fieldOf("name").forGetter(ShareBinding::name),
                    AspectPoolData.CODEC.fieldOf("discovered_aspects").forGetter(ShareBinding::discoveredAspects), PlayerKnowledge.CODEC.fieldOf("knowledge").forGetter(ShareBinding::knowledge))
            .apply(builder, ShareBinding::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShareBinding> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, ShareBinding::player, ByteBufCodecs.STRING_UTF8,
            ShareBinding::name, AspectPoolData.STREAM_CODEC, ShareBinding::discoveredAspects, PlayerKnowledge.STREAM_CODEC, ShareBinding::knowledge, ShareBinding::new);
}
