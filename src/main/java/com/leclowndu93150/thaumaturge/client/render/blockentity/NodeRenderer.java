package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class NodeRenderer implements BlockEntityRenderer<BlockEntityNode> {
    private static final ResourceLocation NODES_TEXTURE = TCIds.rl("textures/misc/nodes.png");

    private static final RenderType NODE_ADDITIVE = TCRenderTypes.fxAdditive(NODES_TEXTURE);
    private static final RenderType NODE_ADDITIVE_NO_DEPTH = TCRenderTypes.fxAdditiveNoDepth(NODES_TEXTURE);
    private static final RenderType NODE_TRANSLUCENT = TCRenderTypes.fxTranslucent(NODES_TEXTURE);
    private static final RenderType NODE_TRANSLUCENT_NO_DEPTH = TCRenderTypes.fxTranslucentNoDepth(NODES_TEXTURE);

    private static final int GRID = 32;
    private static final double VIEW_DISTANCE = 64.0;
    private static final double THAUMOMETER_VIEW_DISTANCE = 48.0;
    private static final float BASE_LAYER_SCALE = 0.25F;
    private static final float FAINT_ALPHA = 0.1F;
    private static final float FAINT_SCALE = 0.5F;
    private static final float JARRED_SIZE = 0.7F;
    private static final float JARRED_HEIGHT = 0.4F;
    private static final int STRIP_ASPECT = 0;
    private static final int STRIP_NORMAL = 1;
    private static final int STRIP_DARK = 2;
    private static final int STRIP_HUNGRY = 3;
    private static final int STRIP_PURE = 4;
    private static final int STRIP_TAINTED = 5;
    private static final int STRIP_UNSTABLE = 6;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final float FRAME_ADVANCE_PER_TICK = 1.25F;
    private static final float TICKS_TO_CLOCK_UNITS = 10.0F;
    private static final float LAYER_PERIOD_BASE = 5000.0F;
    private static final float LAYER_PERIOD_STEP = 500.0F;
    private static final int TRANSLUCENT_BLEND = 771;
    private static final float TRANSLUCENT_ALPHA_BOOST = 1.5F;
    private static final float WHITE_ALPHA_CLAMP = 1.0F;
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
        return new AABB(node.getBlockPos()).inflate(ORB_SWEEP);
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
        if (data.jarred) {
            poseStack.pushPose();
            poseStack.translate(0.5F, JARRED_HEIGHT, 0.5F);
            poseStack.mulPose(
                    Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            drawLayers(data, poseStack, buffers);
            poseStack.popPose();
            if (data.draining) {
                LateWorldRenderQueue.enqueueBlockEntity(
                        origin, (latePose, lateBuffers) -> drawDrainLine(data, latePose, lateBuffers));
            }
            return;
        }
        LateWorldRenderQueue.enqueueBlockEntity(
                origin, (latePose, lateBuffers) -> drawLate(data, latePose, lateBuffers));
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
        } else if (GogglesAccess.revealsNodes(player)) {
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
        float alpha = (float) ((viewDistance - distance) / viewDistance);
        if (state.modifier != null) {
            alpha = switch (state.modifier) {
                case BRIGHT -> alpha * 1.5F;
                case PALE -> alpha * 0.66F;
                case FADING -> alpha * (Mth.sin((player.tickCount + partialTicks) / 3.0F) * 0.25F + 0.33F);
            };
        }
        state.alpha = Math.min(alpha, WHITE_ALPHA_CLAMP);
        state.ticks = player.tickCount + partialTicks;
        state.time = player.level().getGameTime();
        state.frameSeed = node.getBlockPos().getX();
        state.draining = false;
        if (node.getDrainPlayer() != null
                && player.level().getPlayerByUUID(node.getDrainPlayer()) instanceof Player drainer
                && drainer.isUsingItem()) {
            float useTicks = drainer.getTicksUsingItem() + partialTicks;
            float wave = Mth.sin(useTicks / 10.0F) * 10.0F;
            float pitch = Mth.lerp(partialTicks, drainer.xRotO, drainer.getXRot());
            float yaw = Mth.lerp(partialTicks, drainer.yRotO, drainer.getYRot());
            Vec3 offset = new Vec3(-0.1, -0.1, 0.5)
                    .xRot(-pitch * Mth.DEG_TO_RAD)
                    .yRot(-yaw * Mth.DEG_TO_RAD)
                    .yRot(-wave * 0.01F)
                    .xRot(-wave * 0.015F);
            Vec3 hand = new Vec3(
                            Mth.lerp(partialTicks, drainer.xo, drainer.getX()),
                            Mth.lerp(partialTicks, drainer.yo, drainer.getY()) + drainer.getEyeHeight(),
                            Mth.lerp(partialTicks, drainer.zo, drainer.getZ()))
                    .add(offset);
            if (drainer == player
                    && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                Vec3 tip = WandTipTracker.firstPersonTip();
                if (tip != null) {
                    hand = tip;
                }
            }
            Vec3 nodeCenter = Vec3.atCenterOf(node.getBlockPos());
            state.draining = true;
            state.drainFromX = hand.x - nodeCenter.x;
            state.drainFromY = hand.y - nodeCenter.y;
            state.drainFromZ = hand.z - nodeCenter.z;
            state.drainTime = Math.min(drainer.getTicksUsingItem() + partialTicks, 10.0F) / 10.0F;
            node.clientDrainRed = (((node.getDrainColor() >> 16) & 0xFF) + node.clientDrainRed * 4) / 5;
            node.clientDrainGreen = (((node.getDrainColor() >> 8) & 0xFF) + node.clientDrainGreen * 4) / 5;
            node.clientDrainBlue = ((node.getDrainColor() & 0xFF) + node.clientDrainBlue * 4) / 5;
            state.drainColor = (node.clientDrainRed << 16) | (node.clientDrainGreen << 8) | node.clientDrainBlue;
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

    public static void drawLayers(NodeRenderState state, PoseStack poseStack, MultiBufferSource buffers) {
        forEachLayer(
                state,
                (index, type, angle, scale, alpha, color, strip, frame) ->
                        drawLayer(poseStack, buffers, type, angle, scale, alpha, color, strip, frame));
    }

    public interface LayerSink {
        void layer(int index, RenderType type, float angle, float scale, float alpha, int color, int strip, int frame);
    }

    public static void forEachLayer(NodeRenderState state, LayerSink sink) {
        int frame = (int) ((state.ticks * FRAME_ADVANCE_PER_TICK + state.frameSeed) % GRID + GRID) % GRID;
        if (!state.visible || state.layers.isEmpty()) {
            sink.layer(0, NODE_ADDITIVE, 0.0F, FAINT_SCALE, FAINT_ALPHA, 0xFFFFFF, STRIP_NORMAL, frame);
            return;
        }
        float average = 0.0F;
        int count = 0;
        float layerAlpha = state.alpha / Math.max(1.0F, state.layers.size() / 2.0F);
        RenderType aspectType = state.depthIgnore ? NODE_ADDITIVE_NO_DEPTH : NODE_ADDITIVE;
        RenderType translucentType = state.depthIgnore ? NODE_TRANSLUCENT_NO_DEPTH : NODE_TRANSLUCENT;
        float clock = state.ticks * TICKS_TO_CLOCK_UNITS * (state.energized ? ENERGIZED_SPIN_FACTOR : 1.0F);
        float layerContraction = state.energized ? ENERGIZED_LAYER_CONTRACTION : 1.0F;
        float angle = 0.0F;
        for (NodeRenderState.AspectLayer layer : state.layers) {
            average += layer.amount;
            float scale = Mth.sin(state.ticks / (14.0F - count)) * BASE_LAYER_SCALE + BASE_LAYER_SCALE * 2.0F;
            scale = (0.2F + scale * (layer.amount / 50.0F)) * state.size * layerContraction;
            float period = LAYER_PERIOD_BASE + LAYER_PERIOD_STEP * count;
            angle = (clock % period) / period * Mth.TWO_PI;
            boolean translucent = layer.blend == TRANSLUCENT_BLEND;
            sink.layer(
                    count,
                    translucent ? translucentType : aspectType,
                    angle,
                    scale,
                    layerAlpha * (translucent ? TRANSLUCENT_ALPHA_BOOST : 1.0F),
                    layer.color,
                    STRIP_ASPECT,
                    frame);
            count++;
        }
        average /= state.layers.size();
        float coreScale = (0.1F + average / 150.0F) * state.size;
        float coreAngle = angle;
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

    private static void drawLate(NodeRenderState state, PoseStack poseStack, MultiBufferSource buffers) {
        if (state.draining) {
            drawDrainLine(state, poseStack, buffers);
        }
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        drawLayers(state, poseStack, buffers);
        poseStack.popPose();
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
