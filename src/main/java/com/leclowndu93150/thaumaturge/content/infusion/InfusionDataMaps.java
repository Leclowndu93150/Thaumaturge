package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class InfusionDataMaps {
    public static final DataMapType<Block, InfusionModifier> PILLAR_SET = DataMapType.builder(
                    TTIds.rl("infusion_pillar_set"), Registries.BLOCK, InfusionModifier.CODEC)
            .build();
    public static final DataMapType<Block, InfusionModifier> MATRIX_UPGRADE = DataMapType.builder(
                    TTIds.rl("infusion_matrix_upgrade"), Registries.BLOCK, InfusionModifier.CODEC)
            .build();
    public static final DataMapType<Block, InfusionModifier> PEDESTAL = DataMapType.builder(
                    TTIds.rl("infusion_pedestal"), Registries.BLOCK, InfusionModifier.CODEC)
            .build();

    private InfusionDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(PILLAR_SET);
        event.register(MATRIX_UPGRADE);
        event.register(PEDESTAL);
    }
}
