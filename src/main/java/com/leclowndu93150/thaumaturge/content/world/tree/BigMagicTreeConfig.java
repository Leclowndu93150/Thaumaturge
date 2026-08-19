package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record BigMagicTreeConfig(Block log, Block leaves) implements FeatureConfiguration {
    public static final Codec<BigMagicTreeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("log").forGetter(BigMagicTreeConfig::log),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("leaves").forGetter(BigMagicTreeConfig::leaves)).apply(instance, BigMagicTreeConfig::new));
}
