package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.golem.CopperGolemRig;
import com.leclowndu93150.thaumaturge.client.golem.GolemAccessoryRenderTable;
import com.leclowndu93150.thaumaturge.client.golem.GolemRenderState;
import com.leclowndu93150.thaumaturge.client.golem.GolemRenderer;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GolemItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private CopperGolemRig model;

    public GolemItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (model == null) {
            model = new CopperGolemRig(Minecraft.getInstance().getEntityModels().bakeLayer(TTModelLayers.GOLEM));
        }
        GolemRenderState state = new GolemRenderState();
        state.props = stack.getOrDefault(TTDataComponents.GOLEM_PROPERTIES.get(), GolemProperties.createDefault());
        state.lightCoords = light;
        model.setupAnim(state);
        pose.pushPose();
        pose.translate(0.5F, 0.05F, 0.5F);
        pose.scale(CopperGolemRig.SCALE, -CopperGolemRig.SCALE, -CopperGolemRig.SCALE);
        pose.translate(0, -1.5F, 0);
        GolemRenderer.renderParts(model, GolemAccessoryRenderTable.EMPTY, state, pose, buffers, false, -1);
        pose.popPose();
    }
}
