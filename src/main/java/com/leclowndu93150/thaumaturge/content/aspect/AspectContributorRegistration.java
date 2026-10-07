package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.RegisterAspectContributorsEvent;
import com.leclowndu93150.thaumaturge.registry.TTAspectContributors;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AspectContributorRegistration {
    private AspectContributorRegistration() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TTAspectContributors.register(List.of(new CrucibleAspectContributor(), new InfusionAspectContributor(), new CraftingAspectContributor()));
            RegisterAspectContributorsEvent contributors = new RegisterAspectContributorsEvent();
            ModLoader.postEvent(contributors);
            TTAspectContributors.register(contributors.contributors());
        });
    }
}
