package com.leclowndu93150.thaumaturge;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftCost;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchVisSourcesEvent;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexBuilder;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumaturge.content.aura.AuraHelperBindings;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayWorkbenchSource;
import com.leclowndu93150.thaumaturge.content.golem.GolemBindings;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyRegistryAliases;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolBindings;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanBindings;
import com.leclowndu93150.thaumaturge.content.taint.TaintApiBindings;
import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import com.leclowndu93150.thaumaturge.registry.*;
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

@Mod(TCIds.MODID)
public final class Thaumaturge {
    public static final Logger LOGGER = LoggerFactory.getLogger(TCIds.MODID);

    public Thaumaturge(IEventBus modBus, ModContainer container) {
        TCFluidTypes.register(modBus);
        TCFluids.register(modBus);
        TCBlocks.register(modBus);
        TCItems.register(modBus);
        TCFeatures.register(modBus);
        TCStructures.register(modBus);
        TCBlockEntities.register(modBus);
        TCEntities.register(modBus);
        TCMenus.register(modBus);
        TCRecipeTypes.register(modBus);
        TCRecipeSerializers.register(modBus);
        TCDataComponents.register(modBus);
        TCCreativeTabs.register(modBus);
        TCParticles.register(modBus);
        TCSounds.register(modBus);
        TCAttachments.register(modBus);
        TCDamageTypes.register(modBus);
        TCMobEffects.register(modBus);
        TCAttributes.register(modBus);
        TCChunkGenerators.register(modBus);
        TCPlacementModifiers.register(modBus);
        TCFocusElements.register(modBus);
        TCGolemTraits.register(modBus);
        TCGolemParts.register(modBus);
        TCWandParts.register(modBus);
        TCSeals.register(modBus);
        TCEntityDataSerializers.register(modBus);

        LegacyRegistryAliases.register(modBus);

        NeoForge.EVENT_BUS.addListener(TCRecipeTypes::registerSynchronizedRecipes);

        container.registerConfig(ModConfig.Type.COMMON, ThaumaturgeCommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ThaumaturgeClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ThaumaturgeServerConfig.SPEC);

        KnowledgeAccess.bind(player -> player.getData(TCAttachments.KNOWLEDGE));
        AspectIndexAccess.bind(AspectIndexHolder::get);
        AspectIndexBuilder.fireContributorEvent(modBus);
        WandAccess.bind(TCDataComponents.WAND_VIS);
        ArcaneCraftCost.bind(WorkbenchPayment::cost);
        RegisterWorkbenchVisSourcesEvent visSourcesEvent = new RegisterWorkbenchVisSourcesEvent();
        visSourcesEvent.register(new VisRelayWorkbenchSource());
        modBus.post(visSourcesEvent);
        WorkbenchPayment.registerSources(visSourcesEvent.sources());
        AuraHelper.bind(new AuraHelperBindings());
        VisRelayHelper.bind(new VisRelayNetwork());
        TaintApi.bind(new TaintApiBindings());
        WarpHelper.bind(new WarpManager.Bindings());
        ScanningManager.bind(new ScanBindings());
        GolemHelper.bind(new GolemBindings());
        AspectPoolAccess.bind(new AspectPoolBindings());
        ResearchGate.bind(ResearchManager::doesPassGate);
        RechargeAccess.bind(TCDataComponents.CHARGE);
        GogglesAccess.bind(() -> TCAttributes.VIS_DISCOUNT);
        FocusEngine.bindRegistry(TCFocusElements.registry());

        if (ModList.get().isLoaded(TCIds.CURIOS))
            ThaumaturgeCuriosCompat.init(modBus);

        wireGameTests(modBus);
    }

    private static void wireGameTests(IEventBus modBus) {
        if (!Boolean.getBoolean("thaumaturge.gametest")) {
            return;
        }
        try {
            Class<?> registration = Class.forName("com.leclowndu93150.thaumaturge.gametest.TCGameTestRegistration");
            Method handler = registration.getMethod("registerTests", RegisterGameTestsEvent.class);
            modBus.addListener(RegisterGameTestsEvent.class, event -> {
                try {
                    handler.invoke(null, event);
                } catch (ReflectiveOperationException e) {
                    LOGGER.error("Failed to invoke TCGameTestRegistration.registerTests", e);
                }
            });
        } catch (ClassNotFoundException expected) {
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to wire gametest hooks", e);
        }
    }
}
