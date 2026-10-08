package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class SmelterDataMaps {
    public static final DataMapType<Block, SmelterStats> SMELTER_STATS = DataMapType.builder(
                    TTIds.rl("smelter_stats"), Registries.BLOCK, SmelterStats.CODEC)
            .build();

    private SmelterDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(SMELTER_STATS);
    }
}
