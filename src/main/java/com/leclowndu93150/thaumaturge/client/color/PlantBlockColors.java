package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.Holder;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class PlantBlockColors {
    private static final float MANA_POD_HERBA_R = 0.14509805F;
    private static final float MANA_POD_HERBA_G = 0.6156863F;
    private static final float MANA_POD_HERBA_B = 0.45882353F;
    private static final int MANA_POD_TINT_START_AGE = 2;
    private static final float MANA_POD_TINT_SPAN = BlockEntityManaPod.MAX_AGE - MANA_POD_TINT_START_AGE;

    private PlantBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColor grass = (state, level, pos, tintIndex) -> level != null && pos != null
                ? BiomeColors.getAverageGrassColor(level, pos)
                : GrassColor.getDefaultColor();
        BlockColor foliage = (state, level, pos, tintIndex) -> level != null && pos != null
                ? BiomeColors.getAverageFoliageColor(level, pos)
                : FoliageColor.getDefaultColor();
        event.register(grass, TTBlocks.GRASS_AMBIENT.get());
        event.register(foliage, TTBlocks.LEAVES_GREATWOOD.get());
        event.register(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex != 0 || level == null || pos == null) {
                        return -1;
                    }
                    BlockEntityManaPod pod =
                            level.getBlockEntity(pos) instanceof BlockEntityManaPod manaPod ? manaPod : null;
                    if (pod == null) {
                        return -1;
                    }
                    Holder<IAspect> aspect = pod.aspect();
                    if (aspect == null) {
                        return ARGB32.colorFromFloat(1.0F, MANA_POD_HERBA_R, MANA_POD_HERBA_G, MANA_POD_HERBA_B);
                    }
                    int color = aspect.value().color();
                    float progress = Mth.clamp(
                            (state.getValue(BlockManaPod.AGE) - MANA_POD_TINT_START_AGE) / MANA_POD_TINT_SPAN,
                            0.0F,
                            1.0F);
                    return ARGB32.colorFromFloat(
                            1.0F,
                            Mth.lerp(progress, MANA_POD_HERBA_R, ARGB32.red(color) / 255.0F),
                            Mth.lerp(progress, MANA_POD_HERBA_G, ARGB32.green(color) / 255.0F),
                            Mth.lerp(progress, MANA_POD_HERBA_B, ARGB32.blue(color) / 255.0F));
                },
                TTBlocks.MANA_POD.get());
    }
}
