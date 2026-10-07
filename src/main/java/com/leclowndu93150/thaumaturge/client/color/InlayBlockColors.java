package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class InlayBlockColors {
    private static final float UNCHARGED_BRIGHTNESS = 0.3F;

    private InlayBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        BlockTintSource source = state -> {
            int charge = state.getValue(BlockInlay.CHARGE);
            float brightness = charge == 0 ? UNCHARGED_BRIGHTNESS : charge / 15.0F * 0.5F + 0.5F;
            int channel = Mth.clamp((int) (brightness * 255.0F), 0, 255);
            return 0xFF000000 | channel << 16 | channel << 8 | channel;
        };
        event.register(List.of(source), TTBlocks.INLAY.get());
    }
}
