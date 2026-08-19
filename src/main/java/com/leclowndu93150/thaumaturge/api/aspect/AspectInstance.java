package com.leclowndu93150.thaumaturge.api.aspect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;

/**
 * A quantity of a specific aspect. Used as the element type of {@link AspectList} and as the
 * shape that crosses save and network boundaries.
 *
 * @param aspect the aspect, referenced by holder so the entry survives datapack reloads
 * @param amount the amount in vis units; required to be positive
 * @since 1.0.0
 */
public record AspectInstance(Holder<IAspect> aspect, int amount) {
    /** Codec used in datapack JSON. References aspects by their registry id. */
    public static final Codec<AspectInstance> CODEC = RecordCodecBuilder
            .create(builder -> builder.group(RegistryFixedCodec.create(IAspect.REGISTRY_KEY).fieldOf("aspect").forGetter(AspectInstance::aspect),
                    Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("amount", 1).forGetter(AspectInstance::amount)).apply(builder, AspectInstance::new));

    /** Network codec. Syncs the holder by registry id, not by value. */
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectInstance> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(IAspect.REGISTRY_KEY), AspectInstance::aspect,
            ByteBufCodecs.VAR_INT, AspectInstance::amount, AspectInstance::new);

    public AspectInstance {
        if (amount < 1) {
            throw new IllegalArgumentException("AspectInstance amount must be positive, was " + amount);
        }
    }

    /**
     * Returns an instance with the same aspect and a different amount.
     *
     * @param newAmount the new amount, must be positive
     * @return a new instance
     * @throws IllegalArgumentException if {@code newAmount} is not positive
     */
    public AspectInstance withAmount(int newAmount) {
        return new AspectInstance(aspect, newAmount);
    }
}
