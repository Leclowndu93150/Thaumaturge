package com.leclowndu93150.thaumaturge.api.nodes;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The vigor of an aura node. Absent modifier means an average node; the modifier scales how
 * often the node refills itself and how brightly it renders.
 *
 * @since 1.0.0
 */
public enum NodeModifier implements StringRepresentable {
    /** Refills faster and renders more intensely. */
    BRIGHT("bright"),
    /** Refills slower and renders washed out. */
    PALE("pale"),
    /** Does not refill at all and flickers. */
    FADING("fading");

    /** Codec keyed by the lowercase name. */
    public static final Codec<NodeModifier> CODEC = StringRepresentable.fromEnum(NodeModifier::values);
    /** Network codec using the enum ordinal. */
    public static final StreamCodec<ByteBuf, NodeModifier> STREAM_CODEC = ByteBufCodecs.idMapper(id -> NodeModifier.values()[id], NodeModifier::ordinal);

    private final String name;

    NodeModifier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
