package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.client.render.blockentity.NodeRenderState;
import com.leclowndu93150.thaumaturge.client.render.blockentity.NodeRenderer;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class JarNodeItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float NODE_HEIGHT = 0.4F;
    private static final float NODE_SIZE = 1.0F;
    private static final float BASE_ALPHA = 0.5F;
    private static final float TICKS_WRAP = 1000000.0F;

    public JarNodeItemSpecialRenderer() {
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
        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(TTBlocks.JAR_NODE.get().defaultBlockState(), poseStack, buffers, light, overlay);

        NodeData data = stack.get(TTDataComponents.NODE_DATA.get());
        if (data == null || data.aspects().isEmpty()) {
            return;
        }
        NodeRenderState state = new NodeRenderState();
        state.type = data.type();
        state.modifier = data.modifier().orElse(null);
        state.visible = true;
        state.size = NODE_SIZE;
        state.ticks = (Util.getMillis() % (long) TICKS_WRAP) / 50.0F;
        state.frameSeed = 1;
        float alpha = BASE_ALPHA;
        if (state.modifier != null) {
            alpha = switch (state.modifier) {
                case BRIGHT -> alpha * 1.5F;
                case PALE -> alpha * 0.66F;
                case FADING -> alpha * (Mth.sin(state.ticks / 3.0F) * 0.25F + 0.33F);
            };
        }
        state.alpha = Math.min(alpha, 1.0F);
        for (AspectInstance entry : data.aspects().entries()) {
            NodeRenderState.AspectLayer layer = new NodeRenderState.AspectLayer();
            layer.color = entry.aspect().value().color();
            layer.amount = entry.amount();
            layer.blend = entry.aspect().value().blend();
            state.layers.add(layer);
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, NODE_HEIGHT, 0.5F);
        NodeRenderer.drawLayers(state, poseStack, buffers);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        NodeRenderer.drawLayers(state, poseStack, buffers);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        NodeRenderer.drawLayers(state, poseStack, buffers);
        poseStack.popPose();
    }
}
