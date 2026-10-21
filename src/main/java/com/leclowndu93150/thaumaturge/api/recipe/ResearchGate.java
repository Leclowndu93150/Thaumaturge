package com.leclowndu93150.thaumaturge.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public record ResearchGate(Identifier entry, Optional<Integer> stage, boolean negate, List<ResearchGate> additionalRequirements) {
    private static final Codec<ResearchGate> LEAF_CODEC = RecordCodecBuilder.create(i -> i.group(Identifier.CODEC.fieldOf("entry").forGetter(ResearchGate::entry),
            Codec.INT.optionalFieldOf("stage").forGetter(ResearchGate::stage), Codec.BOOL.optionalFieldOf("negate", false).forGetter(ResearchGate::negate)).apply(i, ResearchGate::new));

    public static final Codec<ResearchGate> CODEC = Codec.recursive("ResearchGate",
            self -> Codec.either(self.listOf(1, 64).fieldOf("all_of").codec(), LEAF_CODEC).flatXmap(value -> value.map(ResearchGate::decodeRequirements, DataResult::success),
                    gate -> DataResult.success(gate.additionalRequirements().isEmpty() ? Either.right(gate) : Either.left(gate.requirements()))));

    private static final StreamCodec<RegistryFriendlyByteBuf, ResearchGate> LEAF_STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, ResearchGate::entry,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), ResearchGate::stage, ByteBufCodecs.BOOL, ResearchGate::negate, ResearchGate::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchGate> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ResearchGate decode(RegistryFriendlyByteBuf buffer) {
            int start = buffer.readerIndex();
            if (buffer.readVarInt() != 0) {
                buffer.readerIndex(start);
                return LEAF_STREAM_CODEC.decode(buffer);
            }
            int size = buffer.readVarInt();
            if (size < 1 || size > 64) {
                throw new IllegalArgumentException("Invalid research gate requirement count: " + size);
            }
            List<ResearchGate> requirements = new ArrayList<>(size);
            for (int index = 0; index < size; index++) {
                requirements.add(LEAF_STREAM_CODEC.decode(buffer));
            }
            return allOf(requirements);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ResearchGate gate) {
            if (gate.additionalRequirements().isEmpty()) {
                LEAF_STREAM_CODEC.encode(buffer, gate);
                return;
            }
            List<ResearchGate> requirements = gate.requirements();
            buffer.writeVarInt(0);
            buffer.writeVarInt(requirements.size());
            requirements.forEach(requirement -> LEAF_STREAM_CODEC.encode(buffer, requirement));
        }
    };

    public ResearchGate(Identifier entry, Optional<Integer> stage, boolean negate) {
        this(entry, stage, negate, List.of());
    }

    public ResearchGate {
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(stage, "stage");
        additionalRequirements = additionalRequirements.stream().flatMap(gate -> gate.requirements().stream()).distinct().toList();
        if (additionalRequirements.size() > 63) {
            throw new IllegalArgumentException("A research gate supports at most 64 requirements");
        }
    }

    /**
     * Combines gates without discarding their stages or negation. The existing leaf accessors
     * describe the first requirement; use {@link #requirements()} to inspect the full conjunction.
     *
     * @param gates the nonempty list of requirements
     * @return an immutable gate requiring every supplied gate
     */
    public static ResearchGate allOf(List<ResearchGate> gates) {
        List<ResearchGate> requirements = gates.stream().flatMap(gate -> gate.requirements().stream()).distinct().toList();
        if (requirements.isEmpty()) {
            throw new IllegalArgumentException("A research gate requires at least one requirement");
        }
        ResearchGate first = requirements.getFirst();
        return new ResearchGate(first.entry(), first.stage(), first.negate(), requirements.subList(1, requirements.size()));
    }

    private static DataResult<ResearchGate> decodeRequirements(List<ResearchGate> requirements) {
        try {
            return DataResult.success(allOf(requirements));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    /** Returns every leaf requirement, including this gate's entry, stage and negation. */
    public List<ResearchGate> requirements() {
        if (additionalRequirements.isEmpty()) {
            return List.of(this);
        }
        List<ResearchGate> requirements = new ArrayList<>(additionalRequirements.size() + 1);
        requirements.add(new ResearchGate(entry, stage, negate));
        requirements.addAll(additionalRequirements);
        return List.copyOf(requirements);
    }

    private static BiPredicate<Player, ResearchGate> binding;

    /**
     * Binds the gate check implementation. Called by Thaumaturge during mod init; addons must
     * not call this.
     *
     * @param impl the implementation, receiving a null gate for "ungated"
     * @throws IllegalStateException when already bound
     */
    public static void bind(BiPredicate<Player, ResearchGate> impl) {
        if (binding != null) {
            throw new IllegalStateException("ResearchGate already bound");
        }
        binding = impl;
    }

    /**
     * Tests whether the player satisfies the given gate.
     *
     * @param player the player to test
     * @param gate   the gate, or null when ungated
     * @return {@code true} when the player passes, always {@code true} for a null gate
     * @throws IllegalStateException when accessed before the implementation has bound
     */
    public static boolean passes(Player player, @Nullable ResearchGate gate) {
        if (binding == null) {
            throw new IllegalStateException("ResearchGate accessed before binding");
        }
        return gate == null ? binding.test(player, null) : gate.requirements().stream().allMatch(requirement -> binding.test(player, requirement));
    }
}
