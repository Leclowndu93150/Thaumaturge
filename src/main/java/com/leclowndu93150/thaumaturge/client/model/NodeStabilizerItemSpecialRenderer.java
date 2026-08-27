package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.render.blockentity.NodeStabilizerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class NodeStabilizerItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private final boolean advanced;
    private final boolean transducer;

    public NodeStabilizerItemSpecialRenderer(boolean advanced) {
        this(advanced, false);
    }

    public NodeStabilizerItemSpecialRenderer(boolean advanced, boolean transducer) {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.advanced = advanced;
        this.transducer = transducer;
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
        if (transducer) {
            poseStack.translate(0.5F, 1.0F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            NodeStabilizerRenderer.submitTransducerParts(0, 0, 0.0F, poseStack, buffers, light);
        } else {
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            NodeStabilizerRenderer.submitParts(0, advanced, 0.0F, poseStack, buffers, light);
        }
        poseStack.popPose();
    }
}
