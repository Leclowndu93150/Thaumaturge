package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityRedstoneRelay;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class RelayTextOverlay {
    private static final float TEXT_SCALE = 0.0125F;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final double KNOB_OFFSET = 0.25;
    private static final double TEXT_HEIGHT = 0.35;

    private RelayTextOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit) || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        if (!(mc.level.getBlockEntity(pos) instanceof BlockEntityRedstoneRelay relay)) {
            return;
        }
        Direction facing = mc.level.getBlockState(pos).getValue(DiodeBlock.FACING);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        drawTextInAir(
                event.getPoseStack(),
                mc,
                buffers,
                pos.getX() + 0.5 - facing.getStepX() * KNOB_OFFSET,
                pos.getY() + TEXT_HEIGHT,
                pos.getZ() + 0.5 - facing.getStepZ() * KNOB_OFFSET,
                Component.literal(String.valueOf(relay.getOut())));
        drawTextInAir(
                event.getPoseStack(),
                mc,
                buffers,
                pos.getX() + 0.5 + facing.getStepX() * KNOB_OFFSET,
                pos.getY() + TEXT_HEIGHT,
                pos.getZ() + 0.5 + facing.getStepZ() * KNOB_OFFSET,
                Component.literal(String.valueOf(relay.getIn())));
        buffers.endBatch();
    }

    private static void drawTextInAir(
            PoseStack poseStack,
            Minecraft mc,
            MultiBufferSource buffers,
            double x,
            double y,
            double z,
            Component text) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cam = camera.getPosition();
        float yaw = (float) Math.toDegrees(Math.atan2(cam.x - x, cam.z - z));
        poseStack.pushPose();
        poseStack.translate(x - cam.x, y - cam.y, z - cam.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F));
        poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        int width = mc.font.width(text);
        mc.font.drawInBatch(
                text,
                1 - width / 2,
                1.0F,
                TEXT_COLOR,
                true,
                poseStack.last().pose(),
                buffers,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }
}
