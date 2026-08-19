package com.leclowndu93150.thaumaturge.api.capability;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * Kind of category knowledge a player may accumulate in addition to specific research entries.
 *
 * <p>Each entry advances a player's raw counter for a {@link com.leclowndu93150.thaumaturge.api.research.IResearchCategory category}.
 * The raw counter is divided by {@link #progression()} to yield the visible level reported by
 * {@link IPlayerKnowledge#knowledge(KnowledgeType, net.minecraft.resources.ResourceKey)}; for
 * example, every thirty-second theory yields one level.
 *
 * @since 1.0.0
 */
public enum KnowledgeType implements StringRepresentable {
    THEORY("theory", "T", 32), OBSERVATION("observation", "O", 16);

    /** Codec for datapack and component serialization. */
    public static final Codec<KnowledgeType> CODEC = StringRepresentable.fromEnum(KnowledgeType::values);

    /** Network codec for payload sync. */
    public static final StreamCodec<ByteBuf, KnowledgeType> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], KnowledgeType::ordinal);

    private final String name;
    private final String abbreviation;
    private final int progression;

    KnowledgeType(String name, String abbreviation, int progression) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.progression = progression;
    }

    /**
     * Stable lowercase name. Used as the JSON enum value and the prefix in the persisted map key.
     *
     * @return the lowercase name
     */
    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * Single-letter abbreviation used by HUD overlays and the persisted map key.
     *
     * @return the abbreviation, never empty
     */
    public String abbreviation() {
        return abbreviation;
    }

    /**
     * Divisor used to convert the raw accumulated counter into a visible level.
     *
     * @return the divisor, always positive
     */
    public int progression() {
        return progression;
    }
}
