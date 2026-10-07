package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.items.IGogglesReadout;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class GogglesTextOverlay {
    private static final float TEXT_SCALE = 0.0125F;
    private static final float LINE_SPACING_DIVISOR = 5.5F;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private GogglesTextOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!GogglesAccess.wearsRevealingGear(mc.player)) {
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit) || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        IGogglesReadout display;
        boolean blockForm = false;
        if (mc.level.getBlockEntity(pos) instanceof IGogglesReadout be) {
            display = be;
        } else if (mc.level.getBlockState(pos).getBlock() instanceof IGogglesReadout block) {
            display = block;
            blockForm = true;
        } else {
            return;
        }
        List<Component> lines = display.readout();
        if (lines.isEmpty()) {
            return;
        }
        Vec3 offset = display.readoutAnchor();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        for (int i = 0; i < lines.size(); i++) {
            float lineShift = (i - lines.size() / 2.0F) / LINE_SPACING_DIVISOR;
            double y = pos.getY() + offset.y + (blockForm ? lineShift : -lineShift);
            drawTextInAir(event.getPoseStack(), mc, buffers, pos.getX() + offset.x, y, pos.getZ() + offset.z, lines.get(i));
        }
        buffers.endBatch();
    }

    private static void drawTextInAir(PoseStack poseStack, Minecraft mc, MultiBufferSource buffers, double x, double y, double z, Component text) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cam = camera.position();
        float yaw = (float) Math.toDegrees(Math.atan2(cam.x - (x + 0.5), cam.z - (z + 0.5)));
        poseStack.pushPose();
        poseStack.translate(x + 0.5 - cam.x, y + 0.5 - cam.y, z + 0.5 - cam.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F));
        poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        int width = mc.font.width(text);
        mc.font.drawInBatch(text, 1 - width / 2, 1.0F, TEXT_COLOR, true, poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightCoordsUtil.FULL_BRIGHT);
        poseStack.popPose();
    }
}
