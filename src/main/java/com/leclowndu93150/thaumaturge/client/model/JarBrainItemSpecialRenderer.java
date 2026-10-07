package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class JarBrainItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEX_BRAIN = TTIds.rl("textures/entity/brain2.png");
    private static final ResourceLocation TEX_BRINE = TTIds.rl("textures/entity/jarbrine.png");
    private static final float BRAIN_SCALE = 0.4F;
    private static final float BRAIN_LIFT = -0.77F;

    private final BrainModel brain;
    private final JarBrineModel brine;

    public JarBrainItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.brain = new BrainModel(models.bakeLayer(TTModelLayers.BRAIN));
        this.brine = new JarBrineModel(models.bakeLayer(TTModelLayers.JAR_BRINE));
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
                .renderSingleBlock(TTBlocks.JAR_BRAIN.get().defaultBlockState(), poseStack, buffers, light, overlay);

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.01F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        poseStack.pushPose();
        poseStack.translate(0.0F, BRAIN_LIFT, 0.0F);
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        poseStack.scale(BRAIN_SCALE, BRAIN_SCALE, BRAIN_SCALE);
        brain.setupAnim(0.0F, 0.0F);
        brain.root.render(poseStack, buffers.getBuffer(RenderType.entityCutout(TEX_BRAIN)), light, overlay);
        poseStack.popPose();

        brine.root.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(TEX_BRINE)), light, overlay);
        poseStack.popPose();
    }
}
