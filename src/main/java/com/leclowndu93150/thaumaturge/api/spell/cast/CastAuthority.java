package com.leclowndu93150.thaumaturge.api.spell.cast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/** Immutable identity of a bounded, server-issued automated cast. The record alone grants no permission. */
public record CastAuthority(UUID sourceId, UUID ownerId, Identifier dimension, Identifier scope, UUID grantId, long revision, UUID castId) {
    public static final Codec<CastAuthority> CODEC = RecordCodecBuilder.create(builder -> builder.group(UUIDUtil.CODEC.fieldOf("source").forGetter(CastAuthority::sourceId),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(CastAuthority::ownerId), Identifier.CODEC.fieldOf("dimension").forGetter(CastAuthority::dimension),
            Identifier.CODEC.fieldOf("scope").forGetter(CastAuthority::scope), UUIDUtil.CODEC.fieldOf("grant").forGetter(CastAuthority::grantId),
            Codec.LONG.fieldOf("revision").forGetter(CastAuthority::revision), UUIDUtil.CODEC.fieldOf("cast").forGetter(CastAuthority::castId)).apply(builder, CastAuthority::new));
    public static final StreamCodec<ByteBuf, CastAuthority> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public CastAuthority {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(grantId, "grantId");
        Objects.requireNonNull(castId, "castId");
    }
}
