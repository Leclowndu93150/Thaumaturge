package com.leclowndu93150.thaumaturge.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public record ResearchGate(Identifier entry, Optional<Integer> stage, boolean negate) {
    public static final Codec<ResearchGate> CODEC = RecordCodecBuilder.create(i -> i.group(Identifier.CODEC.fieldOf("entry").forGetter(ResearchGate::entry),
            Codec.INT.optionalFieldOf("stage").forGetter(ResearchGate::stage), Codec.BOOL.optionalFieldOf("negate", false).forGetter(ResearchGate::negate)).apply(i, ResearchGate::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchGate> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, ResearchGate::entry,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), ResearchGate::stage, ByteBufCodecs.BOOL, ResearchGate::negate, ResearchGate::new);

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
        return binding.test(player, gate);
    }
}
