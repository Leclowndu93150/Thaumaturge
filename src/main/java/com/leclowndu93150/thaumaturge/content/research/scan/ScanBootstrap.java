package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.Scans;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ScanBootstrap {
    private static final Set<Identifier> DYNAMIC_ASPECTS = new HashSet<>();
    private static final Set<Identifier> DYNAMIC_ENCHANTMENTS = new HashSet<>();

    private ScanBootstrap() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ScanningManager.register(new ScanGeneric());
            ScanningManager.register(new ScanNode());
            ScanningManager.register(new ScanSky());
            for (Holder.Reference<MobEffect> effect : BuiltInRegistries.MOB_EFFECT.listElements().toList()) {
                ScanningManager.register(Scans.matching(ScanKeys.effect(effect.key().identifier()), CarriedTraits.effect(effect)));
            }
        });
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        event.getLookupProvider().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(aspect -> {
            if (DYNAMIC_ASPECTS.add(aspect.key().identifier())) {
                ScanningManager.register(new ScanAspectDiscovery(aspect.key()));
            }
        });
        event.getLookupProvider().lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(enchantment -> {
            if (DYNAMIC_ENCHANTMENTS.add(enchantment.key().identifier())) {
                ScanningManager.register(Scans.matching(ScanKeys.enchantment(enchantment.key().identifier()), CarriedTraits.enchantment(enchantment.key())));
            }
        });
    }
}
