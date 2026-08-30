package com.leclowndu93150.thaumaturge.content.legacy;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class LegacyRegistryAliases {
    private static final List<RenamedEntry> RENAMES =
            List.of(new RenamedEntry(Registries.ITEM, TCIds.rl("turret_bore"), TCIds.rl("arcane_bore")));

    private LegacyRegistryAliases() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterEvent.class, LegacyRegistryAliases::onRegister);
        NeoForge.EVENT_BUS.addListener(ServerAboutToStartEvent.class, LegacyRegistryAliases::onServerAboutToStart);
    }

    private static void onRegister(RegisterEvent event) {
        alias(event.getRegistry());
    }

    private static void onServerAboutToStart(ServerAboutToStartEvent event) {
        event.getServer().registryAccess().registries().forEach(entry -> alias(entry.value()));
    }

    private static void alias(Registry<?> registry) {
        for (ResourceLocation id : List.copyOf(registry.keySet())) {
            if (!TCIds.MODID.equals(id.getNamespace())) {
                continue;
            }
            ResourceLocation legacy = ResourceLocation.fromNamespaceAndPath(LegacyIds.LEGACY_NAMESPACE, id.getPath());
            if (registry.containsKey(legacy) || !registry.resolve(legacy).equals(legacy)) {
                continue;
            }
            registry.addAlias(legacy, id);
        }
        for (RenamedEntry rename : RENAMES) {
            if (!registry.key().equals(rename.registry()) || !registry.containsKey(rename.current())) {
                continue;
            }
            if (registry.containsKey(rename.legacy())
                    || !registry.resolve(rename.legacy()).equals(rename.legacy())) {
                continue;
            }
            registry.addAlias(rename.legacy(), rename.current());
        }
    }

    private record RenamedEntry(
            ResourceKey<? extends Registry<?>> registry, ResourceLocation legacy, ResourceLocation current) {}
}
