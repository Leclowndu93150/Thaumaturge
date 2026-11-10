package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJarBrain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;

public final class JarBrainRenderer implements BlockEntityRenderer<BlockEntityJarBrain> {
    private static final ResourceLocation TEX_BRAIN = TTIds.rl("textures/entity/brain2.png");
    private static final ResourceLocation TEX_BRINE = TTIds.rl("textures/entity/jarbrine.png");
    private static final float CENTER = 0.5F;
    private static final float BRINE_ANCHOR_Y = 0.01F;
    private static final float BRAIN_ANCHOR_Y = 0.4F;
    private static final float BRAIN_SCALE = 0.36F;
    private static final float BRAIN_PIVOT_Y = 0.9375F;
    private static final float HALF_TURN = 180.0F;
    private static final float QUARTER_TURN = 90.0F;
    private static final int OUTLINE_NONE = 0;
    private static final int XP_GLOW_COLOR = 0xFF8CFF4A;
    private static final RenderType XP_GLOW = RenderType.entityTranslucentEmissive(InventoryMenu.BLOCK_ATLAS);

    private final BrainModel brain;
    private final JarBrineModel brine;

    public JarBrainRenderer(BlockEntityRendererProvider.Context context) {
        this.brain = new BrainModel(context.bakeLayer(TTModelLayers.BRAIN));
        this.brine = new JarBrineModel(context.bakeLayer(TTModelLayers.JAR_BRINE));
    }

    @Override
    public void render(
            BlockEntityJarBrain jar,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        Level level = jar.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        float time = JarBrainRenderMath.age(gameTime, partialTick);
        float yawRadians = JarBrainRenderMath.interpolateYaw(jar.rotb, jar.rota, partialTick);
        float bobOffset = JarBrainRenderMath.bobOffset(gameTime, partialTick, Mth.getSeed(jar.getBlockPos()));
        float xpResponse = JarBrainRenderMath.xpResponse(time, jar.xp(), BlockEntityJarBrain.XP_MAX);

        poseStack.pushPose();
        poseBrain(poseStack, bobOffset, -(yawRadians * Mth.RAD_TO_DEG + QUARTER_TURN));
        brain.setupAnim(time, xpResponse);
        brain.root.render(poseStack, buffers.getBuffer(RenderType.entityCutout(TEX_BRAIN)), light, overlay);
        poseStack.popPose();

        int visibleXp = JarBrainRenderMath.visibleXp(jar.xp(), BlockEntityJarBrain.XP_MAX);
        if (visibleXp > 0) {
            JarRenderer.submitFluid(
                    visibleXp,
                    BlockEntityJarBrain.XP_MAX,
                    XP_GLOW_COLOR,
                    LightTexture.FULL_BRIGHT,
                    JarRenderer.ANIMATED_GLOW_MATERIAL.sprite(),
                    XP_GLOW,
                    poseStack,
                    buffers);
        }

        poseStack.pushPose();
        poseBrine(poseStack);
        brine.root.render(
                poseStack, buffers.getBuffer(TTRenderTypes.entityTranslucentNoDepthWrite(TEX_BRINE)), light, overlay);
        poseStack.popPose();
    }

    public static void poseBrine(PoseStack poseStack) {
        poseStack.translate(CENTER, BRINE_ANCHOR_Y, CENTER);
        poseStack.mulPose(Axis.ZP.rotationDegrees(HALF_TURN));
    }

    public static void poseBrain(PoseStack poseStack, float lift, float turnDegrees) {
        poseStack.translate(CENTER, BRAIN_ANCHOR_Y + lift, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(turnDegrees));
        poseStack.mulPose(Axis.ZP.rotationDegrees(HALF_TURN));
        poseStack.scale(BRAIN_SCALE, BRAIN_SCALE, BRAIN_SCALE);
        poseStack.translate(0.0F, -BRAIN_PIVOT_Y, 0.0F);
    }
}
