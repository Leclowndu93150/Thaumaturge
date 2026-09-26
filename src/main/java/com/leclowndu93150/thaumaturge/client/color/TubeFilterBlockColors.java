package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeFilter;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TubeFilterBlockColors {
    private static final int UNFILTERED = 0xFFFFFFFF;

    private TubeFilterBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.constant(UNFILTERED), new FilterTint()), TCBlocks.TUBE_FILTER.get());
    }

    private static final class FilterTint implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return UNFILTERED;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeFilter filter)) {
                return UNFILTERED;
            }
            AspectList advertised = filter.queryAspects();
            if (advertised.isEmpty()) {
                return UNFILTERED;
            }
            AspectInstance entry = advertised.entries().getFirst();
            return ARGB.opaque(entry.aspect().value().color());
        }
    }
}
