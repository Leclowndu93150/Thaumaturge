package com.leclowndu93150.thaumaturge;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellAuthorities;
import com.leclowndu93150.thaumaturge.content.spell.engine.CastAuthorities;
import com.leclowndu93150.thaumaturge.api.aura.AuraBaseline;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAccess;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization;
import com.leclowndu93150.thaumaturge.api.rifts.RiftTransfers;
import com.leclowndu93150.thaumaturge.content.aura.AuraBaselineBindings;
import com.leclowndu93150.thaumaturge.content.entity.RiftOperations;
import com.leclowndu93150.thaumaturge.content.entity.RiftAuthorizations;
import com.leclowndu93150.thaumaturge.content.entity.RiftTransferOperations;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTChunkGenerators;
import com.leclowndu93150.thaumaturge.registry.TTCreativeTabs;
import com.leclowndu93150.thaumaturge.registry.TTDamageTypes;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTEntityDataSerializers;
import com.leclowndu93150.thaumaturge.registry.TTFeatures;
import com.leclowndu93150.thaumaturge.registry.TTFluidTypes;
import com.leclowndu93150.thaumaturge.registry.TTFluids;
import com.leclowndu93150.thaumaturge.registry.TTGolemAccessories;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import com.leclowndu93150.thaumaturge.registry.TTIngredientTypes;
import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthEncounterTypes;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.leclowndu93150.thaumaturge.registry.TTLootTypes;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTObeliskSiteBehaviors;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.leclowndu93150.thaumaturge.registry.TTPlacementModifiers;
import com.leclowndu93150.thaumaturge.registry.TTRecipeSerializers;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.registry.TTSeals;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.registry.TTSpellFx;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.leclowndu93150.thaumaturge.content.spell.engine.SpellBindings;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.SpellRegistries;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.content.equipment.GogglesBindings;
import com.leclowndu93150.thaumaturge.content.equipment.RechargeBindings;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftCost;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionDelegation;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionDelegations;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.api.research.ResearchNotesAccess;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteAccessImpl;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.wands.AspectOrbAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeTransfers;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeTransferOperations;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeAccessBindings;
import com.leclowndu93150.thaumaturge.content.wands.AspectOrbAccessBindings;
import com.leclowndu93150.thaumaturge.registry.TTNodeBehaviors;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.compat.dynamictrees.DynamicTreesCompat;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumaturge.content.aura.AuraHelperBindings;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthBindings;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitBindings;
import com.leclowndu93150.thaumaturge.content.golem.GolemBindings;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionCraftingTransactions;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyRegistryAliases;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolBindings;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanBindings;
import com.leclowndu93150.thaumaturge.content.taint.TaintApiBindings;
import com.leclowndu93150.thaumaturge.content.wands.WandAccessBindings;
import com.leclowndu93150.thaumaturge.content.warp.WarpBindings;
import com.leclowndu93150.thaumaturge.content.workbench.ArcaneCraftingTransactions;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import com.leclowndu93150.thaumaturge.registry.TTBiomeModifierSerializers;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import com.leclowndu93150.thaumaturge.registry.TTStructureProcessors;
import com.leclowndu93150.thaumaturge.registry.TTStructures;
import com.leclowndu93150.thaumaturge.registry.TTTicketTypes;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.lang.reflect.Method;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TTIds.MODID)
public final class Thaumaturge {
    public static final Logger LOGGER = LoggerFactory.getLogger(TTIds.MODID);

    public Thaumaturge(IEventBus modBus, ModContainer container) {
        TTFluidTypes.register(modBus);
        TTFluids.register(modBus);
        TTBlocks.register(modBus);
        TTItems.register(modBus);
        TTFeatures.register(modBus);
        TTTreePlacers.register(modBus);
        TTBiomeModifierSerializers.register(modBus);
        TTStructures.register(modBus);
        TTBlockEntities.register(modBus);
        TTEntities.register(modBus);
        TTMenus.register(modBus);
        TTRecipeTypes.register(modBus);
        TTRecipeSerializers.register(modBus);
        TTDataComponents.register(modBus);
        TTCreativeTabs.register(modBus);
        TTParticles.register(modBus);
        TTSounds.register(modBus);
        TTAttachments.register(modBus);
        TTDamageTypes.register(modBus);
        TTMobEffects.register(modBus);
        TTAttributes.register(modBus);
        TTChunkGenerators.register(modBus);
        TTPlacementModifiers.register(modBus);
        TTSpellBehaviors.register(modBus);
        TTSpellActions.register(modBus);
        TTSpellFx.register(modBus);
        TTGolemTraits.register(modBus);
        TTMobTraits.register(modBus);
        TTGolemParts.register(modBus);
        TTWandParts.register(modBus);
        TTNodeBehaviors.register(modBus);
        TTSeals.register(modBus);
        TTEntityDataSerializers.register(modBus);
        TTIngredientTypes.register(modBus);
        TTLabyrinthMarkers.register(modBus);
        TTLabyrinthEncounterTypes.register(modBus);
        TTObeliskSiteBehaviors.register(modBus);
        TTStructureProcessors.register(modBus);
        TTLootTypes.register(modBus);
        TTInstabilityEffects.register(modBus);
        TTTicketTypes.register(modBus);
        TTGolemAccessories.register();

        LegacyRegistryAliases.register(modBus);

        NeoForge.EVENT_BUS.addListener(TTRecipeTypes::registerSynchronizedRecipes);

        container.registerConfig(ModConfig.Type.COMMON, ThaumaturgeCommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ThaumaturgeClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ThaumaturgeServerConfig.SPEC);

        KnowledgeAccess.bind(player -> player.getData(TTAttachments.KNOWLEDGE));
        AspectIndexAccess.bind(AspectIndexHolder::get);
        WandAccess.bind(new WandAccessBindings());
        AspectOrbAccess.bind(new AspectOrbAccessBindings());
        NodeAccess.bind(new NodeAccessBindings());
        NodeTransfers.bind(new NodeTransferOperations());
        RiftAccess.bind(new RiftOperations());
        RiftAuthorization.bind(new RiftAuthorizations());
        RiftTransfers.bind(new RiftTransferOperations());
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> RiftAuthorizations.freeze());
        EssentiaCrystalAccess.bind(TTItems.ESSENTIA_CRYSTAL, TTDataComponents.CRYSTAL_ASPECT);
        EssentiaAccess.bind(TTDataComponents.ASPECT_FILTER);
        ArcaneCraftCost.bind(WorkbenchPayment::cost);
        ArcaneCraftingTransaction.bind(new ArcaneCraftingTransactions());
        InfusionCraftingTransaction.bind(new InfusionCraftingTransactions());
        InfusionDelegation.bind(new InfusionDelegations());
        AuraHelper.bind(new AuraHelperBindings());
        AuraBaseline.bind(new AuraBaselineBindings());
        VisRelayHelper.bind(new VisRelayNetwork());
        TaintApi.bind(new TaintApiBindings());
        MobTraits.bind(new MobTraitBindings());
        WarpHelper.bind(new WarpBindings());
        ScanningManager.bind(new ScanBindings());
        GolemHelper.bind(new GolemBindings());
        AspectPoolAccess.bind(new AspectPoolBindings());
        ResearchNotesAccess.bind(ResearchNoteAccessImpl.INSTANCE);
        ResearchGate.bind(ResearchManager::doesPassGate);
        RechargeAccess.bind(new RechargeBindings());
        GogglesAccess.bind(new GogglesBindings());
        SpellRegistries.bind(TTSpellBehaviors.registry(), TTSpellActions.registry(), TTSpellFx.registry());
        Spells.bind(new SpellBindings());
        SpellAuthorities.bind(new CastAuthorities());
        LabyrinthHelper.bind(new LabyrinthBindings());

        if (ModList.get().isLoaded(TTIds.CURIOS))
            ThaumaturgeCuriosCompat.init(modBus);
        if (ModList.get().isLoaded(TTIds.DYNAMIC_TREES))
            DynamicTreesCompat.init(modBus);

        wireGameTests(modBus);
    }

    private static void wireGameTests(IEventBus modBus) {
        if (!Boolean.getBoolean("thaumaturge.gametest")) {
            return;
        }
        try {
            Class<?> registration = Class.forName("com.leclowndu93150.thaumaturge.gametest.TTGameTestRegistration");
            Method handler = registration.getMethod("registerTests", RegisterGameTestsEvent.class);
            modBus.addListener(RegisterGameTestsEvent.class, event -> {
                try {
                    handler.invoke(null, event);
                } catch (ReflectiveOperationException e) {
                    LOGGER.error("Failed to invoke TTGameTestRegistration.registerTests", e);
                }
            });
        } catch (ClassNotFoundException expected) {
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to wire gametest hooks", e);
        }
    }
}
