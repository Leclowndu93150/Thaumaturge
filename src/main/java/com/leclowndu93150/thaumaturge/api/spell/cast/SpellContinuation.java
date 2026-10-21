package com.leclowndu93150.thaumaturge.api.spell.cast;

import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
 * @param authority the server-issued machine authority, empty for native casts
 * @param token the issued carrier token, present exactly when authority is present
 * @param sequence the next emission sequence for this token
 * @param legacy whether this originated from an unversioned record with no authority provenance
 * @since 1.0.0
 */
public record SpellContinuation(List<SpellNode> nodes, SpellState state, Optional<UUID> caster, UUID castId, Optional<CastAuthority> authority, Optional<UUID> token, long sequence, boolean legacy) {
    /** Disk codec. */
    public static final Codec<SpellContinuation> CODEC = RecordCodecBuilder
            .<Serialized>create(i -> i
                    .group(SpellNode.CODEC.listOf().fieldOf("nodes").forGetter(Serialized::nodes), SpellState.CODEC.optionalFieldOf("state", SpellState.DEFAULT).forGetter(Serialized::state),
                            UUIDUtil.CODEC.optionalFieldOf("caster").forGetter(Serialized::caster), UUIDUtil.CODEC.fieldOf("cast").forGetter(Serialized::castId),
                            CastAuthority.CODEC.optionalFieldOf("authority").forGetter(Serialized::authority), UUIDUtil.CODEC.optionalFieldOf("token").forGetter(Serialized::token),
                            Codec.LONG.optionalFieldOf("sequence", 0L).forGetter(Serialized::sequence), Codec.intRange(0, 1).optionalFieldOf("version", 0).forGetter(Serialized::version))
                    .apply(i, Serialized::new))
            .flatXmap(Serialized::decode, value -> DataResult
                    .success(new Serialized(value.nodes(), value.state(), value.caster(), value.castId(), value.authority(), value.token(), value.sequence(), value.legacy() ? 0 : 1)));

    /** Network codec mirroring {@link #CODEC}. */
    private static final StreamCodec<ByteBuf, SpellContinuation> LEGACY_STREAM_CODEC = StreamCodec.composite(SpellNode.STREAM_CODEC.apply(ByteBufCodecs.list()), SpellContinuation::nodes,
            SpellState.STREAM_CODEC, SpellContinuation::state, ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), SpellContinuation::caster, UUIDUtil.STREAM_CODEC, SpellContinuation::castId,
            SpellContinuation::new);

    public static final StreamCodec<ByteBuf, SpellContinuation> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SpellContinuation decode(ByteBuf buffer) {
            int start = buffer.readerIndex();
            int marker = ByteBufCodecs.VAR_INT.decode(buffer);
            if (marker != -1) {
                buffer.readerIndex(start);
                SpellContinuation decoded = LEGACY_STREAM_CODEC.decode(buffer);
                return new SpellContinuation(decoded.nodes(), decoded.state(), decoded.caster(), decoded.castId(), Optional.empty(), Optional.empty(), 0, true);
            }
            int version = ByteBufCodecs.VAR_INT.decode(buffer);
            if (version < 0 || version > 1) {
                throw new IllegalArgumentException("Unsupported spell continuation version");
            }
            SpellContinuation legacy = LEGACY_STREAM_CODEC.decode(buffer);
            Optional<CastAuthority> authority = ByteBufCodecs.optional(CastAuthority.STREAM_CODEC).decode(buffer);
            Optional<UUID> token = ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buffer);
            return new SpellContinuation(legacy.nodes(), legacy.state(), legacy.caster(), legacy.castId(), authority, token, ByteBufCodecs.VAR_LONG.decode(buffer), version == 0);
        }

        @Override
        public void encode(ByteBuf buffer, SpellContinuation value) {
            ByteBufCodecs.VAR_INT.encode(buffer, -1);
            ByteBufCodecs.VAR_INT.encode(buffer, value.legacy() ? 0 : 1);
            LEGACY_STREAM_CODEC.encode(buffer, value);
            ByteBufCodecs.optional(CastAuthority.STREAM_CODEC).encode(buffer, value.authority());
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buffer, value.token());
            ByteBufCodecs.VAR_LONG.encode(buffer, value.sequence());
        }
    };

    public SpellContinuation(List<SpellNode> nodes, SpellState state, Optional<UUID> caster, UUID castId) {
        this(nodes, state, caster, castId, Optional.empty(), Optional.empty(), 0, false);
    }

    public SpellContinuation(List<SpellNode> nodes, SpellState state, Optional<UUID> caster, UUID castId, Optional<CastAuthority> authority, Optional<UUID> token, long sequence) {
        this(nodes, state, caster, castId, authority, token, sequence, false);
    }

    /**
     * Canonicalizes the list into an immutable copy.
     */
    public SpellContinuation {
        nodes = List.copyOf(nodes);
        if (authority.isPresent() != token.isPresent() || sequence < 0
                || authority.isPresent() && (legacy || !authority.get().castId().equals(castId) || !caster.equals(Optional.of(authority.get().sourceId())))) {
            throw new IllegalArgumentException("Invalid spell continuation authority");
        }
    }

    /**
     * Whether nothing is left to run.
     *
     * @return true when there are no nodes
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    private record Serialized(List<SpellNode> nodes, SpellState state, Optional<UUID> caster, UUID castId, Optional<CastAuthority> authority, Optional<UUID> token, long sequence, int version) {
        private DataResult<SpellContinuation> decode() {
            try {
                return DataResult.success(new SpellContinuation(nodes, state, caster, castId, authority, token, sequence, version == 0));
            } catch (IllegalArgumentException exception) {
                return DataResult.error(exception::getMessage);
            }
        }
    }
}
