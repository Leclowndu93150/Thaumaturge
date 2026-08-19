package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ChampionDataMaps {
    public static final DataMapType<EntityType<?>, Integer> CHAMPION_WHITELIST = DataMapType.builder(TCIds.rl("champion_whitelist"), Registries.ENTITY_TYPE, Codec.intRange(0, 100)).build();

    private ChampionDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(CHAMPION_WHITELIST);
    }
}
