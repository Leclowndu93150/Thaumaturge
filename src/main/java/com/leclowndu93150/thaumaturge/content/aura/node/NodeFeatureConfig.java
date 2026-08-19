package com.leclowndu93150.thaumaturge.content.aura.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record NodeFeatureConfig(boolean silverwood, boolean eerie, boolean small, int specialRarity, int baseAura) implements FeatureConfiguration {
    public static final Codec<NodeFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.BOOL.optionalFieldOf("silverwood", false).forGetter(NodeFeatureConfig::silverwood),
            Codec.BOOL.optionalFieldOf("eerie", false).forGetter(NodeFeatureConfig::eerie), Codec.BOOL.optionalFieldOf("small", false).forGetter(NodeFeatureConfig::small),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("special_rarity", NodeGenerator.DEFAULT_SPECIAL_RARITY).forGetter(NodeFeatureConfig::specialRarity),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("base_aura", NodeGenerator.DEFAULT_BASE_AURA).forGetter(NodeFeatureConfig::baseAura)).apply(instance, NodeFeatureConfig::new));
}
