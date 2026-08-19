package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record MagicForestFloraConfig(Block ambientGrass, Block vishroom, int grassAttempts, int vishroomAttempts) implements FeatureConfiguration {
    public static final Codec<MagicForestFloraConfig> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("ambient_grass").forGetter(MagicForestFloraConfig::ambientGrass),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("vishroom").forGetter(MagicForestFloraConfig::vishroom),
                    Codec.intRange(0, 64).fieldOf("grass_attempts").forGetter(MagicForestFloraConfig::grassAttempts),
                    Codec.intRange(0, 64).fieldOf("vishroom_attempts").forGetter(MagicForestFloraConfig::vishroomAttempts)).apply(instance, MagicForestFloraConfig::new));
}
