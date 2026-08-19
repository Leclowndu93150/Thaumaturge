package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record CrystalClusterConfig(List<Entry> crystals, int attempts, int maxTotal, int biomeAspectChance) implements FeatureConfiguration {

    public record Entry(ResourceKey<IAspect> aspect, Block block) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(ResourceKey.codec(IAspect.REGISTRY_KEY).fieldOf("aspect").forGetter(Entry::aspect), BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Entry::block))
                .apply(instance, Entry::new));
    }

    public static final Codec<CrystalClusterConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(Entry.CODEC.listOf().fieldOf("crystals").forGetter(CrystalClusterConfig::crystals),
            Codec.intRange(1, 64).fieldOf("attempts").forGetter(CrystalClusterConfig::attempts), Codec.intRange(1, 4096).fieldOf("max_total").forGetter(CrystalClusterConfig::maxTotal),
            Codec.intRange(1, 100).fieldOf("biome_aspect_chance").forGetter(CrystalClusterConfig::biomeAspectChance)).apply(instance, CrystalClusterConfig::new));
}
