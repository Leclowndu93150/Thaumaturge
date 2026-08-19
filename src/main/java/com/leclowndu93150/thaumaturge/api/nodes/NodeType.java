package com.leclowndu93150.thaumaturge.api.nodes;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The behavioral archetype of an aura node.
 *
 * @since 1.0.0
 */
public enum NodeType implements StringRepresentable {
    /** A plain node with no special behavior. */
    NORMAL("normal"),
    /** Periodically leaks aspect orbs and may calm down when stabilized. */
    UNSTABLE("unstable"),
    /** A sinister node that calls undead to nearby players. */
    DARK("dark"),
    /** Feeds on flux instead of vis and spreads taint around itself. */
    TAINTED("tainted"),
    /** Pulls in and consumes entities and blocks, growing from what it kills. */
    HUNGRY("hungry"),
    /** A benevolent node that slowly cleanses flux from its chunk. */
    PURE("pure");

    /** Codec keyed by the lowercase name. */
    public static final Codec<NodeType> CODEC = StringRepresentable.fromEnum(NodeType::values);
    /** Network codec using the enum ordinal. */
    public static final StreamCodec<ByteBuf, NodeType> STREAM_CODEC = ByteBufCodecs.idMapper(id -> NodeType.values()[id], NodeType::ordinal);

    private final String name;

    NodeType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
