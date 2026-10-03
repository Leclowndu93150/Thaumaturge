package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.client.render.blockentity.JarRenderer;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

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
        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                        blockItem.getBlock().defaultBlockState(),
                        poseStack,
                        buffers,
                        light,
                        overlay,
                        ModelData.EMPTY,
                        null);
        AspectInstance contents = extractContents(stack);
        if (contents != null) {
            int capacity = blockItem.getBlock() instanceof IEssentiaJar jar
                    ? jar.jarCapacity()
                    : IEssentiaJar.DEFAULT_CAPACITY;
            JarRenderer.submitFluid(
                    contents.amount(),
                    capacity,
                    contents.aspect().value().color(),
                    light,
                    JarRenderer.ANIMATED_GLOW_MATERIAL.sprite(),
                    poseStack,
                    buffers);
        }
    }

    private static AspectInstance extractContents(ItemStack stack) {
        var essentiaList = stack.get(TCDataComponents.ESSENTIA_CONTENTS.get());
        if (essentiaList == null || essentiaList.isEmpty()) {
            return null;
        }
        return essentiaList.contents().entries().getFirst();
    }
}
