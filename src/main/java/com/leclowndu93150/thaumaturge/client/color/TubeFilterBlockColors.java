package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeFilter;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.client.color.block.BlockColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TubeFilterBlockColors {
    private static final int UNSET_COLOR = 0xFFFFFFFF;

    private TubeFilterBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColor color = (state, level, pos, tintIndex) -> {
            if (tintIndex != 1 || level == null || pos == null) {
                return UNSET_COLOR;
            }
            if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeFilter tube) || tube.aspectFilter() == null) {
                return UNSET_COLOR;
            }
            var aspect = EssentiaTransportHelper.resolve(tube.getLevel(), tube.aspectFilter());
            return aspect == null ? UNSET_COLOR : 0xFF000000 | aspect.value().color();
        };
        event.register(color, TCBlocks.TUBE_FILTER.get());
    }
}
