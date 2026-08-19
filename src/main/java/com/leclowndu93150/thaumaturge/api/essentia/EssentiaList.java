package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Persisted essentia payload, a thin wrapper around {@link AspectList} that carries the same
 * (aspect, amount) entries but lives in its own type so {@code DataComponentType} registrations
 * separate raw aspect tagging from stored essentia.
 *
 * <p>The codecs delegate to {@link AspectList#CODEC} and {@link AspectList#STREAM_CODEC}; the
 * wire shape is identical.
 *
 * @param contents the aspect quantities stored, never {@code null}
 * @since 1.0.0
 */
public record EssentiaList(AspectList contents) {
    /** Empty essentia payload. */
    public static final EssentiaList EMPTY = new EssentiaList(AspectList.EMPTY);

    /** Datapack codec. Wraps {@link AspectList#CODEC}. */
    public static final Codec<EssentiaList> CODEC = AspectList.CODEC.xmap(EssentiaList::new, EssentiaList::contents);

    /** Network codec. Wraps {@link AspectList#STREAM_CODEC}. */
    public static final StreamCodec<RegistryFriendlyByteBuf, EssentiaList> STREAM_CODEC = AspectList.STREAM_CODEC.map(EssentiaList::new, EssentiaList::contents);

    public EssentiaList {
        if (contents == null) {
            throw new IllegalArgumentException("EssentiaList contents must not be null");
        }
    }

    /**
     * The total essentia amount across all aspects.
     *
     * @return the sum of all entry amounts
     */
    public int totalAmount() {
        return contents.totalAmount();
    }

    /**
     * Whether this payload carries no essentia.
     *
     * @return {@code true} when no aspects are present
     */
    public boolean isEmpty() {
        return contents.isEmpty();
    }
}
