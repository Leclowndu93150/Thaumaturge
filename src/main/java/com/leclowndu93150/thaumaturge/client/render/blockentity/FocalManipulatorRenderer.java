package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class FocalManipulatorRenderer implements BlockEntityRenderer<BlockEntityFocalManipulator> {
    private static final float CENTER = 0.5F;
    private static final float FOCUS_SCALE = 1.0F;
    private static final float FOCUS_HEIGHT = 1.32F;
    private static final float CRYSTAL_SCALE = 0.45F;
    private static final float LEGACY_CENTER_Y = 0.35F;
    private static final float GLINT_RISE = 0.17F;
    private static final float GLINT_SIZE = 0.2F;
    private static final float GLINT_HALF = 0.5F;
    private static final float GLINT_ALPHA = 0.85F;
    private static final float GLINT_TICKS_PER_FRAME = 2.0F;
    private static final int GLINT_FRAME_STEP = 5;
    private static final int BEAM_COUNT = 2;
    private static final float BEAM_START = 0.03F;
    private static final float BEAM_BASE_HALF = 0.012F;
    private static final float BEAM_ALPHA = 0.5F;
    private static final int FADED = 0;
    private static final float FLAT_UV = 0.5F;
    private static final int RENDER_BOUNDS_HEIGHT = 2;

    private static final RenderType BEAM_TYPE = TTRenderTypes.fxAdditive(TTIds.rl("textures/misc/white.png"));
    private static final RenderType GLOW_TYPE = TTRenderTypes.fxAdditiveBlurred(ParticleTextures.STAR_GLINT);

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
        Quaternionf facing = ClientSpaces.cameraFacing(
                table.getLevel(),
                Vec3.atCenterOf(table.getBlockPos()),
                Minecraft.getInstance().gameRenderer.getMainCamera().rotation());

        ItemStack focus = table.focusStack();
        if (!focus.isEmpty()) {
            float lift = LegacyItemLift.centerLift(focus, ItemDisplayContext.GROUND);
            float bob = FocalManipulatorRenderMath.focusBob(ticks);
            poseStack.pushPose();
            poseStack.translate(CENTER, FOCUS_HEIGHT + bob - LEGACY_CENTER_Y * FOCUS_SCALE, CENTER);
            poseStack.mulPose(Axis.YP.rotationDegrees(FocalManipulatorRenderMath.focusSpin(ticks)));
            poseStack.scale(FOCUS_SCALE, FOCUS_SCALE, FOCUS_SCALE);
            poseStack.translate(0.0F, lift, 0.0F);
            ItemRenderHelper.render(focus, ItemDisplayContext.GROUND, poseStack, buffers, light, overlay, 0);
            poseStack.popPose();
        }

        List<AspectInstance> entries = table.crystals().entries();
        int q = entries.size();
        if (q == 0) {
            return;
        }
        for (int a = 0; a < q; a++) {
            AspectInstance instance = entries.get(a);
            ItemStack crystal = EssentiaCrystalFactory.of(instance.aspect());
            float crystalLift = LegacyItemLift.centerLift(crystal, ItemDisplayContext.GROUND);
            float angle = FocalManipulatorRenderMath.orbitDegrees(ticks, a, q);
            float radians = angle * Mth.DEG_TO_RAD;
            float x = CENTER + Mth.cos(radians) * FocalManipulatorRenderMath.orbitRadius();
            float z = CENTER - Mth.sin(radians) * FocalManipulatorRenderMath.orbitRadius();
            float y = FocalManipulatorRenderMath.crystalHeight(ticks, a);
            int color = instance.aspect().value().color();
            drawCrystal(table, a, crystal, crystalLift, color, angle, y, ticks, poseStack, buffers, light, overlay);
            drawGlint(color, a, x, y, z, ticks, facing, poseStack, buffers);
        }
    }

    private static void drawCrystal(
            BlockEntityFocalManipulator table,
            int crystalIndex,
            ItemStack crystal,
            float crystalLift,
            int color,
            float angle,
            float y,
            float ticks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        poseStack.pushPose();
        poseStack.translate(CENTER, y, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(FocalManipulatorRenderMath.orbitRadius(), 0.0F, 0.0F);
        poseStack.pushPose();
        poseStack.translate(0.0F, -LEGACY_CENTER_Y * CRYSTAL_SCALE, 0.0F);
        poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
        poseStack.translate(0.0F, crystalLift, 0.0F);
        ItemRenderHelper.render(crystal, ItemDisplayContext.GROUND, poseStack, buffers, light, overlay, 0);
        poseStack.popPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        int seed = Long.hashCode(table.getBlockPos().asLong());
        for (int beam = 0; beam < BEAM_COUNT; beam++) {
            drawBeam(seed, crystalIndex, beam, color, ticks, poseStack, buffers);
        }
        poseStack.popPose();
    }

    private static void drawGlint(
            int color,
            int index,
            float x,
            float y,
            float z,
            float ticks,
            Quaternionf facing,
            PoseStack poseStack,
            MultiBufferSource buffers) {
        int frame = Math.floorMod(
                (int) (ticks / GLINT_TICKS_PER_FRAME) + index * GLINT_FRAME_STEP, ParticleTextures.STAR_GLINT_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.STAR_GLINT_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.STAR_GLINT_FRAMES);
        int tint = ARGB32.color((int) (GLINT_ALPHA * 255.0F), color);
        poseStack.pushPose();
        poseStack.translate(x, y + GLINT_RISE, z);
        poseStack.mulPose(facing);
        poseStack.scale(GLINT_SIZE, GLINT_SIZE, GLINT_SIZE);
        writeGlint(buffers.getBuffer(GLOW_TYPE), poseStack.last().pose(), u0, u1, tint);
        poseStack.popPose();
    }

    private static void writeGlint(VertexConsumer buffer, Matrix4f matrix, float u0, float u1, int tint) {
        buffer.addVertex(matrix, -GLINT_HALF, -GLINT_HALF, 0.0F)
                .setUv(u0, StripUv.V1)
                .setColor(tint)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix, GLINT_HALF, -GLINT_HALF, 0.0F)
                .setUv(u1, StripUv.V1)
                .setColor(tint)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix, GLINT_HALF, GLINT_HALF, 0.0F)
                .setUv(u1, StripUv.V0)
                .setColor(tint)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix, -GLINT_HALF, GLINT_HALF, 0.0F)
                .setUv(u0, StripUv.V0)
                .setColor(tint)
                .setLight(LightTexture.FULL_BRIGHT);
    }

    private static void drawBeam(
            int seed,
            int crystal,
            int beam,
            int aspectColor,
            float ticks,
            PoseStack poseStack,
            MultiBufferSource buffers) {
        FocalManipulatorRenderMath.Beam profile = FocalManipulatorRenderMath.beam(seed, crystal, beam, ticks);
        int baseColor = ARGB32.color((int) (BEAM_ALPHA * 255.0F), aspectColor);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(profile.yawDegrees()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(profile.tiltDegrees()));
        poseStack.mulPose(Axis.XP.rotationDegrees(profile.rollDegrees()));
        writeBeam(
                buffers.getBuffer(BEAM_TYPE),
                poseStack.last().pose(),
                profile.length(),
                profile.tipHalfWidth(),
                baseColor);
        poseStack.popPose();
    }

    private static void writeBeam(VertexConsumer buffer, Matrix4f matrix, float length, float tipHalf, int baseColor) {
        float tip = BEAM_START + length;
        beamVertex(buffer, matrix, BEAM_START, -BEAM_BASE_HALF, 0.0F, baseColor);
        beamVertex(buffer, matrix, BEAM_START, BEAM_BASE_HALF, 0.0F, baseColor);
        beamVertex(buffer, matrix, tip, tipHalf, 0.0F, FADED);
        beamVertex(buffer, matrix, tip, -tipHalf, 0.0F, FADED);
        beamVertex(buffer, matrix, BEAM_START, 0.0F, -BEAM_BASE_HALF, baseColor);
        beamVertex(buffer, matrix, BEAM_START, 0.0F, BEAM_BASE_HALF, baseColor);
        beamVertex(buffer, matrix, tip, 0.0F, tipHalf, FADED);
        beamVertex(buffer, matrix, tip, 0.0F, -tipHalf, FADED);
    }

    private static void beamVertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, int color) {
        buffer.addVertex(matrix, x, y, z)
                .setUv(FLAT_UV, FLAT_UV)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityFocalManipulator table) {
        var pos = table.getBlockPos();
        return new AABB(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 1.0,
                pos.getY() + RENDER_BOUNDS_HEIGHT,
                pos.getZ() + 1.0);
    }
}
