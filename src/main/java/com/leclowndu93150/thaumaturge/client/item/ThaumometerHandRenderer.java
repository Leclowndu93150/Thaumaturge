package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ThaumometerHandRenderer {
    private static final float UNIT_SCALE = 0.9F;
    private static final float SWING_SCALE = 0.15F;
    private static final float EQUIP_DIP_SCALE = 0.25F;
    private static final float DRIFT_FACTOR = 0.1F;
    private static final float HAND_YAW = 92.0F;
    private static final float HAND_PITCH = 45.0F;
    private static final float HAND_ROLL = -41.0F;
    private static final float HAND_X = 0.3F;
    private static final float HAND_Y = -1.1F;
    private static final float HAND_Z = 0.45F;
    private static final float GLASS_Z = 0.056F;
    private static final float READOUT_SCALE = 0.9F;

    private ThaumometerHandRenderer() {}

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !event.getItemStack().is(TCItems.THAUMOMETER.get())) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !player.getOffhandItem().isEmpty()) {
            return;
        }
        event.setCanceled(true);
        if (IrisCompat.isSolidHandPass()) {
            return;
        }
        renderTwoHanded(event, mc, player);
    }

    private static void renderTwoHanded(RenderHandEvent event, Minecraft mc, LocalPlayer player) {
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float partial = mc.getTimer().getGameTimeDeltaPartialTick(false);
        float equip = event.getEquipProgress();
        boolean scanning = player.isUsingItem() && player.getUseItem().is(TCItems.THAUMOMETER.get());
        float swing = scanning ? 0.0F : event.getSwingProgress();
        float sqrtSwing = Mth.sqrt(swing);
        float ySwing = -0.2F * Mth.sin(swing * (float) Math.PI) * SWING_SCALE;
        float zSwing = -0.4F * Mth.sin(sqrtSwing * (float) Math.PI) * SWING_SCALE;
        poseStack.pushPose();
        poseStack.translate(0.0F, -ySwing / 2.0F, zSwing);
        poseStack.translate(0.0F, 0.04F + equip * -1.2F * EQUIP_DIP_SCALE, -0.72F);
        poseStack.scale(UNIT_SCALE, UNIT_SCALE, UNIT_SCALE);
        float pitch = Mth.lerp(partial, player.xRotO, player.getXRot());
        float yaw = Mth.lerp(partial, player.yRotO, player.getYRot());
        float xBob = Mth.lerp(partial, player.xBobO, player.xBob);
        float yBob = Mth.lerp(partial, player.yBobO, player.yBob);
        poseStack.mulPose(Axis.XP.rotationDegrees((pitch - xBob) * DRIFT_FACTOR));
        poseStack.mulPose(Axis.YP.rotationDegrees((yaw - yBob) * DRIFT_FACTOR));
        if (!player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            renderMapHand(mc, player, poseStack, buffers, light, HumanoidArm.RIGHT);
            renderMapHand(mc, player, poseStack, buffers, light, HumanoidArm.LEFT);
            poseStack.popPose();
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(sqrtSwing * (float) Math.PI) * 20.0F * SWING_SCALE));
        renderScanner(mc, player, event.getItemStack(), poseStack, buffers, light);
        poseStack.translate(0.0F, 0.0F, GLASS_Z);
        poseStack.scale(READOUT_SCALE, -READOUT_SCALE, READOUT_SCALE);
        ThaumometerLensRenderer.submitReadout(mc, player, poseStack, buffers);
        poseStack.popPose();
    }

    private static void renderScanner(
            Minecraft mc,
            AbstractClientPlayer player,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        ItemRenderHelper.render(
                stack,
                ItemDisplayContext.HEAD,
                poseStack,
                buffers,
                light,
                OverlayTexture.NO_OVERLAY,
                player.getId() + ItemDisplayContext.HEAD.ordinal());
    }

    private static void renderMapHand(
            Minecraft mc,
            AbstractClientPlayer player,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            HumanoidArm arm) {
        PlayerRenderer renderer =
                (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        poseStack.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(HAND_YAW));
        poseStack.mulPose(Axis.XP.rotationDegrees(HAND_PITCH));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * HAND_ROLL));
        poseStack.translate(invert * HAND_X, HAND_Y, HAND_Z);
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(poseStack, buffers, light, player);
        } else {
            renderer.renderLeftHand(poseStack, buffers, light, player);
        }
        poseStack.popPose();
    }
}
