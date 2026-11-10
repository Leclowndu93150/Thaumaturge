package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.leclowndu93150.thaumaturge.client.render.blockentity.JarBrainRenderer;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class JarBrainItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEX_BRAIN = TTIds.rl("textures/entity/brain2.png");
    private static final ResourceLocation TEX_BRINE = TTIds.rl("textures/entity/jarbrine.png");
    private static final float BRAIN_YAW_DEGREES = 155.0F;

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
                .renderSingleBlock(
                        TTBlocks.JAR_BRAIN.get().defaultBlockState(),
                        poseStack,
                        buffers,
                        light,
                        overlay,
                        ModelData.EMPTY,
                        null);

        poseStack.pushPose();
        JarBrainRenderer.poseBrain(poseStack, 0.0F, BRAIN_YAW_DEGREES);
        brain.setupAnim(0.0F, 0.0F);
        brain.root.render(poseStack, buffers.getBuffer(RenderType.entityCutout(TEX_BRAIN)), light, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        JarBrainRenderer.poseBrine(poseStack);
        brine.root.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(TEX_BRINE)), light, overlay);
        poseStack.popPose();
    }
}
