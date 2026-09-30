package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class CandleBlockColors {
    private CandleBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        for (DyeColor dye : DyeColor.values()) {
            int color = 0xFF000000 | dye.getMapColor().col;
            BlockColor source = (state, level, pos, tintIndex) -> color;
            event.register(source, TCBlocks.CANDLES.get(dye).get());
        }
        BlockColor holderTint = (state, level, pos, tintIndex) -> state.getValue(BlockCandleHolder.CANDLE)
                .dye()
                .map(dye -> 0xFF000000 | dye.getMapColor().col)
                .orElse(0xFFFFFFFF);
        for (DeferredBlock<BlockCandleHolder> holder : TCBlocks.CANDLE_HOLDERS.values()) {
            event.register(holderTint, holder.get());
        }
    }
}
