package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class PlantBlockColors {
    private static final float MANA_POD_HERBA_R = 37.0F / 255.0F;
    private static final float MANA_POD_HERBA_G = 157.0F / 255.0F;
    private static final float MANA_POD_HERBA_B = 117.0F / 255.0F;
    private static final int MANA_POD_TINT_START_AGE = 2;
    private static final float MANA_POD_TINT_SPAN = BlockEntityManaPod.MAX_AGE - MANA_POD_TINT_START_AGE;

    private PlantBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.grassBlock()), TTBlocks.GRASS_AMBIENT.get());
        event.register(List.of(BlockTintSources.foliage()), TTBlocks.LEAVES_GREATWOOD.get());
        event.register(List.of(new ManaPodTint()), TTBlocks.MANA_POD.get());
    }

    private static final class ManaPodTint implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return ARGB.opaque(0xFFFFFF);
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            if (!(level.getBlockEntity(pos) instanceof BlockEntityManaPod pod)) {
                return color(state);
            }
            Holder<IAspect> aspect = pod.aspect();
            if (aspect == null) {
                return ARGB.colorFromFloat(1.0F, MANA_POD_HERBA_R, MANA_POD_HERBA_G, MANA_POD_HERBA_B);
            }
            int color = aspect.value().color();
            float progress = Mth.clamp((state.getValue(BlockManaPod.AGE) - MANA_POD_TINT_START_AGE) / MANA_POD_TINT_SPAN, 0.0F, 1.0F);
            return ARGB.colorFromFloat(1.0F, Mth.lerp(progress, MANA_POD_HERBA_R, ARGB.redFloat(color)), Mth.lerp(progress, MANA_POD_HERBA_G, ARGB.greenFloat(color)),
                    Mth.lerp(progress, MANA_POD_HERBA_B, ARGB.blueFloat(color)));
        }
    }
}
