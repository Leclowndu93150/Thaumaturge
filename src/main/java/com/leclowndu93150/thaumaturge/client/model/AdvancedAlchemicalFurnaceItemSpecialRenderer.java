package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.render.blockentity.AdvancedAlchemicalFurnaceRenderer;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the complete Advanced Alchemical Furnace multiblock in item contexts. */
public final class AdvancedAlchemicalFurnaceItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float SCALE = 1.0F / 3.0F;

    public AdvancedAlchemicalFurnaceItemSpecialRenderer() {
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
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        AdvancedAlchemicalFurnaceRenderer.renderPreview(
                TCBlocks.ADVANCED_ALCHEMICAL_FURNACE.get().defaultBlockState(), poseStack, buffers, light, overlay);
        poseStack.popPose();
    }
}
