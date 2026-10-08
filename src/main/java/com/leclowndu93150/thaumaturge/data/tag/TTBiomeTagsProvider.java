package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

public final class TTBiomeTagsProvider extends TagsProvider<Biome> {
    public TTBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BIOME, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TTBiomeTags.HAS_GREATWOOD)
                .add(Biomes.FOREST)
                .add(Biomes.FLOWER_FOREST)
                .add(Biomes.BIRCH_FOREST)
                .add(Biomes.OLD_GROWTH_BIRCH_FOREST)
                .add(Biomes.DARK_FOREST)
                .add(TTBiomes.MAGICAL_FOREST)
                .add(TTBiomes.TAINTED_LANDS);
        tag(TTBiomeTags.HAS_GREATWOOD_RARE)
                .add(Biomes.TAIGA)
                .add(Biomes.OLD_GROWTH_PINE_TAIGA)
                .add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
                .add(Biomes.SAVANNA)
                .add(Biomes.SAVANNA_PLATEAU)
                .add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.SWAMP)
                .add(Biomes.MANGROVE_SWAMP);
        tag(TTBiomeTags.HAS_SILVERWOOD)
                .add(Biomes.FOREST)
                .add(Biomes.BIRCH_FOREST)
                .addTag(TTBiomeTags.IS_MAGICAL)
                .addOptionalTag(Tags.Biomes.IS_MAGICAL)
                .remove(TTBiomes.MAGICAL_FOREST, TTBiomes.TAINTED_LANDS);
        tag(TTBiomeTags.HAS_PECH).addTag(TTBiomeTags.IS_MAGICAL).remove(TTBiomes.ELDRITCH);
        tag(TTBiomeTags.HAS_CINDERPEARL)
                .add(Biomes.DESERT)
                .add(Biomes.BADLANDS)
                .add(Biomes.ERODED_BADLANDS)
                .add(Biomes.WOODED_BADLANDS);
        tag(BiomeTags.IS_OVERWORLD)
                .add(TTBiomes.MAGICAL_FOREST)
                .add(TTBiomes.MAGICAL_FOREST_CAVES)
                .add(TTBiomes.EERIE)
                .add(TTBiomes.ELDRITCH)
                .add(TTBiomes.TAINTED_LANDS);
        tag(BiomeTags.IS_FOREST).add(TTBiomes.MAGICAL_FOREST);
        tag(TTBiomeTags.IS_MAGICAL)
                .add(TTBiomes.MAGICAL_FOREST)
                .add(TTBiomes.MAGICAL_FOREST_CAVES)
                .add(TTBiomes.EERIE)
                .add(TTBiomes.ELDRITCH)
                .add(TTBiomes.TAINTED_LANDS);
        tag(TTBiomeTags.IS_SPOOKY).add(Biomes.DARK_FOREST).add(TTBiomes.EERIE);
        tag(TTBiomeTags.IS_TAINTED).add(TTBiomes.TAINTED_LANDS);
        tag(TTBiomeTags.HAS_ELDRITCH_OBELISK)
                .add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.DESERT)
                .add(Biomes.SAVANNA)
                .add(Biomes.TAIGA)
                .add(Biomes.SNOWY_PLAINS)
                .add(Biomes.SNOWY_TAIGA)
                .add(Biomes.SWAMP)
                .add(Biomes.FOREST)
                .add(Biomes.DARK_FOREST);

        tag(TTBiomeTags.HAS_BRAINY_HUSK).add(Biomes.DESERT);

        tag(TTBiomeTags.HAS_MOUND)
                .add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.FOREST)
                .add(Biomes.FLOWER_FOREST)
                .add(Biomes.BIRCH_FOREST)
                .add(Biomes.OLD_GROWTH_BIRCH_FOREST)
                .add(Biomes.DARK_FOREST)
                .add(Biomes.TAIGA)
                .add(Biomes.SAVANNA)
                .add(Biomes.MEADOW)
                .add(TTBiomes.MAGICAL_FOREST)
                .add(TTBiomes.EERIE);
    }
}
