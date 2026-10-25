package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.BlockEntityEldritchPortal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class EldritchPortalRenderer implements BlockEntityRenderer<BlockEntityEldritchPortal> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/misc/eldritch_portal.png");
    private static final ResourceLocation OVERWORLD_TEXTURE = TTIds.rl("textures/misc/eldritch_portal_overworld.png");
    private static final RenderType PORTAL_TYPE = TTRenderTypes.fxTranslucent(TEXTURE);
    private static final RenderType OVERWORLD_PORTAL_TYPE = TTRenderTypes.fxTranslucent(OVERWORLD_TEXTURE);

    private static final int FRAMES = 32;
    private static final float FRAME_WIDTH = 1.0F / FRAMES;
    private static final float GROW_TICKS_WIDTH = 5.0F;
    private static final float GROW_TICKS_HEIGHT = 30.0F;
    private static final int LIGHT = 0x00F000DC;

    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityEldritchPortal portal,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        float openCount = portal.openTicks() + partialTick;
        if (openCount < 0.0F) {
            return;
        }
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        float animationTime = viewEntity == null ? partialTick : viewEntity.tickCount + partialTick;
        float sx = Math.min(GROW_TICKS_WIDTH, openCount) / GROW_TICKS_WIDTH;
        float sy = Math.min(GROW_TICKS_HEIGHT, openCount) / GROW_TICKS_HEIGHT;
        int frame = (int) animationTime % FRAMES;
        float u0 = frame * FRAME_WIDTH;
        float u1 = u0 + FRAME_WIDTH;
        boolean towardOverworld = portal.getLevel() != null && portal.getLevel().dimension() == OuterLands.DIMENSION;
        RenderType type = towardOverworld ? OVERWORLD_PORTAL_TYPE : PORTAL_TYPE;
        LateWorldRenderQueue.enqueueBlockEntityAfterTranslucent(
                Vec3.atCenterOf(portal.getBlockPos()),
                (latePose, lateBuffers) -> drawQuad(latePose, lateBuffers, type, sx, sy, u0, u1));
    }

    private static void drawQuad(
            PoseStack poseStack, MultiBufferSource buffers, RenderType type, float sx, float sy, float u0, float u1) {
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        VertexConsumer buffer = buffers.getBuffer(type);
        Matrix4f mat = poseStack.last().pose();
        buffer.addVertex(mat, -sx, -sy, 0.0F).setUv(u1, 0.0F).setColor(-1).setLight(LIGHT);
        buffer.addVertex(mat, -sx, sy, 0.0F).setUv(u1, 1.0F).setColor(-1).setLight(LIGHT);
        buffer.addVertex(mat, sx, sy, 0.0F).setUv(u0, 1.0F).setColor(-1).setLight(LIGHT);
        buffer.addVertex(mat, sx, -sy, 0.0F).setUv(u0, 0.0F).setColor(-1).setLight(LIGHT);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchPortal portal) {
        return new AABB(portal.getBlockPos()).inflate(1.5);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityEldritchPortal portal) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
