package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.api.research.scan.Scans;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ScanBootstrap {
    private static final Set<ResourceLocation> DYNAMIC_ASPECTS = new HashSet<>();
    private static final Set<ResourceLocation> DYNAMIC_ENCHANTMENTS = new HashSet<>();

    private ScanBootstrap() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ScanningManager.register(new ScanGeneric());
            ScanningManager.register(new ScanNode());
            ScanningManager.register(new ScanSky());
            for (Holder.Reference<MobEffect> effect :
                    BuiltInRegistries.MOB_EFFECT.holders().toList()) {
                ScanningManager.register(
                        Scans.matching(ScanKeys.effect(effect.key().location()), CarriedTraits.effect(effect)));
            }
        });
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        event.getRegistryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .listElements()
                .forEach(aspect -> {
                    if (DYNAMIC_ASPECTS.add(aspect.key().location())) {
                        ScanningManager.register(new ScanAspectDiscovery(aspect.key()));
                    }
                });
        event.getRegistryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .listElements()
                .forEach(enchantment -> {
                    if (DYNAMIC_ENCHANTMENTS.add(enchantment.key().location())) {
                        ScanningManager.register(Scans.matching(
                                ScanKeys.enchantment(enchantment.key().location()),
                                CarriedTraits.enchantment(enchantment.key())));
                    }
                });
    }
}
