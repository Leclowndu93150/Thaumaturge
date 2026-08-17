package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeStabilizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class NodeStabilizerRenderer implements BlockEntityRenderer<BlockEntityNodeStabilizer> {
    private static final ResourceLocation MODEL = TCIds.rl("models/mesh/node_stabilizer.tcmesh");
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/block/node_stabilizer.png");
    private static final ResourceLocation OVERLAY_TEXTURE = TCIds.rl("textures/block/node_stabilizer_over.png");

    private static final RenderType BASE = RenderType.entityCutout(TEXTURE);
    private static final RenderType OVERLAY = RenderType.entityTranslucentEmissive(OVERLAY_TEXTURE);
    private static final ResourceLocation TRANSDUCER_TEXTURE = TCIds.rl("textures/block/node_converter.png");
    private static final ResourceLocation TRANSDUCER_OVERLAY_TEXTURE =
            TCIds.rl("textures/block/node_converter_over.png");
    private static final RenderType TRANSDUCER_BASE = RenderType.entityCutout(TRANSDUCER_TEXTURE);
    private static final RenderType TRANSDUCER_OVERLAY =
            RenderType.entityTranslucentEmissive(TRANSDUCER_OVERLAY_TEXTURE);
    private static final float TRANSDUCER_EXTEND = 0.4F;
    private static final float TRANSDUCER_GLOW_GAIN = 2.5F;
    private static final ResourceLocation BUBBLE_TEXTURE = TCIds.rl("textures/misc/node_bubble.png");
    private static final RenderType BUBBLE = TCRenderTypes.fxAdditive(BUBBLE_TEXTURE);

    private static final String PART_LOCK = "lock";
    private static final String PART_PISTON = "piston";
    private static final int ARM_COUNT = 4;
    private static final float ARM_ANGLE_STEP = 90.0F;
    private static final float ARM_TWIST = 45.0F;
    private static final float EXTEND_DIVISOR = 100.0F;
    private static final int ADVANCED_TINT = 0xFFFF3333;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int OVERLAY_LIGHT_BASE = 50;
    private static final int OVERLAY_LIGHT_RANGE = 170;
    private static final float BUBBLE_HALF = 0.9F;
    private static final float BUBBLE_LIFT = 1.5F;
    private static final float BUBBLE_ALPHA_BASE = 0.5F;
    private static final float BUBBLE_ALPHA_PULSE = 0.1F;
    private static final float BUBBLE_PULSE_PERIOD = 8.0F;
    private static final int BUBBLE_ADVANCED_TINT = 0xFF4444;
    private static final int BUBBLE_LIGHT = 0x00F000F0;

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public AABB getRenderBoundingBox(BlockEntityNodeStabilizer stabilizer) {
        Vec3 center = Vec3.atCenterOf(stabilizer.getBlockPos()).add(0.0, BUBBLE_LIFT - 0.5, 0.0);
        return new AABB(center, center).inflate(BUBBLE_HALF);
    }

    @Override
    public void render(
            BlockEntityNodeStabilizer stabilizer,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        int count = stabilizer.count;
        boolean advanced = stabilizer.isAdvanced();
        LocalPlayer player = Minecraft.getInstance().player;
        float ticks = player == null ? 0.0F : player.tickCount + partialTick;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        submitParts(count, advanced, ticks, poseStack, buffers, light);
        poseStack.popPose();
        if (count > 0) {
            float pulse = Mth.sin(ticks / BUBBLE_PULSE_PERIOD) * BUBBLE_ALPHA_PULSE + BUBBLE_ALPHA_BASE;
            float alpha = count / (float) BlockEntityNodeStabilizer.MAX_COUNT * pulse;
            int tint = advanced ? BUBBLE_ADVANCED_TINT : 0xFFFFFF;
            int color = ARGB32.color((int) (alpha * 255.0F), tint);
            Vec3 origin = Vec3.atCenterOf(stabilizer.getBlockPos()).add(0.0, BUBBLE_LIFT - 0.5, 0.0);
            LateWorldRenderQueue.enqueue(origin, (latePose, lateBuffers) -> drawBubble(latePose, lateBuffers, color));
        }
    }

    private static void drawBubble(PoseStack poseStack, MultiBufferSource buffers, int color) {
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(BUBBLE);
        buffer.addVertex(pose, -BUBBLE_HALF, -BUBBLE_HALF, 0.0F)
                .setUv(0.0F, 1.0F)
                .setColor(color)
                .setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, BUBBLE_HALF, -BUBBLE_HALF, 0.0F)
                .setUv(1.0F, 1.0F)
                .setColor(color)
                .setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, BUBBLE_HALF, BUBBLE_HALF, 0.0F)
                .setUv(1.0F, 0.0F)
                .setColor(color)
                .setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, -BUBBLE_HALF, BUBBLE_HALF, 0.0F)
                .setUv(0.0F, 0.0F)
                .setColor(color)
                .setLight(BUBBLE_LIGHT);
        poseStack.popPose();
    }

    public static void submitParts(
            int count, boolean advanced, float ticks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        TCMesh mesh = GolemMeshes.get(MODEL);
        TCMeshPart lock = findPart(mesh, PART_LOCK);
        TCMeshPart piston = findPart(mesh, PART_PISTON);
        if (lock != null) {
            GolemMeshes.renderPart(lock, poseStack.last(), buffers.getBuffer(BASE), light, WHITE);
        }
        if (piston != null) {
            for (int arm = 0; arm < ARM_COUNT; arm++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(ARM_ANGLE_STEP * arm));
                poseStack.mulPose(Axis.YP.rotationDegrees(ARM_TWIST));
                poseStack.translate(0.0F, 0.0F, count / EXTEND_DIVISOR);
                PoseStack.Pose armPose = poseStack.last();
                GolemMeshes.renderPart(piston, armPose, buffers.getBuffer(BASE), light, WHITE);
                float pulse = Mth.sin((ticks + arm * 5) / 3.0F) * 0.1F + 0.9F;
                int glow = OVERLAY_LIGHT_BASE
                        + (int) (OVERLAY_LIGHT_RANGE * (count / (float) BlockEntityNodeStabilizer.MAX_COUNT * pulse));
                int glowUnit = Mth.clamp(glow / 16, 0, 15);
                int glowLight = (glowUnit << 4) | (glowUnit << 20);
                int tint = advanced ? ADVANCED_TINT : WHITE;
                GolemMeshes.renderPart(piston, armPose, buffers.getBuffer(OVERLAY), glowLight, tint);
                poseStack.popPose();
            }
        }
    }

    public static void submitTransducerParts(
            float chargeFraction, float ticks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        TCMesh mesh = GolemMeshes.get(MODEL);
        TCMeshPart lock = findPart(mesh, PART_LOCK);
        TCMeshPart piston = findPart(mesh, PART_PISTON);
        if (lock != null) {
            PoseStack.Pose lockPose = poseStack.last();
            GolemMeshes.renderPart(lock, lockPose, buffers.getBuffer(TRANSDUCER_BASE), light, WHITE);
            float pulse = Mth.sin(ticks / 3.0F) * 0.1F + 0.9F;
            int glow = OVERLAY_LIGHT_BASE
                    + (int) (OVERLAY_LIGHT_RANGE * Math.min(1.0F, chargeFraction * TRANSDUCER_GLOW_GAIN * pulse));
            int glowUnit = Mth.clamp(glow / 16, 0, 15);
            int glowLight = (glowUnit << 4) | (glowUnit << 20);
            GolemMeshes.renderPart(lock, lockPose, buffers.getBuffer(TRANSDUCER_OVERLAY), glowLight, WHITE);
        }
        if (piston != null) {
            for (int arm = 0; arm < ARM_COUNT; arm++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(ARM_ANGLE_STEP * arm));
                poseStack.mulPose(Axis.YP.rotationDegrees(ARM_TWIST));
                float armPulse = Mth.sin((ticks + arm * 5) / 3.0F) * 0.1F + 0.9F;
                poseStack.translate(0.0F, 0.0F, chargeFraction * armPulse * TRANSDUCER_EXTEND);
                GolemMeshes.renderPart(piston, poseStack.last(), buffers.getBuffer(TRANSDUCER_BASE), light, WHITE);
                poseStack.popPose();
            }
        }
    }

    private static @Nullable TCMeshPart findPart(TCMesh mesh, String name) {
        for (TCMeshPart part : mesh.parts()) {
            if (name.equals(part.name())) {
                return part;
            }
        }
        return null;
    }
}
