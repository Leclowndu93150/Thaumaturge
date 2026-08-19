package com.leclowndu93150.thaumaturge.content.wands.assembly;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class WandAssemblyPackEvents {
    private WandAssemblyPackEvents() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        event.addRepositorySource(consumer -> {
            PackLocationInfo location = new PackLocationInfo(TCIds.MODID + "_wand_assembly", Component.literal("Thaumaturge Wand Assembly"), PackSource.BUILT_IN, Optional.empty());
            Pack.Metadata metadata = new Pack.Metadata(Component.literal("Wand assembly recipes derived from the cap and rod registries"), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of(),
                    true);
            PackSelectionConfig config = new PackSelectionConfig(true, Pack.Position.BOTTOM, false);
            consumer.accept(new Pack(location, new AssemblySupplier(), metadata, config));
        });
    }

    private static final class AssemblySupplier implements Pack.ResourcesSupplier {
        @Override
        public PackResources openPrimary(PackLocationInfo info) {
            return new WandAssemblyPackResources(info);
        }

        @Override
        public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
            return new WandAssemblyPackResources(info);
        }
    }
}
