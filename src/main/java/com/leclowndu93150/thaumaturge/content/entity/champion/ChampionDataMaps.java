package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ChampionDataMaps {
    public static final DataMapType<EntityType<?>, Integer> CHAMPION_WHITELIST = DataMapType.builder(
                    TTIds.rl("champion_whitelist"), Registries.ENTITY_TYPE, Codec.intRange(0, 100))
            .build();

    private ChampionDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(CHAMPION_WHITELIST);
    }
}
