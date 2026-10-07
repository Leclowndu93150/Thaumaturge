package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class DynamicTreesResourcePack {
    private DynamicTreesResourcePack() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !ModList.get().isLoaded(TTIds.DYNAMIC_TREES)) {
            return;
        }
        event.addPackFinders(TTIds.DYNAMIC_TREES_RESOURCE_PACK, PackType.CLIENT_RESOURCES, Component.translatable("pack.thaumaturge.dynamic_trees"), PackSource.BUILT_IN, true, Pack.Position.TOP);
    }
}
