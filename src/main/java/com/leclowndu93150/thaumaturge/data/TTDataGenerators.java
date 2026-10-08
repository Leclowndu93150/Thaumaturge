package com.leclowndu93150.thaumaturge.data;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.compat.apothicenchanting.data.EnchantingStatsProvider;
import com.leclowndu93150.thaumaturge.compat.curio.data.TTCurioProvider;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable;
import com.leclowndu93150.thaumaturge.data.damagetype.TTDamageTypeBootstrap;
import com.leclowndu93150.thaumaturge.data.datamap.AuraModifierProvider;
import com.leclowndu93150.thaumaturge.data.datamap.ChampionWhitelistProvider;
import com.leclowndu93150.thaumaturge.data.datamap.EntityAspectsProvider;
import com.leclowndu93150.thaumaturge.data.datamap.FocusTierProvider;
import com.leclowndu93150.thaumaturge.data.datamap.FuelValuesProvider;
import com.leclowndu93150.thaumaturge.data.datamap.GolemAccessoryItemProvider;
import com.leclowndu93150.thaumaturge.data.datamap.InfernalBonusProvider;
import com.leclowndu93150.thaumaturge.data.datamap.StrippingProvider;
import com.leclowndu93150.thaumaturge.data.datamap.TaintedProfileProvider;
import com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthDefinitionBootstrap;
import com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthEncounterBootstrap;
import com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthProcessorBootstrap;
import com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthRoomBootstrap;
import com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthRoomProvider;
import com.leclowndu93150.thaumaturge.data.labyrinth.ObeliskSiteBootstrap;
import com.leclowndu93150.thaumaturge.data.labyrinth.SparseTemplateProvider;
import com.leclowndu93150.thaumaturge.data.labyrinth.TTLabyrinthRoomTagsProvider;
import com.leclowndu93150.thaumaturge.data.lang.TTEnglishProvider;
import com.leclowndu93150.thaumaturge.data.loot.TTBlockLootSubProvider;
import com.leclowndu93150.thaumaturge.data.loot.TTEntityLootSubProvider;
import com.leclowndu93150.thaumaturge.data.loot.TTGameplayLootSubProvider;
import com.leclowndu93150.thaumaturge.data.loot.TTGlobalLootModifierProvider;
import com.leclowndu93150.thaumaturge.data.loot.TTTaintedLootSubProvider;
import com.leclowndu93150.thaumaturge.data.model.TTModelProvider;
import com.leclowndu93150.thaumaturge.data.recipe.TTRecipeProvider;
import com.leclowndu93150.thaumaturge.data.spell.AffinityBootstrap;
import com.leclowndu93150.thaumaturge.data.spell.SpellPartBootstrap;
import com.leclowndu93150.thaumaturge.data.tag.TTBiomeTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TTBlockTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TTDamageTypeTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TTEntityTypeTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TTItemTagsProvider;
import com.leclowndu93150.thaumaturge.data.tag.TTMobEffectTagsProvider;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomeModifiers;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.data.worldgen.blueprint.BlueprintBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.dimension.OuterLandsBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TTConfiguredFeatures;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TTPlacedFeatures;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TTStructureBootstrap;
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

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTDataGenerators {
    private TTDataGenerators() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        RegistrySetBuilder registries = new RegistrySetBuilder()
                .add(IAspect.REGISTRY_KEY, AspectBootstrap::bootstrap)
                .add(IResearchCategory.REGISTRY_KEY, CategoryBootstrap::bootstrap)
                .add(ScanEntry.REGISTRY_KEY, ScanEntryBootstrap::bootstrap)
                .add(PechTradeTable.REGISTRY_KEY, PechTradeBootstrap::bootstrap)
                .add(Blueprint.REGISTRY_KEY, BlueprintBootstrap::bootstrap)
                .add(Registries.DAMAGE_TYPE, TTDamageTypeBootstrap::bootstrap)
                .add(Registries.CONFIGURED_FEATURE, TTConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, TTPlacedFeatures::bootstrap)
                .add(Registries.BIOME, TTBiomes::bootstrap)
                .add(Registries.DIMENSION_TYPE, OuterLandsBootstrap::bootstrapTypes)
                .add(Registries.LEVEL_STEM, OuterLandsBootstrap::bootstrapStems)
                .add(Registries.STRUCTURE, TTStructureBootstrap::bootstrapStructures)
                .add(Registries.STRUCTURE_SET, TTStructureBootstrap::bootstrapSets)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, TTBiomeModifiers::bootstrap)
                .add(SpellPart.REGISTRY_KEY, SpellPartBootstrap::bootstrap)
                .add(AspectAffinity.REGISTRY_KEY, AffinityBootstrap::bootstrap)
                .add(Registries.PROCESSOR_LIST, LabyrinthProcessorBootstrap::bootstrap)
                .add(RoomType.REGISTRY_KEY, LabyrinthRoomBootstrap::bootstrap)
                .add(LabyrinthEncounter.REGISTRY_KEY, LabyrinthEncounterBootstrap::bootstrap)
                .add(LabyrinthDefinition.REGISTRY_KEY, LabyrinthDefinitionBootstrap::bootstrap)
                .add(ObeliskSite.REGISTRY_KEY, ObeliskSiteBootstrap::bootstrap);
        event.createDatapackRegistryObjects(registries);

        event.createProvider(TTEnglishProvider::new);
        event.createProvider(LabyrinthRoomProvider::new);
        event.createProvider(SparseTemplateProvider::new);
        event.createProvider(TTLabyrinthRoomTagsProvider::new);
        event.createProvider(TTModelProvider::new);
        event.createProvider(TTRecipeProvider::new);
        event.createProvider(AuraModifierProvider::new);
        event.createProvider(EntityAspectsProvider::new);
        event.createProvider(ChampionWhitelistProvider::new);
        event.createProvider(InfernalBonusProvider::new);
        event.createProvider(GolemAccessoryItemProvider::new);
        event.createProvider(TaintedProfileProvider::new);
        event.createProvider(StrippingProvider::new);
        event.createProvider(FuelValuesProvider::new);
        event.createProvider(EnchantingStatsProvider::new);
        event.createProvider(FocusTierProvider::new);
        event.createProvider(
                (output, lookupProvider) -> new TTCurioProvider(output, event.getExistingFileHelper(), lookupProvider));

        event.createBlockAndItemTags(
                (output, lookupProvider) ->
                        new TTBlockTagsProvider(output, lookupProvider, event.getExistingFileHelper()),
                (output, lookupProvider, blockTags) ->
                        new TTItemTagsProvider(output, lookupProvider, blockTags, event.getExistingFileHelper()));
        event.createProvider(TTDamageTypeTagsProvider::new);
        event.createProvider(TTBiomeTagsProvider::new);
        event.createProvider(TTEntityTypeTagsProvider::new);
        event.createProvider(TTMobEffectTagsProvider::new);

        event.createProvider((output, lookupProvider) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(TTBlockLootSubProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(
                                TTEntityLootSubProvider::new, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(
                                TTTaintedLootSubProvider::new, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(
                                TTGameplayLootSubProvider::new, LootContextParamSets.CHEST)),
                lookupProvider));

        event.createProvider(TTGlobalLootModifierProvider::new);
    }
}
