package com.leclowndu93150.thaumaturge.client.render.aspect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.TCFlatRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class AspectTagWorldRenderer {
    public static final float DEFAULT_SCALE = 0.0625F;
    public static final Identifier UNKNOWN_TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/aspects/_unknown.png");
    private static final float UNKNOWN_ALPHA = 0.75F;
    private static final float HALF_QUAD = 0.5F;
    private static final int ROW_SIZE = 5;
    private static final float ROW_SPACING = 1.05F;
    private static final float TEXT_SCALE = 0.04F;
    private static final int TEXT_SHADOW_COLOR = 0xFF111111;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private AspectTagWorldRenderer() {}

    public static void renderTagCloud(PoseStack poseStack, Minecraft mc, double x, double y, double z, AspectList aspects, @Nullable Direction dir, float tagScale, float alpha) {
        renderTagCloud(poseStack, mc, x, y, z, aspects, dir, tagScale, alpha, aspect -> true);
    }

    public static void renderTagCloud(PoseStack poseStack, Minecraft mc, double x, double y, double z, AspectList aspects, @Nullable Direction dir, float tagScale, float alpha, Predicate<Holder<IAspect>> discovered) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cam = camera.position();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Font font = mc.font;
        int stepX = 0;
        int stepY = 0;
        int stepZ = 0;
        if (dir != null) {
            stepX = dir.getStepX();
            stepY = dir.getStepY();
            stepZ = dir.getStepZ();
        } else {
            x -= 0.5;
            z -= 0.5;
        }
        List<AspectInstance> entries = aspects.sortedByTag();
        float yawDeg = (float) Math.toDegrees(Math.atan2(cam.x - (x + 0.5), cam.z - (z + 0.5)));
        int current = 0;
        float rowShift = 0.0F;
        int left = entries.size();
        for (AspectInstance entry : entries) {
            int div = Math.min(left, ROW_SIZE);
            if (current >= ROW_SIZE) {
                current = 0;
                rowShift += tagScale * ROW_SPACING;
                left -= ROW_SIZE;
                if (left < ROW_SIZE) {
                    div = left % ROW_SIZE;
                }
            }
            float shift = (current - div / 2.0F + 0.5F) * tagScale * 4.0F;
            shift *= tagScale;

            poseStack.pushPose();
            poseStack.translate(x + 0.5 + tagScale * 2.0F * stepX - cam.x, y + 0.5 + tagScale * 2.0F * stepY - cam.y, z + 0.5 + tagScale * 2.0F * stepZ - cam.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg + 180.0F));
            poseStack.scale(-1.0F, 1.0F, 1.0F);
            poseStack.translate(shift, rowShift, 0.0);

            boolean known = discovered.test(entry.aspect());
            poseStack.pushPose();
            poseStack.scale(tagScale, tagScale, tagScale);
            renderQuad(poseStack, buffers.getBuffer(TCFlatRenderTypes.entityTranslucentFlat(known ? entry.aspect().value().texture() : UNKNOWN_TEXTURE)), entry.aspect(), known ? alpha : UNKNOWN_ALPHA,
                    false, LightCoordsUtil.FULL_BRIGHT);
            poseStack.popPose();

            String amount = Integer.toString(entry.amount());
            int width = font.width(amount);
            poseStack.pushPose();
            poseStack.scale(tagScale * TEXT_SCALE, -tagScale * TEXT_SCALE, tagScale * TEXT_SCALE);
            poseStack.translate(0.0, 6.0, -0.1);
            font.drawInBatch(amount, 14 - width, 1, TEXT_SHADOW_COLOR, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);
            poseStack.translate(0.0, 0.0, -0.1);
            font.drawInBatch(amount, 13 - width, 0, TEXT_COLOR, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);
            poseStack.popPose();

            poseStack.popPose();
            current++;
        }
        buffers.endBatch();
    }

    public static void renderBillboard(PoseStack poseStack, MultiBufferSource buffers, Holder<IAspect> aspect, float scale, float alpha, boolean bw, int packedLight) {
        renderBillboard(poseStack, buffers, aspect, scale, alpha, bw, packedLight, AspectTagRenderer.BlendMode.ALPHA);
    }

    public static void renderBillboardAdditive(PoseStack poseStack, MultiBufferSource buffers, Holder<IAspect> aspect, float scale, float alpha, boolean bw, int packedLight) {
        renderBillboard(poseStack, buffers, aspect, scale, alpha, bw, packedLight, AspectTagRenderer.BlendMode.ADDITIVE);
    }

    public static void renderBillboard(PoseStack poseStack, MultiBufferSource buffers, Holder<IAspect> aspect, float scale, float alpha, boolean bw, int packedLight, AspectTagRenderer.BlendMode blend) {
        if (aspect == null || aspect.value() == null)
            return;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        IAspect value = aspect.value();
        int color = AspectTagRenderer.colorOf(value, alpha, bw);
        RenderType type = blend == AspectTagRenderer.BlendMode.ADDITIVE ? TCFlatRenderTypes.entityAdditiveFlat(value.texture()) : TCFlatRenderTypes.entityTranslucentFlat(value.texture());
        VertexConsumer buffer = buffers.getBuffer(type);
        poseStack.pushPose();
        poseStack.mulPose(camera.rotation());
        poseStack.scale(scale, scale, scale);
        PoseStack.Pose pose = poseStack.last();
        addQuadVertex(buffer, pose, -HALF_QUAD, -HALF_QUAD, 0.0F, 1.0F, color, packedLight);
        addQuadVertex(buffer, pose, HALF_QUAD, -HALF_QUAD, 1.0F, 1.0F, color, packedLight);
        addQuadVertex(buffer, pose, HALF_QUAD, HALF_QUAD, 1.0F, 0.0F, color, packedLight);
        addQuadVertex(buffer, pose, -HALF_QUAD, HALF_QUAD, 0.0F, 0.0F, color, packedLight);
        poseStack.popPose();
    }

    public static void renderQuad(PoseStack poseStack, VertexConsumer buffer, Holder<IAspect> aspect, float alpha, boolean bw, int packedLight) {
        renderQuad(poseStack.last(), buffer, aspect, alpha, bw, packedLight);
    }

    public static void renderQuad(PoseStack.Pose pose, VertexConsumer buffer, Holder<IAspect> aspect, float alpha, boolean bw, int packedLight) {
        if (aspect == null || aspect.value() == null)
            return;
        IAspect value = aspect.value();
        int color = AspectTagRenderer.colorOf(value, alpha, bw);
        addQuadVertex(buffer, pose, -HALF_QUAD, -HALF_QUAD, 0.0F, 1.0F, color, packedLight);
        addQuadVertex(buffer, pose, HALF_QUAD, -HALF_QUAD, 1.0F, 1.0F, color, packedLight);
        addQuadVertex(buffer, pose, HALF_QUAD, HALF_QUAD, 1.0F, 0.0F, color, packedLight);
        addQuadVertex(buffer, pose, -HALF_QUAD, HALF_QUAD, 0.0F, 0.0F, color, packedLight);
    }

    private static void addQuadVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int color, int packedLight) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
