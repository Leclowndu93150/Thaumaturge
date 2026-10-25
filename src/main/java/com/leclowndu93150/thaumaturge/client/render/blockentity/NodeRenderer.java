package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.NodeAnimation;
import com.leclowndu93150.thaumaturge.client.render.NodeTexture;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.compat.sable.SableClientCompat;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class NodeRenderer implements BlockEntityRenderer<BlockEntityNode> {
    private static final ResourceLocation NODES_TEXTURE = NodeTexture.TEXTURE;

    private static final RenderType NODE_ADDITIVE = TTRenderTypes.fxAdditive(NODES_TEXTURE);
    private static final RenderType NODE_ADDITIVE_NO_DEPTH = TTRenderTypes.fxAdditiveNoDepth(NODES_TEXTURE);
    private static final RenderType NODE_TRANSLUCENT = TTRenderTypes.fxTranslucent(NODES_TEXTURE);
    private static final RenderType NODE_TRANSLUCENT_NO_DEPTH = TTRenderTypes.fxTranslucentNoDepth(NODES_TEXTURE);

    private static final int GRID = 32;
    private static final double VIEW_DISTANCE = 64.0;
    private static final double THAUMOMETER_VIEW_DISTANCE = 48.0;
    private static final float FAINT_ALPHA = 0.0066F;
    // Iris routes this through its translucent particle program instead of the normal additive
    // path, so it needs a stronger source alpha to retain the same faint, unaided visibility.
    private static final float IRIS_FAINT_ALPHA = 0.4F;
    private static final float FAINT_SCALE = 0.5F;
    private static final float JARRED_SIZE = 0.7F;
    private static final float JARRED_HEIGHT = 0.4F;
    private static final int STRIP_ASPECT = 0;
    private static final int STRIP_NORMAL = 7;
    private static final int STRIP_DARK = 2;
    private static final int STRIP_HUNGRY = 8;
    private static final int STRIP_PURE = 9;
    private static final int STRIP_TAINTED = 5;
    private static final int STRIP_UNSTABLE = 6;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final float FRAME_ADVANCE_PER_TICK = 1.25F;
    private static final int TRANSLUCENT_BLEND = 771;
    private static final float TRANSLUCENT_ALPHA_BOOST = 1.5F;
    private static final float HIDDEN_BRIGHTNESS_MULTIPLIER = 0.10F;
    private static final float REVEALED_BRIGHTNESS_MULTIPLIER = 0.5F;
    private static final float DRAIN_LINE_SPEED = -0.02F;
    private static final float DRAIN_LINE_WIDTH = 0.15F;
    private static final float ENERGIZED_SPIN_FACTOR = 2.0F;
    private static final float ENERGIZED_LAYER_CONTRACTION = 0.6F;
    private static final float ENERGIZED_CORE_SCALE = 1.35F;
    private static final float ENERGIZED_PULSE_PERIOD = 4.0F;
    private static final float ENERGIZED_PULSE_AMPLITUDE = 0.25F;
    private static final double ORB_SWEEP = (ENERGIZED_CORE_SCALE + ENERGIZED_PULSE_AMPLITUDE) * Mth.SQRT_OF_TWO;
    private static final int ENERGIZED_CORE_COLOR = 0x99CCFF;

    public NodeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen(BlockEntityNode node) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityNode node) {
        AABB bounds = new AABB(node.getBlockPos()).inflate(ORB_SWEEP);
        UUID drainId = node.getDrainPlayer();
        Level level = node.getLevel();
        Player drainer = drainId == null || level == null ? null : level.getPlayerByUUID(drainId);
        return drainer == null ? bounds : bounds.minmax(drainer.getBoundingBox());
    }

    @Override
    public void render(
            BlockEntityNode node,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        final NodeRenderState data = build(node, partialTick, player);
        final Vec3 origin = Vec3.atCenterOf(node.getBlockPos());
        if (data.depthIgnore) {
            LateWorldRenderQueue.enqueueBlockEntityOverlay(
                    origin, (latePose, lateBuffers) -> drawDepthIgnoredLayers(data, latePose, lateBuffers));
            if (data.draining) {
                LateWorldRenderQueue.enqueueBlockEntity(
                        origin, (latePose, lateBuffers) -> drawDrainLine(data, latePose, lateBuffers));
            }
            return;
        }
        if (data.jarred) {
            poseStack.pushPose();
            poseStack.translate(0.5F, JARRED_HEIGHT, 0.5F);
            SableClientCompat.faceCamera(
                    node.getLevel(),
                    origin,
                    poseStack,
                    Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            drawLayers(data, poseStack, buffers);
            poseStack.popPose();
            if (data.draining) {
                LateWorldRenderQueue.enqueueBlockEntity(
                        origin, (latePose, lateBuffers) -> drawDrainLine(data, latePose, lateBuffers));
            }
            return;
        }
        // These layers retain depth testing, but do not write depth. Draw after water so
        // it cannot paint over a faint node that is closer to the camera.
        LateWorldRenderQueue.enqueueBlockEntityAfterTranslucent(
                origin, (latePose, lateBuffers) -> drawDeferredLayers(data, latePose, lateBuffers));
        if (data.draining) {
            LateWorldRenderQueue.enqueueBlockEntity(
                    origin, (latePose, lateBuffers) -> drawDrainLine(data, latePose, lateBuffers));
        }
    }

    private static void drawDeferredLayers(NodeRenderState data, PoseStack poseStack, MultiBufferSource buffers) {
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        drawLayers(data, poseStack, buffers);
    }

    private static void drawDepthIgnoredLayers(NodeRenderState data, PoseStack poseStack, MultiBufferSource buffers) {
        if (data.jarred) {
            poseStack.translate(0.0F, JARRED_HEIGHT - 0.5F, 0.0F);
        }
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        drawLayers(data, poseStack, buffers);
    }

    private static NodeRenderState build(BlockEntityNode node, float partialTicks, LocalPlayer player) {
        NodeRenderState state = new NodeRenderState();
        state.type = node.getNodeType();
        state.modifier = node.getNodeModifier();
        Vec3 center = Vec3.atCenterOf(node.getBlockPos());
        double distance = Math.sqrt(player.distanceToSqr(center));
        double viewDistance = VIEW_DISTANCE;
        state.size = 1.0F;
        state.jarred = node instanceof BlockEntityJarNode;
        state.energized = node.isEnergized();
        if (state.jarred) {
            state.visible = true;
            state.size = JARRED_SIZE;
        } else if (GogglesAccess.wearsRevealingGear(player)) {
            state.visible = true;
            state.depthIgnore = true;
        } else if (player.getMainHandItem().getItem() instanceof ThaumometerItem
                || player.getOffhandItem().getItem() instanceof ThaumometerItem) {
            state.visible = true;
            state.depthIgnore = true;
            viewDistance = THAUMOMETER_VIEW_DISTANCE;
        }
        if (distance > viewDistance) {
            state.visible = false;
        }
        state.ticks = player.tickCount + partialTicks;
        state.alpha = NodeAnimation.opacity(1.0F - (float) (distance / viewDistance), state.modifier, state.ticks);
        state.time = player.level().getGameTime();
        state.frameSeed = node.getBlockPos().getX();
        state.draining = false;
        if (node.getDrainPlayer() != null
                && player.level().getPlayerByUUID(node.getDrainPlayer()) instanceof Player drainer
                && drainer.isUsingItem()) {
            float useTicks = drainer.getTicksUsingItem() + partialTicks;
            Vec3 hand = drainHand(drainer, player, partialTicks);
            Vec3 nodeCenter = SableClientCompat.renderPosition(node.getLevel(), Vec3.atCenterOf(node.getBlockPos()));
            state.draining = true;
            state.drainFromX = hand.x - nodeCenter.x;
            state.drainFromY = hand.y - nodeCenter.y;
            state.drainFromZ = hand.z - nodeCenter.z;
            state.drainTime = Mth.clamp(useTicks / 10.0F, 0.0F, 1.0F);
            node.clientDrainRed = Math.round(Mth.lerp(0.2F, node.clientDrainRed, ARGB32.red(node.getDrainColor())));
            node.clientDrainGreen =
                    Math.round(Mth.lerp(0.2F, node.clientDrainGreen, ARGB32.green(node.getDrainColor())));
            node.clientDrainBlue = Math.round(Mth.lerp(0.2F, node.clientDrainBlue, ARGB32.blue(node.getDrainColor())));
            state.drainColor = ARGB32.color(255, node.clientDrainRed, node.clientDrainGreen, node.clientDrainBlue);
        }
        for (AspectInstance entry : node.getAspects().entries()) {
            NodeRenderState.AspectLayer layer = new NodeRenderState.AspectLayer();
            layer.color = entry.aspect().value().color();
            layer.amount = entry.amount();
            layer.blend = entry.aspect().value().blend();
            state.layers.add(layer);
        }
        return state;
    }

    private static Vec3 drainHand(Player drainer, LocalPlayer viewer, float partialTicks) {
        if (drainer == viewer && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            Vec3 tip = WandTipTracker.firstPersonTip();
            if (tip != null) {
                return tip;
            }
        }
        HumanoidArm arm = drainer.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? drainer.getMainArm()
                : drainer.getMainArm().getOpposite();
        float yaw = Mth.rotLerp(partialTicks, drainer.yRotO, drainer.getYRot());
        Vec3 side = Vec3.directionFromRotation(0.0F, yaw + 90.0F).scale(arm == HumanoidArm.RIGHT ? 0.23 : -0.23);
        return drainer.getEyePosition(partialTicks)
                .add(drainer.getViewVector(partialTicks).scale(0.56))
                .add(side)
                .add(0.0, -0.18, 0.0);
    }

    public static void drawLayers(NodeRenderState state, PoseStack poseStack, MultiBufferSource buffers) {
        float brightness = state.visible
                ? (state.depthIgnore ? REVEALED_BRIGHTNESS_MULTIPLIER : 1.0F)
                : HIDDEN_BRIGHTNESS_MULTIPLIER;
        forEachLayer(
                state,
                (index, type, angle, scale, alpha, color, strip, frame) -> drawLayer(
                        poseStack, buffers, type, angle, scale, alpha, scaleColor(color, brightness), strip, frame));
    }

    public interface LayerSink {
        void layer(int index, RenderType type, float angle, float scale, float alpha, int color, int strip, int frame);
    }

    public static void forEachLayer(NodeRenderState state, LayerSink sink) {
        int frame = Math.floorMod(Mth.floor(state.ticks * FRAME_ADVANCE_PER_TICK) + state.frameSeed, GRID);
        if (!state.visible || state.layers.isEmpty()) {
            float faintAlpha = IrisCompat.shadersActive() ? IRIS_FAINT_ALPHA : FAINT_ALPHA;
            RenderType faintType = state.visible && state.depthIgnore ? NODE_ADDITIVE_NO_DEPTH : NODE_ADDITIVE;
            sink.layer(0, faintType, 0.0F, FAINT_SCALE, faintAlpha, 0xFFFFFF, STRIP_NORMAL, frame);
            return;
        }
        int count = state.layers.size();
        float layerAlpha = state.alpha * Math.min(1.0F, 2.0F / count);
        RenderType aspectType = state.depthIgnore ? NODE_ADDITIVE_NO_DEPTH : NODE_ADDITIVE;
        RenderType translucentType = state.depthIgnore ? NODE_TRANSLUCENT_NO_DEPTH : NODE_TRANSLUCENT;
        float animationTicks = state.ticks * (state.energized ? ENERGIZED_SPIN_FACTOR : 1.0F);
        float shellSize = state.size * (state.energized ? ENERGIZED_LAYER_CONTRACTION : 1.0F);
        float totalCharge = 0.0F;
        for (int index = 0; index < count; index++) {
            NodeRenderState.AspectLayer layer = state.layers.get(index);
            float charge = NodeAnimation.charge(layer.amount);
            totalCharge += charge;
            boolean translucent = layer.blend == TRANSLUCENT_BLEND;
            sink.layer(
                    index,
                    translucent ? translucentType : aspectType,
                    NodeAnimation.shellAngle(animationTicks, index),
                    NodeAnimation.shellRadius(charge, state.ticks, index) * shellSize,
                    layerAlpha * (translucent ? TRANSLUCENT_ALPHA_BOOST : 1.0F),
                    layer.color,
                    STRIP_ASPECT,
                    frame);
        }
        float coreScale = NodeAnimation.coreRadius(totalCharge / count) * state.size;
        float coreAngle = NodeAnimation.shellAngle(animationTicks, count);
        int strip =
                switch (state.type) {
                    case NORMAL -> STRIP_NORMAL;
                    case UNSTABLE -> STRIP_UNSTABLE;
                    case DARK -> STRIP_DARK;
                    case TAINTED -> STRIP_TAINTED;
                    case PURE -> STRIP_PURE;
                    case HUNGRY -> STRIP_HUNGRY;
                };
        if (state.type == NodeType.HUNGRY) {
            coreScale *= 0.75F;
        }
        if (state.type == NodeType.UNSTABLE) {
            coreAngle = 0.0F;
        }
        boolean translucentCore = state.type == NodeType.DARK || state.type == NodeType.TAINTED;
        RenderType coreType = translucentCore ? translucentType : aspectType;
        sink.layer(count, coreType, coreAngle, coreScale, state.alpha, 0xFFFFFF, strip, frame);
        if (state.energized) {
            float pulse =
                    Mth.sin(state.ticks / ENERGIZED_PULSE_PERIOD) * ENERGIZED_PULSE_AMPLITUDE + ENERGIZED_CORE_SCALE;
            sink.layer(
                    count + 1,
                    aspectType,
                    -coreAngle,
                    coreScale * pulse,
                    state.alpha,
                    ENERGIZED_CORE_COLOR,
                    STRIP_UNSTABLE,
                    frame);
        }
    }

    private static void drawDrainLine(NodeRenderState state, PoseStack poseStack, MultiBufferSource buffers) {
        FloatyLineRenderer.draw(
                poseStack,
                buffers,
                new Vec3(state.drainFromX, state.drainFromY, state.drainFromZ),
                FloatyLineRenderer.time(state.time, state.ticks % 1.0F),
                state.drainColor,
                DRAIN_LINE_SPEED,
                state.drainTime,
                DRAIN_LINE_WIDTH);
    }

    private static void drawLayer(
            PoseStack poseStack,
            MultiBufferSource buffers,
            RenderType renderType,
            float angle,
            float scale,
            float alpha,
            int color,
            int strip,
            int frame) {
        int tint = ARGB32.color((int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F), color);
        poseStack.pushPose();
        if (angle != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotation(angle));
        }
        emitQuad(poseStack.last().pose(), buffers.getBuffer(renderType), scale, tint, strip, frame);
        poseStack.popPose();
    }

    private static int scaleColor(int color, float brightness) {
        return ((int) (((color >> 16) & 0xFF) * brightness) << 16)
                | ((int) (((color >> 8) & 0xFF) * brightness) << 8)
                | (int) ((color & 0xFF) * brightness);
    }

    public static void emitQuad(Matrix4f mat, VertexConsumer buffer, float half, int tint, int strip, int frame) {
        float u0 = frame / (float) GRID;
        float u1 = (frame + 1) / (float) GRID;
        float v0 = strip / (float) GRID;
        float v1 = (strip + 1) / (float) GRID;
        buffer.addVertex(mat, -half, -half, 0.0F).setUv(u1, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, -half, half, 0.0F).setUv(u1, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, half, half, 0.0F).setUv(u0, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, half, -half, 0.0F).setUv(u0, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
    }
}
