package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class CandleBlockColors {
    private CandleBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        for (DyeColor dye : DyeColor.values()) {
            int color = 0xFF000000 | dye.getMapColor().col;
            event.register(List.of(BlockTintSources.constant(color)), TCBlocks.CANDLES.get(dye).get());
        }
    }

    @SubscribeEvent
    public static void onRegisterItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(TCIds.rl("aspect_filter"), AspectFilterTint.MAP_CODEC);
    }
}
