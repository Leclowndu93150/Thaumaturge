package com.leclowndu93150.thaumaturge.data.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

final class TTBiomeTagsProviderTest {
    @Test
    void eldritchIsExcludedFromSilverwoodAndOverworldTags() throws ReflectiveOperationException {
        TTBiomeTagsProvider provider = new TTBiomeTagsProvider(
                new PackOutput(Path.of("build/test-data")),
                CompletableFuture.completedFuture(Mockito.mock(HolderLookup.Provider.class)));
        provider.addTags(null);

        TagBuilder silverwood = builder(provider, TTBiomeTags.HAS_SILVERWOOD);
        Set<ResourceLocation> silverwoodExclusions =
                silverwood.getRemoveEntries().map(TagEntry::getId).collect(Collectors.toSet());
        assertEquals(
                Set.of(
                        TTBiomes.MAGICAL_FOREST.location(),
                        TTBiomes.TAINTED_LANDS.location(),
                        TTBiomes.ELDRITCH.location(),
                        Biomes.GROVE.location()),
                silverwoodExclusions);

        Set<ResourceLocation> overworldBiomes = builder(provider, BiomeTags.IS_OVERWORLD).build().stream()
                .filter(entry -> !entry.isTag())
                .map(TagEntry::getId)
                .collect(Collectors.toSet());
        assertFalse(overworldBiomes.contains(TTBiomes.ELDRITCH.location()));
        assertEquals(
                Set.of(
                        TTBiomes.MAGICAL_FOREST.location(),
                        TTBiomes.MAGICAL_FOREST_CAVES.location(),
                        TTBiomes.EERIE.location(),
                        TTBiomes.TAINTED_LANDS.location()),
                overworldBiomes);
    }

    private static TagBuilder builder(TTBiomeTagsProvider provider, TagKey<Biome> key)
            throws ReflectiveOperationException {
        Field buildersField = TagsProvider.class.getDeclaredField("builders");
        buildersField.setAccessible(true);
        Map<?, ?> builders = (Map<?, ?>) buildersField.get(provider);
        return (TagBuilder) builders.get(key.location());
    }
}
