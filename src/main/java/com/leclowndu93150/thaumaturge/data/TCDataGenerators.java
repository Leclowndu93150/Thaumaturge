package com.leclowndu93150.thaumaturge.data;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry;
import com.leclowndu93150.thaumaturge.compat.curio.data.TCCurioProvider;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable;
import com.leclowndu93150.thaumaturge.data.damagetype.TCDamageTypeBootstrap;
import com.leclowndu93150.thaumaturge.data.datamap.*;
import com.leclowndu93150.thaumaturge.data.lang.TCEnglishProvider;
import com.leclowndu93150.thaumaturge.data.lang.TCSimplifiedChineseProvider;
import com.leclowndu93150.thaumaturge.data.loot.TCBlockLootSubProvider;
import com.leclowndu93150.thaumaturge.data.loot.TCEntityLootSubProvider;
import com.leclowndu93150.thaumaturge.data.loot.TCGameplayLootSubProvider;
import com.leclowndu93150.thaumaturge.data.loot.TCGlobalLootModifierProvider;
import com.leclowndu93150.thaumaturge.data.model.TCModelProvider;
import com.leclowndu93150.thaumaturge.data.recipe.TCRecipeProvider;
import com.leclowndu93150.thaumaturge.data.tag.TCBiomeTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TCBlockTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TCDamageTypeTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TCEntityTypeTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TCItemTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TCMobEffectTagsProvider;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomeModifiers;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.leclowndu93150.thaumaturge.data.worldgen.blueprint.BlueprintBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.dimension.OuterLandsBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TCConfiguredFeatures;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TCPlacedFeatures;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TCStructureBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.pech.PechTradeBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.research.CategoryBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.scan.ScanEntryBootstrap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCDataGenerators {
    private TCDataGenerators() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        RegistrySetBuilder registries = new RegistrySetBuilder().add(IAspect.REGISTRY_KEY, AspectBootstrap::bootstrap).add(IResearchCategory.REGISTRY_KEY, CategoryBootstrap::bootstrap)
                .add(ScanEntry.REGISTRY_KEY, ScanEntryBootstrap::bootstrap).add(PechTradeTable.REGISTRY_KEY, PechTradeBootstrap::bootstrap).add(Blueprint.REGISTRY_KEY, BlueprintBootstrap::bootstrap)
                .add(Registries.DAMAGE_TYPE, TCDamageTypeBootstrap::bootstrap).add(Registries.CONFIGURED_FEATURE, TCConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, TCPlacedFeatures::bootstrap).add(Registries.BIOME, TCBiomes::bootstrap).add(Registries.DIMENSION_TYPE, OuterLandsBootstrap::bootstrapTypes)
                .add(Registries.LEVEL_STEM, OuterLandsBootstrap::bootstrapStems).add(Registries.STRUCTURE, TCStructureBootstrap::bootstrapStructures)
                .add(Registries.STRUCTURE_SET, TCStructureBootstrap::bootstrapSets).add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, TCBiomeModifiers::bootstrap);
        event.createDatapackRegistryObjects(registries);

        event.createProvider(TCEnglishProvider::new);
        event.createProvider(TCSimplifiedChineseProvider::new);

        event.createProvider(TCModelProvider::new);
        event.createProvider(TCRecipeProvider.Runner::new);
        event.createProvider(AuraModifierProvider::new);
        event.createProvider(EntityAspectsProvider::new);
        event.createProvider(ChampionWhitelistProvider::new);
        event.createProvider(InfernalBonusProvider::new);
        event.createProvider(StrippingProvider::new);
        event.createProvider(FuelValuesProvider::new);
        event.createProvider(TCCurioProvider::new);

        event.createBlockAndItemTags(TCBlockTagsProvider::new, TCItemTagsProvider::new);
        event.createProvider(TCDamageTypeTagsProvider::new);
        event.createProvider(TCBiomeTagsProvider::new);
        event.createProvider(TCMobEffectTagsProvider::new);
        event.createProvider(TCEntityTypeTagsProvider::new);

        event.createProvider((output, lookupProvider) -> new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(TCBlockLootSubProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(TCEntityLootSubProvider::new, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(TCGameplayLootSubProvider::new, LootContextParamSets.CHEST)),
                lookupProvider));

        event.createProvider(TCGlobalLootModifierProvider::new);
    }
}
