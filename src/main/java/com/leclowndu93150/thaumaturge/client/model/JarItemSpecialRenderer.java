package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.client.render.blockentity.JarRenderer;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class JarItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {

    public JarItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        MultiBufferSource jarBuffers = stack.hasFoil() && displayContext == ItemDisplayContext.GUI
                ? renderType -> ItemRenderer.getFoilBuffer(buffers, renderType, true, true)
                : buffers;
        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(blockItem.getBlock().defaultBlockState(), poseStack, jarBuffers, light, overlay);
        AspectInstance contents = extractContents(stack);
        if (contents != null) {
            int capacity = blockItem.getBlock() instanceof IEssentiaJar jar
                    ? jar.jarCapacity()
                    : IEssentiaJar.DEFAULT_CAPACITY;
            JarRenderer.submitFluid(
                    contents.amount(),
                    capacity,
                    ARGB32.opaque(contents.aspect().value().color()),
                    light,
                    JarRenderer.ANIMATED_GLOW_MATERIAL.sprite(),
                    poseStack,
                    buffers);
        }
    }

    private static AspectInstance extractContents(ItemStack stack) {
        var essentiaList = stack.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        if (essentiaList == null || essentiaList.isEmpty()) {
            return null;
        }
        return essentiaList.contents().entries().getFirst();
    }
}
