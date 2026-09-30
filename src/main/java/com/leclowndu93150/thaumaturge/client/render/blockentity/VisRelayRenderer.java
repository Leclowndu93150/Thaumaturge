package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.VisRelayBeamRenderTypes;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class VisRelayRenderer implements BlockEntityRenderer<BlockEntityVisRelay> {
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
    private static final int FLARE_FRAMES = 16;
    private static final float FLARE_TEXEL_NUDGE = 0.0001F;

    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityVisRelay relay,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockPos parent = relay.parentPos();
        LocalPlayer player = Minecraft.getInstance().player;
        if (parent == null || !relay.isLinked() || relay.getLevel() == null || player == null) {
            return;
        }
        Vec3 own = Vec3.atCenterOf(relay.getBlockPos()).add(0.0, CRYSTAL_HEIGHT - 0.5, 0.0);
        Vec3 start = Vec3.atCenterOf(parent).subtract(own);
        long now = relay.getLevel().getGameTime();
        float sincePulse = now - relay.pulseStart() + partialTick;
        int color = relay.pulseColor();
        float recovery = COLOR_RECOVERY * Math.max(0.0F, sincePulse);
        float red = Math.min(1.0F, ARGB32.red(color) / 255.0F + recovery);
        float green = Math.min(1.0F, ARGB32.green(color) / 255.0F + recovery);
        float blue = Math.min(1.0F, ARGB32.blue(color) / 255.0F + recovery);
        float opacity = sincePulse < BlockEntityVisRelay.PULSE_TICKS
                ? PULSE_OPACITY
                : Math.max(
                        IDLE_OPACITY, PULSE_OPACITY - OPACITY_DECAY * (sincePulse - BlockEntityVisRelay.PULSE_TICKS));
        float slide = player.tickCount + partialTick;
        float scroll = -slide * SCROLL_FAST - Mth.floor(-slide * SCROLL_SLOW);
        boolean revealing = GogglesAccess.revealsNodes(player);
        int beamColor = ARGB32.colorFromFloat(opacity * (revealing ? REVEALED_ALPHA : BEAM_ALPHA), red, green, blue);
        int flareColor = ARGB32.colorFromFloat(opacity * (revealing ? REVEALED_ALPHA : FLARE_ALPHA), red, green, blue);
        float flareHalf = FLARE_SIZE * opacity;
        int frame = (int) (now % FLARE_FRAMES);
        LateWorldRenderQueue.enqueueBlockEntity(own, (latePose, lateBuffers) -> {
            drawBeam(latePose, lateBuffers, start, scroll, beamColor);
            drawFlare(latePose, lateBuffers, flareHalf, frame, flareColor);
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
            buffer.addVertex(pose, -wx, -wy, -wz)
                    .setUv(1.0F, v1)
                    .setColor(color)
                    .setLight(LIGHT);
            buffer.addVertex(pose, sx - wx, sy - wy, sz - wz)
                    .setUv(1.0F, v0)
                    .setColor(color)
                    .setLight(LIGHT);
            buffer.addVertex(pose, sx + wx, sy + wy, sz + wz)
                    .setUv(0.0F, v0)
                    .setColor(color)
                    .setLight(LIGHT);
            buffer.addVertex(pose, wx, wy, wz).setUv(0.0F, v1).setColor(color).setLight(LIGHT);
        }
    }

    private static void drawFlare(PoseStack poseStack, MultiBufferSource buffers, float half, int frame, int color) {
        float u0 = frame / (float) FLARE_FRAMES;
        float u1 = (frame + 1) / (float) FLARE_FRAMES - FLARE_TEXEL_NUDGE;
        float v0 = 0.0F;
        float v1 = 1.0F - FLARE_TEXEL_NUDGE;
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

    @Override
    public boolean shouldRenderOffScreen(BlockEntityVisRelay be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityVisRelay relay) {
        AABB self = new AABB(relay.getBlockPos());
        BlockPos parent = relay.parentPos();
        return parent == null ? self : self.minmax(new AABB(parent));
    }
}
