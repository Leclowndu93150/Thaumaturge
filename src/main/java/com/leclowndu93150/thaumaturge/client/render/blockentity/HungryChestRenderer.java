package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityHungryChest;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.ChestBlock;

public final class HungryChestRenderer implements BlockEntityRenderer<BlockEntityHungryChest> {
    private static final Material MATERIAL = new Material(Sheets.CHEST_SHEET, TTIds.rl("entity/chest/hungry"));

    private final ModelPart bottom;
    private final ModelPart lid;
    private final ModelPart lock;

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = root.getChild("bottom");
        this.lid = root.getChild("lid");
        this.lock = root.getChild("lock");
    }

    @Override
    public void render(
            BlockEntityHungryChest chest,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        float yRot = chest.getBlockState().getValue(ChestBlock.FACING).toYRot();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        float open = chest.getOpenNess(partialTick);
        open = 1.0F - open;
        open = 1.0F - open * open * open;
        VertexConsumer buffer = MATERIAL.buffer(buffers, RenderType::entityCutout);
        lid.xRot = -(open * (float) (Math.PI / 2));
        lock.xRot = lid.xRot;
        lid.render(poseStack, buffer, light, overlay);
        lock.render(poseStack, buffer, light, overlay);
        bottom.render(poseStack, buffer, light, overlay);
        poseStack.popPose();
    }
}
