package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.content.device.BlockEntityHungryChest;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class HungryChestItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private final BlockEntityHungryChest chest;

    public HungryChestItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.chest = new BlockEntityHungryChest(
                BlockPos.ZERO, TTBlocks.HUNGRY_CHEST.get().defaultBlockState());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(chest, poseStack, buffers, light, overlay);
    }
}
