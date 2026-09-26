package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.leclowndu93150.thaumaturge.registry.TCBiomeTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class TCBiomeTagsProvider extends KeyTagProvider<Biome> {
    public TCBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BIOME, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TCBiomeTags.HAS_GREATWOOD).add(Biomes.FOREST).add(Biomes.FLOWER_FOREST).add(Biomes.BIRCH_FOREST).add(Biomes.OLD_GROWTH_BIRCH_FOREST).add(Biomes.DARK_FOREST).add(TCBiomes.MAGICAL_FOREST);
        tag(TCBiomeTags.HAS_GREATWOOD_RARE).add(Biomes.TAIGA).add(Biomes.OLD_GROWTH_PINE_TAIGA).add(Biomes.OLD_GROWTH_SPRUCE_TAIGA).add(Biomes.SAVANNA).add(Biomes.SAVANNA_PLATEAU).add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP);
        tag(TCBiomeTags.HAS_SILVERWOOD).add(TCBiomes.MAGICAL_FOREST);
        tag(TCBiomeTags.HAS_CINDERPEARL).add(Biomes.DESERT).add(Biomes.BADLANDS).add(Biomes.ERODED_BADLANDS).add(Biomes.WOODED_BADLANDS);
        tag(BiomeTags.IS_OVERWORLD).add(TCBiomes.MAGICAL_FOREST).add(TCBiomes.EERIE);
        tag(BiomeTags.IS_FOREST).add(TCBiomes.MAGICAL_FOREST);
        tag(TCBiomeTags.IS_MAGICAL).add(TCBiomes.MAGICAL_FOREST).add(TCBiomes.EERIE);
        tag(TCBiomeTags.IS_SPOOKY).add(Biomes.DARK_FOREST).add(TCBiomes.EERIE);
        tag(TCBiomeTags.HAS_ELDRITCH_OBELISK).add(Biomes.PLAINS).add(Biomes.SUNFLOWER_PLAINS).add(Biomes.DESERT).add(Biomes.SAVANNA).add(Biomes.TAIGA).add(Biomes.SNOWY_PLAINS).add(Biomes.SNOWY_TAIGA)
                .add(Biomes.SWAMP).add(Biomes.FOREST).add(Biomes.DARK_FOREST);

        tag(TCBiomeTags.HAS_BRAINY_HUSK).add(Biomes.DESERT);

        tag(TCBiomeTags.HAS_MOUND).add(Biomes.PLAINS).add(Biomes.SUNFLOWER_PLAINS).add(Biomes.FOREST).add(Biomes.FLOWER_FOREST).add(Biomes.BIRCH_FOREST).add(Biomes.OLD_GROWTH_BIRCH_FOREST)
                .add(Biomes.DARK_FOREST).add(Biomes.TAIGA).add(Biomes.SAVANNA).add(Biomes.MEADOW).add(TCBiomes.MAGICAL_FOREST).add(TCBiomes.EERIE);
    }
}
