package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record SilverwoodTreeConfig(Block log, Block leaves, int minHeight, int extraHeight, Optional<Block> flower, boolean node) implements FeatureConfiguration {
    public static final Codec<SilverwoodTreeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("log").forGetter(SilverwoodTreeConfig::log), BuiltInRegistries.BLOCK.byNameCodec().fieldOf("leaves").forGetter(SilverwoodTreeConfig::leaves),
            Codec.intRange(1, 32).fieldOf("min_height").forGetter(SilverwoodTreeConfig::minHeight), Codec.intRange(1, 32).fieldOf("extra_height").forGetter(SilverwoodTreeConfig::extraHeight),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("flower").forGetter(SilverwoodTreeConfig::flower), Codec.BOOL.optionalFieldOf("node", false).forGetter(SilverwoodTreeConfig::node))
            .apply(instance, SilverwoodTreeConfig::new));
}
