package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchObelisk;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class EldritchObeliskRenderer implements BlockEntityRenderer<BlockEntityEldritchObelisk> {
    public static final ResourceLocation CAP_MODEL = TCIds.rl("models/mesh/obelisk_cap.tcmesh");
    public static final String CAP_PART = "Cap";

    private static final ResourceLocation SIDE_TEXTURE = TCIds.rl("textures/entity/obelisk_side.png");
    private static final ResourceLocation SIDE_TEXTURE_OUTER = TCIds.rl("textures/entity/obelisk_side_2.png");
    private static final ResourceLocation CAP_TEXTURE = TCIds.rl("textures/entity/obelisk_cap.png");
    private static final ResourceLocation CAP_TEXTURE_OUTER = TCIds.rl("textures/entity/obelisk_cap_2.png");

    private static final float COLUMN_BASE = 1.0F;
    private static final int COLUMN_HEIGHT = 3;
    private static final float BOB_PERIOD = 10.0F;
    private static final float BOB_AMPLITUDE = 0.1F;
    private static final float PLANE_INSET = 0.01F;

    public EldritchObeliskRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityEldritchObelisk obelisk,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        float animationTime = viewEntity == null ? partialTick : viewEntity.tickCount + partialTick;
        boolean outerLands = obelisk.getLevel() != null && obelisk.getLevel().dimension() == OuterLands.DIMENSION;
        float bob = Mth.sin(animationTime / BOB_PERIOD) * BOB_AMPLITUDE + BOB_AMPLITUDE;
        float base = COLUMN_BASE + bob;
        float top = base + COLUMN_HEIGHT;

        VertexConsumer surface = buffers.getBuffer(EldritchPortalSurface.SURFACE);
        PoseStack.Pose surfacePose = poseStack.last();
        EldritchPortalSurface.quad(
                surfacePose,
                surface,
                obelisk.getBlockPos(),
                0.0F,
                base,
                PLANE_INSET,
                0.0F,
                top,
                PLANE_INSET,
                1.0F,
                top,
                PLANE_INSET,
                1.0F,
                base,
                PLANE_INSET);
        EldritchPortalSurface.quad(
                surfacePose,
                surface,
                obelisk.getBlockPos(),
                0.0F,
                base,
                1.0F - PLANE_INSET,
                0.0F,
                top,
                1.0F - PLANE_INSET,
                1.0F,
                top,
                1.0F - PLANE_INSET,
                1.0F,
                base,
                1.0F - PLANE_INSET);
        EldritchPortalSurface.quad(
                surfacePose,
                surface,
                obelisk.getBlockPos(),
                PLANE_INSET,
                base,
                0.0F,
                PLANE_INSET,
                top,
                0.0F,
                PLANE_INSET,
                top,
                1.0F,
                PLANE_INSET,
                base,
                1.0F);
        EldritchPortalSurface.quad(
                surfacePose,
                surface,
                obelisk.getBlockPos(),
                1.0F - PLANE_INSET,
                base,
                0.0F,
                1.0F - PLANE_INSET,
                top,
                0.0F,
                1.0F - PLANE_INSET,
                top,
                1.0F,
                1.0F - PLANE_INSET,
                base,
                1.0F);

        RenderType sideType = RenderType.entityTranslucent(outerLands ? SIDE_TEXTURE_OUTER : SIDE_TEXTURE);
        VertexConsumer sideBuffer = buffers.getBuffer(sideType);
        for (int a = 0; a < 4; a++) {
            poseStack.pushPose();
            poseStack.translate(0.5F, base, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(a * 90.0F));
            poseStack.translate(0.0F, 0.0F, -0.5F);
            sideQuad(poseStack.last(), sideBuffer, light);
            poseStack.popPose();
        }

        RenderType capType = RenderType.entityTranslucent(outerLands ? CAP_TEXTURE_OUTER : CAP_TEXTURE);
        poseStack.pushPose();
        poseStack.translate(0.5F, base, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        renderCap(poseStack, buffers, capType, light);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5F, top, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        renderCap(poseStack, buffers, capType, light);
        poseStack.popPose();
    }

    static void renderCap(PoseStack poseStack, MultiBufferSource buffers, RenderType type, int light) {
        TCMesh mesh = GolemMeshes.get(CAP_MODEL);
        VertexConsumer buffer = buffers.getBuffer(type);
        for (TCMeshPart part : mesh.parts()) {
            if (CAP_PART.equals(part.name())) {
                GolemMeshes.renderPart(part, poseStack.last(), buffer, light, -1);
            }
        }
    }

    private static void sideQuad(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        sideVertex(buffer, pose, -0.5F, COLUMN_HEIGHT, 0.0F, 1.0F, light);
        sideVertex(buffer, pose, 0.5F, COLUMN_HEIGHT, 1.0F, 1.0F, light);
        sideVertex(buffer, pose, 0.5F, 0.0F, 1.0F, 0.0F, light);
        sideVertex(buffer, pose, -0.5F, 0.0F, 0.0F, 0.0F, light);
    }

    private static void sideVertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light) {
        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchObelisk obelisk) {
        return new AABB(obelisk.getBlockPos()).inflate(0.5, 0.0, 0.5).expandTowards(0.0, 5.0, 0.0);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityEldritchObelisk obelisk) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
