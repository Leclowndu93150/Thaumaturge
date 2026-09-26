package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TubeOnewayBlockColors {
    private static final int MARKER = 0x7380FF;

    private TubeOnewayBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.constant(ARGB.opaque(MARKER))), TCBlocks.TUBE_ONEWAY.get());
    }
}
