package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class DeconTableItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/decontable.png");
    private static final float BOOK_Y = 1.02F;
    private static final float BOOK_SCALE = 0.8F;
    private static final float IN_FRAME_SCALE = 0.5128205F;

    private final DeconTableModel model;
    private final ItemStack thaumometer;

    public DeconTableItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.model = new DeconTableModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(TTModelLayers.DECONSTRUCTION_TABLE));
        this.thaumometer = new ItemStack(TTItems.THAUMOMETER.get());
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
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        model.root.render(poseStack, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), light, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5F, BOOK_Y, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.scale(BOOK_SCALE, BOOK_SCALE, BOOK_SCALE);
        poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        ItemRenderHelper.render(thaumometer, ItemDisplayContext.FIXED, poseStack, buffers, light, overlay, 0);
        poseStack.popPose();
    }
}
