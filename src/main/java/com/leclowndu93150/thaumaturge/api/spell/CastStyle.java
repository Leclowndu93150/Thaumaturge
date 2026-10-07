package com.leclowndu93150.thaumaturge.api.spell;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * How a caster releases a spell.
 *
 * @since 1.0.0
 */
public enum CastStyle implements StringRepresentable {
    /** Casts once on use, then waits out a cooldown scaled by complexity. */
    INSTANT("instant"),
    /** Charges while use is held; releasing casts with power scaled by the charge. */
    CHARGED("charged"),
    /** Pulses on a fixed interval while use is held, draining vis every pulse. */
    CHANNELED("channeled");

    /** Serializes by {@link #getSerializedName()}. */
    public static final Codec<CastStyle> CODEC = StringRepresentable.fromEnum(CastStyle::values);

    /** Network encoding by ordinal. */
    public static final StreamCodec<ByteBuf, CastStyle> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], CastStyle::ordinal);

    private final String name;

    CastStyle(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * The translation key of this style's display name.
     *
     * @return {@code spell.thaumaturge.style.<name>}
     */
    public String nameKey() {
        return "spell.thaumaturge.style." + name;
    }

    /**
     * The style after this one, wrapping around.
     *
     * @return the next style
     */
    public CastStyle next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
