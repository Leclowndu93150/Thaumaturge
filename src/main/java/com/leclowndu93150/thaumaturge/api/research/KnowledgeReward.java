package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

/**
 * A knowledge gain attached to a research stage or completion reward.
 *
 * <p>{@code category} resolves through the {@link IResearchCategory#REGISTRY_KEY} datapack
 * registry; the holder may be a direct reference or, for global knowledge, the synthetic global
 * category placed by the bootstrap.
 *
 * @param type the knowledge type
 * @param category the target category
 * @param amount the raw amount to grant; must be positive
 * @since 1.0.0
 */
public record KnowledgeReward(KnowledgeType type, Holder<IResearchCategory> category, int amount) {
    /** Codec for datapack serialization. */
    public static final Codec<KnowledgeReward> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(KnowledgeType.CODEC.fieldOf("type").forGetter(KnowledgeReward::type),
                    RegistryFixedCodec.create(IResearchCategory.REGISTRY_KEY).fieldOf("category").forGetter(KnowledgeReward::category), Codec.INT.fieldOf("amount").forGetter(KnowledgeReward::amount))
            .apply(instance, KnowledgeReward::new));
}
