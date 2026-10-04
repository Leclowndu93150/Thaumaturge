package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.VisRelayBeamRenderTypes;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

public final class VisRelayRenderer implements BlockEntityRenderer<BlockEntityVisRelay, VisRelayRenderState> {
    private static final float CRYSTAL_HEIGHT = 0.55F;
    private static final float BEAM_HALF_WIDTH = 0.105F;
    private static final int BEAM_QUADS = 2;
    private static final float QUAD_SCROLL_STEP = 1.0F / 3.0F;
    private static final float SCROLL_FAST = 0.2F;
    private static final float SCROLL_SLOW = 0.1F;
    private static final int LIGHT = 200;
    private static final float IDLE_OPACITY = 0.3F;
    private static final float PULSE_OPACITY = 0.8F;
    private static final float OPACITY_DECAY = 0.025F;
    private static final float COLOR_RECOVERY = 0.025F;
    private static final float BEAM_ALPHA = 0.1F;
    private static final float FLARE_ALPHA = 0.2F;
    private static final float REVEALED_ALPHA = 1.0F;
    private static final float FLARE_SIZE = 0.66F;
    private static final float FLARE_TEXEL_NUDGE = 0.0001F;

    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityVisRelay relay) {
        AABB self = new AABB(relay.getBlockPos());
        BlockPos parent = relay.parentPos();
        return parent == null ? self : self.minmax(new AABB(parent));
    }

    @Override
    public VisRelayRenderState createRenderState() {
        return new VisRelayRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityVisRelay relay, VisRelayRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(relay, state, partialTicks, cameraPosition, breakProgress);
        state.beamTarget = null;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !relay.isLinked() || relay.parentPos() == null) {
            return;
        }
        Vec3 own = Vec3.atCenterOf(relay.getBlockPos()).add(0.0, CRYSTAL_HEIGHT - 0.5, 0.0);
        state.beamTarget = Vec3.atCenterOf(relay.parentPos()).subtract(own);
        long now = player.level().getGameTime();
        float sincePulse = now - relay.pulseStart() + partialTicks;
        int color = relay.pulseColor();
        float recovery = COLOR_RECOVERY * Math.max(0.0F, sincePulse);
        state.red = Math.min(1.0F, ARGB.red(color) / 255.0F + recovery);
        state.green = Math.min(1.0F, ARGB.green(color) / 255.0F + recovery);
        state.blue = Math.min(1.0F, ARGB.blue(color) / 255.0F + recovery);
        state.opacity = sincePulse < BlockEntityVisRelay.PULSE_TICKS ? PULSE_OPACITY : Math.max(IDLE_OPACITY, PULSE_OPACITY - OPACITY_DECAY * (sincePulse - BlockEntityVisRelay.PULSE_TICKS));
        float slide = player.tickCount + partialTicks;
        state.scroll = -slide * SCROLL_FAST - Mth.floor(-slide * SCROLL_SLOW);
        state.revealing = GogglesAccess.wearsRevealingGear(player);
        state.flareFrame = (int) (now % ParticleTextures.STAR_GLINT_FRAMES);
    }

    @Override
    public void submit(VisRelayRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.beamTarget == null) {
            return;
        }
        Vec3 origin = Vec3.atCenterOf(state.blockPos).add(0.0, CRYSTAL_HEIGHT - 0.5, 0.0);
        Vec3 start = state.beamTarget;
        float beamAlpha = state.opacity * (state.revealing ? REVEALED_ALPHA : BEAM_ALPHA);
        float flareAlpha = state.opacity * (state.revealing ? REVEALED_ALPHA : FLARE_ALPHA);
        int beamColor = ARGB.colorFromFloat(beamAlpha, state.red, state.green, state.blue);
        int flareColor = ARGB.colorFromFloat(flareAlpha, state.red, state.green, state.blue);
        float scroll = state.scroll;
        float flareHalf = FLARE_SIZE * state.opacity;
        int frame = state.flareFrame;
        LateWorldRenderQueue.enqueue(origin, (latePose, buffers) -> {
            drawBeam(latePose, buffers, start, scroll, beamColor);
            drawFlare(latePose, buffers, flareHalf, frame, flareColor);
        });
    }

    private static void drawBeam(PoseStack poseStack, MultiBufferSource buffers, Vec3 start, float scroll, int color) {
        float length = (float) start.length();
        if (length <= 0.0F) {
            return;
        }
        Vector3f axis = new Vector3f((float) -start.x, (float) -start.y, (float) -start.z).div(length);
        Vector3f reference = Math.abs(axis.y) < 0.99F ? new Vector3f(0.0F, 1.0F, 0.0F) : new Vector3f(1.0F, 0.0F, 0.0F);
        Vector3f first = new Vector3f(axis).cross(reference).normalize();
        Vector3f second = new Vector3f(axis).cross(first).normalize();
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(VisRelayBeamRenderTypes.BEAM);
        float sx = (float) start.x;
        float sy = (float) start.y;
        float sz = (float) start.z;
        for (int quad = 0; quad < BEAM_QUADS; quad++) {
            Vector3f side = quad == 0 ? first : second;
            float wx = side.x * BEAM_HALF_WIDTH;
            float wy = side.y * BEAM_HALF_WIDTH;
            float wz = side.z * BEAM_HALF_WIDTH;
            float v0 = -1.0F + scroll + quad * QUAD_SCROLL_STEP;
            float v1 = length + v0;
            buffer.addVertex(pose, -wx, -wy, -wz).setUv(1.0F, v1).setColor(color).setLight(LIGHT);
            buffer.addVertex(pose, sx - wx, sy - wy, sz - wz).setUv(1.0F, v0).setColor(color).setLight(LIGHT);
            buffer.addVertex(pose, sx + wx, sy + wy, sz + wz).setUv(0.0F, v0).setColor(color).setLight(LIGHT);
            buffer.addVertex(pose, wx, wy, wz).setUv(0.0F, v1).setColor(color).setLight(LIGHT);
        }
    }

    private static void drawFlare(PoseStack poseStack, MultiBufferSource buffers, float half, int frame, int color) {
        float u0 = StripUv.u0(frame, ParticleTextures.STAR_GLINT_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.STAR_GLINT_FRAMES) - FLARE_TEXEL_NUDGE;
        float v0 = StripUv.V0;
        float v1 = StripUv.V1 - FLARE_TEXEL_NUDGE;
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(VisRelayBeamRenderTypes.FLARE);
        buffer.addVertex(pose, -half, -half, 0.0F).setUv(u1, v1).setColor(color).setLight(LIGHT);
        buffer.addVertex(pose, -half, half, 0.0F).setUv(u1, v0).setColor(color).setLight(LIGHT);
        buffer.addVertex(pose, half, half, 0.0F).setUv(u0, v0).setColor(color).setLight(LIGHT);
        buffer.addVertex(pose, half, -half, 0.0F).setUv(u0, v1).setColor(color).setLight(LIGHT);
        poseStack.popPose();
    }
}
