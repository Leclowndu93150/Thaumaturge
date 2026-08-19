package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record BigTreeConfig(Block log, Block leaves, int trunkSize, double heightAttenuation, double branchSlope, double scaleWidth, boolean doubleCanopy,
        float spiderChance) implements FeatureConfiguration {
    public static final Codec<BigTreeConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("log").forGetter(BigTreeConfig::log), BuiltInRegistries.BLOCK.byNameCodec().fieldOf("leaves").forGetter(BigTreeConfig::leaves),
                    Codec.intRange(1, 2).fieldOf("trunk_size").forGetter(BigTreeConfig::trunkSize), Codec.DOUBLE.fieldOf("height_attenuation").forGetter(BigTreeConfig::heightAttenuation),
                    Codec.DOUBLE.fieldOf("branch_slope").forGetter(BigTreeConfig::branchSlope), Codec.DOUBLE.fieldOf("scale_width").forGetter(BigTreeConfig::scaleWidth),
                    Codec.BOOL.fieldOf("double_canopy").forGetter(BigTreeConfig::doubleCanopy), Codec.floatRange(0.0F, 1.0F).fieldOf("spider_chance").forGetter(BigTreeConfig::spiderChance))
            .apply(instance, BigTreeConfig::new));
}
