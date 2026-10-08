package com.leclowndu93150.thaumaturge.api.spell.cast;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * The stats flowing down one path of a spell tree. Immutable; every change returns a copy.
 *
 * @param stats the written stats; unwritten stats read their default
 * @since 1.0.0
 */
public record SpellState(Map<ResourceLocation, Float> stats) {
    /** A state where every stat reads its default. */
    public static final SpellState DEFAULT = new SpellState(Map.of());

    /** Disk codec. */
    public static final Codec<SpellState> CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT).xmap(SpellState::new, SpellState::stats);

    /** Network codec. */
    public static final StreamCodec<ByteBuf, SpellState> STREAM_CODEC = ByteBufCodecs.map(
                    HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.FLOAT)
            .map(SpellState::new, state -> new HashMap<>(state.stats()));

    /**
     * Canonicalizes the map into an immutable copy.
     */
    public SpellState {
        stats = Map.copyOf(stats);
    }

    /**
     * Reads a stat.
     *
     * @param stat the stat
     * @return the written value, or the stat's default
     */
    public float get(SpellStat stat) {
        Float value = stats.get(stat.id());
        return value != null ? value : stat.defaultValue();
    }

    /**
     * Whether a flag-like stat is on.
     *
     * @param stat the stat
     * @return true when the value is above zero
     */
    public boolean has(SpellStat stat) {
        return get(stat) > 0.0F;
    }

    /**
     * A copy with a stat set.
     *
     * @param stat  the stat
     * @param value the new value
     * @return the copy
     */
    public SpellState with(SpellStat stat, float value) {
        Map<ResourceLocation, Float> next = new HashMap<>(stats);
        next.put(stat.id(), value);
        return new SpellState(next);
    }

    /**
     * A copy with a stat multiplied.
     *
     * @param stat   the stat
     * @param factor the factor
     * @return the copy
     */
    public SpellState multiply(SpellStat stat, float factor) {
        return with(stat, get(stat) * factor);
    }

    /**
     * A copy with an amount added to a stat.
     *
     * @param stat   the stat
     * @param amount the amount
     * @return the copy
     */
    public SpellState add(SpellStat stat, float amount) {
        return with(stat, get(stat) + amount);
    }

    /**
     * The power stat.
     *
     * @return the power multiplier
     */
    public float power() {
        return get(SpellStats.POWER);
    }
}
