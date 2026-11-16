package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.client.render.blockentity.LegacyItemLift.ModelBounds;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class WorkbenchWandDisplayRenderer implements BlockEntityRenderer<BlockEntityArcaneWorkbench> {
    private static final float TABLE_CENTER = 0.5F;
    private static final float SURFACE_HEIGHT = 1.0F;
    private static final float CROSSWISE_ANGLE = 45.0F;
    private static final float GRID_DIAGONAL = (float) (10.0F * Math.sqrt(2.0) / 16.0F);
    private static final float WAND_HALF_WIDTH = 0.15F;
    private static final float WAND_MIN_Y = -0.8125F;
    private static final float WAND_MAX_Y = 0.6875F;
    private static final float STAFF_MIN_Y = -2.1063F;
    private static final float STAFF_MAX_Y = 0.5547F;

    public WorkbenchWandDisplayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityArcaneWorkbench workbench,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        ItemStack wand = workbench.getInventory().wandStack();
        if (wand.isEmpty() || !(wand.getItem() instanceof ItemWand)) {
            return;
        }
        ItemWand wandItem = (ItemWand) wand.getItem();
        boolean staff = wandItem.isStaff(wand);
        ModelBounds bounds = new ModelBounds(
                TABLE_CENTER - WAND_HALF_WIDTH,
                TABLE_CENTER + WAND_HALF_WIDTH,
                staff ? STAFF_MIN_Y : WAND_MIN_Y,
                staff ? STAFF_MAX_Y : WAND_MAX_Y,
                TABLE_CENTER - WAND_HALF_WIDTH,
                TABLE_CENTER + WAND_HALF_WIDTH);

        float scale = GRID_DIAGONAL / bounds.sizeY();
        poseStack.pushPose();
        poseStack.translate(TABLE_CENTER, SURFACE_HEIGHT + bounds.sizeZ() * scale / 2.0F, TABLE_CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(CROSSWISE_ANGLE));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-bounds.centerX(), -bounds.centerY(), -bounds.centerZ());
        ItemRenderHelper.render(
                wand,
                ItemDisplayContext.NONE,
                poseStack,
                buffers,
                light,
                overlay,
                workbench.getBlockPos().hashCode());
        poseStack.popPose();
    }
}
