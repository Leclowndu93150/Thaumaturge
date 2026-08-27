package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchPortal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class EldritchPortalRenderer implements BlockEntityRenderer<BlockEntityEldritchPortal> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/misc/eldritch_portal.png");
    private static final RenderType PORTAL_TYPE = TCRenderTypes.fxTranslucent(TEXTURE);

    private static final int FRAMES = 16;
    private static final float FRAME_WIDTH = 0.0625F;
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
        float openCount = portal.opencount + partialTick;
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
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        VertexConsumer buffer = buffers.getBuffer(PORTAL_TYPE);
        Matrix4f mat = poseStack.last().pose();
        buffer.addVertex(mat, -sx, -sy, 0.0F).setUv(u1, 0.0F).setColor(-1).setLight(LIGHT);
        buffer.addVertex(mat, -sx, sy, 0.0F).setUv(u1, 1.0F).setColor(-1).setLight(LIGHT);
        buffer.addVertex(mat, sx, sy, 0.0F).setUv(u0, 1.0F).setColor(-1).setLight(LIGHT);
        buffer.addVertex(mat, sx, -sy, 0.0F).setUv(u0, 0.0F).setColor(-1).setLight(LIGHT);
        poseStack.popPose();
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
