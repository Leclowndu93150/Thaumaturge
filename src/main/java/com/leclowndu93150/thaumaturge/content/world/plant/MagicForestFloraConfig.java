package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record MagicForestFloraConfig(
        Block ambientGrass,
        Block vishroom,
        int grassAttempts,
        int vishroomAttempts,
        HolderSet<Block> flowers,
        int flowerAttempts,
        List<PlantPatch> plants,
        HolderSet<ConfiguredFeature<?, ?>> hugeMushrooms,
        int hugeMushroomRarity)
        implements FeatureConfiguration {
    public static final Codec<MagicForestFloraConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BuiltInRegistries.BLOCK
                            .byNameCodec()
                            .fieldOf("ambient_grass")
                            .forGetter(MagicForestFloraConfig::ambientGrass),
                    BuiltInRegistries.BLOCK
                            .byNameCodec()
                            .fieldOf("vishroom")
                            .forGetter(MagicForestFloraConfig::vishroom),
                    Codec.intRange(0, 64).fieldOf("grass_attempts").forGetter(MagicForestFloraConfig::grassAttempts),
                    Codec.intRange(0, 64)
                            .fieldOf("vishroom_attempts")
                            .forGetter(MagicForestFloraConfig::vishroomAttempts),
                    RegistryCodecs.homogeneousList(Registries.BLOCK)
                            .fieldOf("flowers")
                            .forGetter(MagicForestFloraConfig::flowers),
                    Codec.intRange(0, 64).fieldOf("flower_attempts").forGetter(MagicForestFloraConfig::flowerAttempts),
                    PlantPatch.CODEC.listOf().fieldOf("plants").forGetter(MagicForestFloraConfig::plants),
                    RegistryCodecs.homogeneousList(Registries.CONFIGURED_FEATURE)
                            .fieldOf("huge_mushrooms")
                            .forGetter(MagicForestFloraConfig::hugeMushrooms),
                    Codec.intRange(1, 1000000)
                            .fieldOf("huge_mushroom_rarity")
                            .forGetter(MagicForestFloraConfig::hugeMushroomRarity))
            .apply(instance, MagicForestFloraConfig::new));

    public record PlantPatch(BlockStateProvider state, int attempts, int rarity) {
        public static final Codec<PlantPatch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(PlantPatch::state),
                        Codec.intRange(0, 64).fieldOf("attempts").forGetter(PlantPatch::attempts),
                        Codec.intRange(1, 1000000).optionalFieldOf("rarity", 1).forGetter(PlantPatch::rarity))
                .apply(instance, PlantPatch::new));
    }
}
