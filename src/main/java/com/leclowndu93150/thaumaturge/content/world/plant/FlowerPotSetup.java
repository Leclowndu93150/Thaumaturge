package com.leclowndu93150.thaumaturge.content.world.plant;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class FlowerPotSetup {
    private FlowerPotSetup() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FlowerPotBlock flowerPot = (FlowerPotBlock) Blocks.FLOWER_POT;
            flowerPot.addPlant(TTBlocks.SAPLING_GREATWOOD.getId(), TTBlocks.POTTED_SAPLING_GREATWOOD);
            flowerPot.addPlant(TTBlocks.SAPLING_SILVERWOOD.getId(), TTBlocks.POTTED_SAPLING_SILVERWOOD);
            flowerPot.addPlant(TTBlocks.PLANT_SHIMMERLEAF.getId(), TTBlocks.POTTED_SHIMMERLEAF);
            flowerPot.addPlant(TTBlocks.PLANT_CINDERPEARL.getId(), TTBlocks.POTTED_CINDERPEARL);
            flowerPot.addPlant(TTBlocks.PLANT_VISHROOM.getId(), TTBlocks.POTTED_VISHROOM);
        });
    }
}
