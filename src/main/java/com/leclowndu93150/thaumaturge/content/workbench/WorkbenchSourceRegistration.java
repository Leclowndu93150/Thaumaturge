package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchAuraSourcesEvent;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchVisSourcesEvent;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayWorkbenchSource;
import com.leclowndu93150.thaumaturge.registry.TTWorkbenchSources;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class WorkbenchSourceRegistration {
    private WorkbenchSourceRegistration() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RegisterWorkbenchVisSourcesEvent visSources = new RegisterWorkbenchVisSourcesEvent();
            visSources.register(new VisRelayWorkbenchSource());
            ModLoader.postEvent(visSources);
            TTWorkbenchSources.registerVisSources(visSources.sources());

            RegisterWorkbenchAuraSourcesEvent auraSources = new RegisterWorkbenchAuraSourcesEvent();
            ModLoader.postEvent(auraSources);
            TTWorkbenchSources.registerAuraSources(auraSources.sources());
        });
    }
}
