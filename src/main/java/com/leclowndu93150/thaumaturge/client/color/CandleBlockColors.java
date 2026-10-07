package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class CandleBlockColors {
    private CandleBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        for (DyeColor dye : DyeColor.values()) {
            int color = 0xFF000000 | dye.getMapColor().col;
            event.register(List.of(BlockTintSources.constant(color)), TTBlocks.CANDLES.get(dye).get());
        }
        for (DeferredBlock<BlockCandleHolder> holder : TTBlocks.CANDLE_HOLDERS.values()) {
            event.register(List.of(new CandleHolderTint()), holder.get());
        }
    }

    @SubscribeEvent
    public static void onRegisterItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(TTIds.rl("aspect_filter"), AspectFilterTint.MAP_CODEC);
    }
}
