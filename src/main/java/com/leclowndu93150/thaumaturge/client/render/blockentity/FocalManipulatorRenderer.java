package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public final class FocalManipulatorRenderer implements BlockEntityRenderer<BlockEntityFocalManipulator> {
    private static final float FOCUS_HEIGHT = 0.8F;
    private static final float FOCUS_BOB_PERIOD = 14.0F;
    private static final float FOCUS_HOVER_PHASE = 0.2F;
    private static final float ITEM_ENTITY_BOB_HEIGHT = 0.1F;
    private static final float CRYSTAL_RING_HEIGHT = 1.05F;
    private static final float CRYSTAL_RING_RADIUS = 0.4F;
    private static final float CRYSTAL_SCALE = 0.5F;
    private static final float CRYSTAL_BOB_SCALE = 0.02F;
    private static final float GLOW_RING_HEIGHT = 1.3F;
    private static final float GLOW_HALF = 0.175F;
    private static final float GLOW_ALPHA = 0.66F;
    private static final int EMISSIVE_LIGHT = 0xF000F0;
    private static final float RAY_LIFT = 0.475F;
    private static final long RAY_SEED = 187L;
    private static final float RAY_ALPHA = 0.66F;

    private static final RenderType RAY_TYPE = TTRenderTypes.SPARKLE_CULLED;
    private static final RenderType GLOW_TYPE = TTRenderTypes.fxAdditiveBlurred(ParticleTextures.STAR_GLINT);

    private final RandomSource rayRandom = RandomSource.create();

    public FocalManipulatorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityFocalManipulator table,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        float ticks = viewEntity == null ? partialTick : viewEntity.tickCount + partialTick;
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        ItemStack focus = table.focusStack();
        if (!focus.isEmpty()) {
            float lift = LegacyItemLift.centerLift(focus, ItemDisplayContext.GROUND)
                    + Mth.sin(Mth.sin(ticks / FOCUS_BOB_PERIOD) * FOCUS_HOVER_PHASE + FOCUS_HOVER_PHASE)
                            * ITEM_ENTITY_BOB_HEIGHT;
            poseStack.pushPose();
            poseStack.translate(0.5F, FOCUS_HEIGHT, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
            poseStack.translate(0.0F, lift, 0.0F);
            ItemRenderHelper.render(focus, ItemDisplayContext.GROUND, poseStack, buffers, light, overlay, 0);
            poseStack.popPose();
        }

        List<AspectInstance> entries = table.crystals().entries();
        int q = entries.size();
        if (q == 0) {
            return;
        }
        float ang = 360.0F / q;
        for (int a = 0; a < q; a++) {
            AspectInstance instance = entries.get(a);
            ItemStack crystal = EssentiaCrystalFactory.of(instance.aspect());
            float crystalLift = LegacyItemLift.centerLift(crystal, ItemDisplayContext.GROUND);
            float angle = ticks % 720.0F / 2.0F + ang * a;
            float bob = Mth.sin((ticks + a * 10) / 12.0F) * CRYSTAL_BOB_SCALE + CRYSTAL_BOB_SCALE;
            int color = instance.aspect().value().color();
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            poseStack.pushPose();
            poseStack.translate(0.5F, GLOW_RING_HEIGHT, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.translate(0.0F, bob, CRYSTAL_RING_RADIUS);
            poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
            poseStack.mulPose(camera.rotation());
            drawGlow(buffers, poseStack, ticks, r, g, b);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0.5F, CRYSTAL_RING_HEIGHT, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.translate(0.0F, bob, CRYSTAL_RING_RADIUS);
            poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
            drawRay(poseStack, buffers, angle, a, bob, r, g, b, ticks);
            drawRay(poseStack, buffers, angle, (a + 1) * 5, bob, r, g, b, ticks);
            poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
            poseStack.translate(0.0F, crystalLift, 0.0F);
            ItemRenderHelper.render(crystal, ItemDisplayContext.GROUND, poseStack, buffers, light, overlay, 0);
            poseStack.popPose();
        }
    }

    private static void drawGlow(
            MultiBufferSource buffers, PoseStack poseStack, float ticks, float r, float g, float b) {
        int frame = (int) ticks % ParticleTextures.STAR_GLINT_FRAMES;
        float u0 = StripUv.u0(frame, ParticleTextures.STAR_GLINT_FRAMES);
        float v0 = StripUv.V0;
        float u1 = StripUv.u1(frame, ParticleTextures.STAR_GLINT_FRAMES);
        float v1 = StripUv.V1;
        int tint = ARGB32.colorFromFloat(GLOW_ALPHA, r, g, b);
        VertexConsumer buffer = buffers.getBuffer(GLOW_TYPE);
        Matrix4f mat = poseStack.last().pose();
        buffer.addVertex(mat, -GLOW_HALF, -GLOW_HALF, 0.0F)
                .setUv(u1, v1)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, -GLOW_HALF, GLOW_HALF, 0.0F)
                .setUv(u1, v0)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, GLOW_HALF, GLOW_HALF, 0.0F)
                .setUv(u0, v0)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, GLOW_HALF, -GLOW_HALF, 0.0F)
                .setUv(u0, v1)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
    }

    private void drawRay(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float angle,
            int num,
            float bob,
            float r,
            float g,
            float b,
            float ticks) {
        rayRandom.setSeed(RAY_SEED + (long) num * num);
        float pan = Mth.sin((ticks + num * 10) / 15.0F) * 15.0F;
        float aperture = Mth.sin((ticks + num * 10) / 14.0F) * 2.0F;
        poseStack.pushPose();
        poseStack.translate(0.0F, RAY_LIFT + bob, 0.0F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.mulPose(Axis.YP.rotationDegrees(rayRandom.nextFloat() * 360.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(pan));
        float ramp = Math.min(ticks, 10.0F) / 10.0F;
        float fa = (rayRandom.nextFloat() * 20.0F + 10.0F) / 30.0F * ramp;
        float f4 = (rayRandom.nextFloat() * 4.0F + 6.0F + aperture) / 30.0F * ramp;
        VertexConsumer buffer = buffers.getBuffer(RAY_TYPE);
        Matrix4f mat = poseStack.last().pose();
        float bx1 = -0.8F * f4;
        float bz1 = -0.5F * f4;
        float bx2 = 0.8F * f4;
        float bz3 = f4;
        buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(r, g, b, RAY_ALPHA);
        buffer.addVertex(mat, bx1, fa, bz1).setColor(r, g, b, 0.0F);
        buffer.addVertex(mat, bx2, fa, bz1).setColor(r, g, b, 0.0F);
        buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(r, g, b, RAY_ALPHA);
        buffer.addVertex(mat, bx2, fa, bz1).setColor(r, g, b, 0.0F);
        buffer.addVertex(mat, 0.0F, fa, bz3).setColor(r, g, b, 0.0F);
        buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(r, g, b, RAY_ALPHA);
        buffer.addVertex(mat, 0.0F, fa, bz3).setColor(r, g, b, 0.0F);
        buffer.addVertex(mat, bx1, fa, bz1).setColor(r, g, b, 0.0F);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
