package com.leclowndu93150.thaumaturge.api.spell.cast;

import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The rest of a spell, held by a carrier (projectile, cloud, mine, wall, summon) until it reaches
 * its targets and resumes the cast through {@code Spells.resume}.
 *
 * @param nodes  the nodes to run next; each receives the same targets
 * @param state  the stats at the point the spell paused
 * @param caster the casting entity, empty when the spell has none
 * @param castId the id shared by everything spawned from one cast
 * @since 1.0.0
 */
public record SpellContinuation(List<SpellNode> nodes, SpellState state, Optional<UUID> caster, UUID castId) {
    /** Disk codec. */
    public static final Codec<SpellContinuation> CODEC = RecordCodecBuilder.create(i -> i.group(
                    SpellNode.CODEC.listOf().fieldOf("nodes").forGetter(SpellContinuation::nodes),
                    SpellState.CODEC
                            .optionalFieldOf("state", SpellState.DEFAULT)
                            .forGetter(SpellContinuation::state),
                    UUIDUtil.CODEC.optionalFieldOf("caster").forGetter(SpellContinuation::caster),
                    UUIDUtil.CODEC.fieldOf("cast").forGetter(SpellContinuation::castId))
            .apply(i, SpellContinuation::new));

    /** Network codec mirroring {@link #CODEC}. */
    public static final StreamCodec<ByteBuf, SpellContinuation> STREAM_CODEC = StreamCodec.composite(
            SpellNode.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SpellContinuation::nodes,
            SpellState.STREAM_CODEC,
            SpellContinuation::state,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            SpellContinuation::caster,
            UUIDUtil.STREAM_CODEC,
            SpellContinuation::castId,
            SpellContinuation::new);

    /**
     * Canonicalizes the list into an immutable copy.
     */
    public SpellContinuation {
        nodes = List.copyOf(nodes);
    }

    /**
     * Whether nothing is left to run.
     *
     * @return true when there are no nodes
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
}
