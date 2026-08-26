package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeStabilizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class NodeStabilizerRenderer implements BlockEntityRenderer<BlockEntityNodeStabilizer, NodeStabilizerRenderState> {
    private static final Identifier MODEL = TCIds.rl("models/mesh/node_stabilizer.tcmesh");
    private static final Identifier TEXTURE = TCIds.rl("textures/block/node_stabilizer.png");
    private static final Identifier OVERLAY_TEXTURE = TCIds.rl("textures/block/node_stabilizer_over.png");

    private static final RenderType BASE = RenderTypes.entityCutout(TEXTURE);
    private static final RenderType OVERLAY = RenderTypes.entityTranslucentEmissive(OVERLAY_TEXTURE);
    private static final Identifier TRANSDUCER_TEXTURE = TCIds.rl("textures/block/node_converter.png");
    private static final Identifier TRANSDUCER_OVERLAY_TEXTURE = TCIds.rl("textures/block/node_converter_over.png");
    private static final RenderType TRANSDUCER_BASE = RenderTypes.entityCutout(TRANSDUCER_TEXTURE);
    private static final RenderType TRANSDUCER_OVERLAY = RenderTypes.entityTranslucentEmissive(TRANSDUCER_OVERLAY_TEXTURE);
    private static final float TRANSDUCER_EXTEND = 0.4F;
    private static final float TRANSDUCER_GLOW_GAIN = 2.5F;
    private static final Identifier BUBBLE_TEXTURE = TCIds.rl("textures/misc/node_bubble.png");
    private static final RenderType BUBBLE = RenderType.create("tc_node_bubble",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", BUBBLE_TEXTURE).useLightmap().sortOnUpload().createRenderSetup());

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
    private static final double BUBBLE_SWEEP = BUBBLE_HALF * Mth.SQRT_OF_TWO;

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public NodeStabilizerRenderState createRenderState() {
        return new NodeStabilizerRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityNodeStabilizer stabilizer, NodeStabilizerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(stabilizer, state, partialTicks, cameraPosition, breakProgress);
        state.count = stabilizer.count;
        state.advanced = stabilizer.isAdvanced();
        LocalPlayer player = Minecraft.getInstance().player;
        state.ticks = player == null ? 0.0F : player.tickCount + partialTicks;
        state.light = state.lightCoords;
    }

    @Override
    public void submit(NodeStabilizerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        submitParts(state.count, state.advanced, state.ticks, poseStack, collector, state.light);
        poseStack.popPose();
        if (state.count > 0) {
            float pulse = Mth.sin(state.ticks / BUBBLE_PULSE_PERIOD) * BUBBLE_ALPHA_PULSE + BUBBLE_ALPHA_BASE;
            float alpha = state.count / (float) BlockEntityNodeStabilizer.MAX_COUNT * pulse;
            int tint = state.advanced ? BUBBLE_ADVANCED_TINT : 0xFFFFFF;
            int r = (int) (((tint >> 16) & 0xFF) * alpha);
            int g = (int) (((tint >> 8) & 0xFF) * alpha);
            int b = (int) ((tint & 0xFF) * alpha);
            int color = 0xFF000000 | (r << 16) | (g << 8) | b;
            Vec3 origin = Vec3.atCenterOf(state.blockPos).add(0.0, BUBBLE_LIFT - 0.5, 0.0);
            LateWorldRenderQueue.enqueue(origin, (latePose, buffers) -> drawBubble(latePose, buffers, color));
        }
    }

    private static void drawBubble(PoseStack poseStack, MultiBufferSource buffers, int color) {
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(BUBBLE);
        buffer.addVertex(pose, -BUBBLE_HALF, -BUBBLE_HALF, 0.0F).setUv(0.0F, 1.0F).setColor(color).setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, BUBBLE_HALF, -BUBBLE_HALF, 0.0F).setUv(1.0F, 1.0F).setColor(color).setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, BUBBLE_HALF, BUBBLE_HALF, 0.0F).setUv(1.0F, 0.0F).setColor(color).setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, -BUBBLE_HALF, BUBBLE_HALF, 0.0F).setUv(0.0F, 0.0F).setColor(color).setLight(BUBBLE_LIGHT);
        poseStack.popPose();
    }

    public static void submitParts(int count, boolean advanced, float ticks, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TCMesh mesh = GolemMeshes.get(MODEL);
        TCMeshPart lock = findPart(mesh, PART_LOCK);
        TCMeshPart piston = findPart(mesh, PART_PISTON);
        if (lock != null) {
            PoseStack.Pose lockPose = poseStack.last().copy();
            collector.submitCustomGeometry(poseStack, BASE, (pose, buffer) -> GolemMeshes.renderPart(lock, lockPose, buffer, light, WHITE));
        }
        if (piston != null) {
            for (int arm = 0; arm < ARM_COUNT; arm++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(ARM_ANGLE_STEP * arm));
                poseStack.mulPose(Axis.YP.rotationDegrees(ARM_TWIST));
                poseStack.translate(0.0F, 0.0F, count / EXTEND_DIVISOR);
                PoseStack.Pose armPose = poseStack.last().copy();
                collector.submitCustomGeometry(poseStack, BASE, (pose, buffer) -> GolemMeshes.renderPart(piston, armPose, buffer, light, WHITE));
                float pulse = Mth.sin((ticks + arm * 5) / 3.0F) * 0.1F + 0.9F;
                int glow = OVERLAY_LIGHT_BASE + (int) (OVERLAY_LIGHT_RANGE * (count / (float) BlockEntityNodeStabilizer.MAX_COUNT * pulse));
                int glowUnit = Mth.clamp(glow / 16, 0, 15);
                int glowLight = (glowUnit << 4) | (glowUnit << 20);
                int tint = advanced ? ADVANCED_TINT : WHITE;
                collector.submitCustomGeometry(poseStack, OVERLAY, (pose, buffer) -> GolemMeshes.renderPart(piston, armPose, buffer, glowLight, tint));
                poseStack.popPose();
            }
        }
    }

    public static void submitTransducerParts(float chargeFraction, float ticks, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TCMesh mesh = GolemMeshes.get(MODEL);
        TCMeshPart lock = findPart(mesh, PART_LOCK);
        TCMeshPart piston = findPart(mesh, PART_PISTON);
        if (lock != null) {
            PoseStack.Pose lockPose = poseStack.last().copy();
            collector.submitCustomGeometry(poseStack, TRANSDUCER_BASE, (pose, buffer) -> GolemMeshes.renderPart(lock, lockPose, buffer, light, WHITE));
            float pulse = Mth.sin(ticks / 3.0F) * 0.1F + 0.9F;
            int glow = OVERLAY_LIGHT_BASE + (int) (OVERLAY_LIGHT_RANGE * Math.min(1.0F, chargeFraction * TRANSDUCER_GLOW_GAIN * pulse));
            int glowUnit = Mth.clamp(glow / 16, 0, 15);
            int glowLight = (glowUnit << 4) | (glowUnit << 20);
            collector.submitCustomGeometry(poseStack, TRANSDUCER_OVERLAY, (pose, buffer) -> GolemMeshes.renderPart(lock, lockPose, buffer, glowLight, WHITE));
        }
        if (piston != null) {
            for (int arm = 0; arm < ARM_COUNT; arm++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(ARM_ANGLE_STEP * arm));
                poseStack.mulPose(Axis.YP.rotationDegrees(ARM_TWIST));
                float armPulse = Mth.sin((ticks + arm * 5) / 3.0F) * 0.1F + 0.9F;
                poseStack.translate(0.0F, 0.0F, chargeFraction * armPulse * TRANSDUCER_EXTEND);
                PoseStack.Pose armPose = poseStack.last().copy();
                collector.submitCustomGeometry(poseStack, TRANSDUCER_BASE, (pose, buffer) -> GolemMeshes.renderPart(piston, armPose, buffer, light, WHITE));
                poseStack.popPose();
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityNodeStabilizer stabilizer) {
        BlockPos pos = stabilizer.getBlockPos();
        Vec3 bubble = Vec3.atCenterOf(pos).add(0.0, BUBBLE_LIFT - 0.5, 0.0);
        return new AABB(pos).minmax(AABB.ofSize(bubble, BUBBLE_SWEEP * 2.0, BUBBLE_SWEEP * 2.0, BUBBLE_SWEEP * 2.0));
    }

    private static @Nullable TCMeshPart findPart(TCMesh mesh, String name) {
        for (TCMeshPart part : mesh.parts()) {
            if (name.equals(part.name())) {
                return part;
            }
        }
        return null;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityNodeStabilizer blockEntity) {
        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity).expandTowards(0.0,1.1,0.0);
    }
}
