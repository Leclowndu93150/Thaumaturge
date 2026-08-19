package com.leclowndu93150.thaumaturge.api.capability;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * Per-entry user-interface flag set on a player's research record.
 *
 * <p>Flags are independent of completion: a research entry can be complete and still carry
 * {@link #POPUP} until the Thaumonomicon clears it.
 *
 * @since 1.0.0
 */
public enum ResearchFlag implements StringRepresentable {
    /** The player has read this entry in the Thaumonomicon. */
    PAGE("page"),
    /** The entry is currently being researched. */
    RESEARCH("research"),
    /** A popup is pending for this entry. */
    POPUP("popup");

    /** Codec for datapack and component serialization. */
    public static final Codec<ResearchFlag> CODEC = StringRepresentable.fromEnum(ResearchFlag::values);

    /** Network codec for payload sync. */
    public static final StreamCodec<ByteBuf, ResearchFlag> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], ResearchFlag::ordinal);

    private final String name;

    ResearchFlag(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
