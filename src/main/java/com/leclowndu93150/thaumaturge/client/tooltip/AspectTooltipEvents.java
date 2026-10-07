package com.leclowndu93150.thaumaturge.client.tooltip;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectChipsTooltip;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class AspectTooltipEvents {
    private AspectTooltipEvents() {}

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (event.getItemStack().isEmpty()) {
            return;
        }
        /*if (!isContainerScreenOpen()) {
            return;
        }*/
        if (!shouldShowAspects()) {
            return;
        }
        AspectList aspects = AspectIndexAccess.index().of(event.getItemStack());
        if (aspects.isEmpty()) {
            return;
        }
        event.getTooltipElements().add(Either.right(new AspectChipsTooltip(aspects)));
    }

    private static boolean isContainerScreenOpen() {
        return Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>;
    }

    private static boolean shouldShowAspects() {
        boolean shift = Minecraft.getInstance().hasShiftDown();
        return shift != ThaumaturgeClientConfig.showAspectsByDefault();
    }
}
