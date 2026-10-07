package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.PechModel;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class PechRenderer extends MobRenderer<EntityPech, PechModel> {
    private static final ResourceLocation[] TEXTURES = {
        TTIds.rl("textures/entity/pech_forage.png"),
        TTIds.rl("textures/entity/pech_thaum.png"),
        TTIds.rl("textures/entity/pech_stalker.png")
    };
    private static final float SHADOW = 0.5F;
    private static final float ITEM_LIFT = -0.1F;
    private static final float ITEM_FORWARD = 0.0625F;
    private static final float BOW_SHIFT_X = -0.075F;
    private static final float BOW_SHIFT_Y = -0.1F;
    private static final float HAND_SIDE_OFFSET = 0.0625F;
    private static final float HAND_DOWN_OFFSET = 0.125F;
    private static final float HAND_OUT_OFFSET = -0.625F;

    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(TTModelLayers.PECH)), SHADOW);
    }

    @Override
    public void render(
            EntityPech entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
        boolean holdingBow = entity.getMainHandItem().is(Items.BOW);
        HumanoidArm mainArm = entity.getMainArm();
        float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        ItemStack rightItem = mainArm == HumanoidArm.RIGHT ? entity.getMainHandItem() : entity.getOffhandItem();
        ItemStack leftItem = mainArm == HumanoidArm.LEFT ? entity.getMainHandItem() : entity.getOffhandItem();
        renderHandItem(
                rightItem,
                HumanoidArm.RIGHT,
                this.model.rightArm,
                mainArm == HumanoidArm.RIGHT && holdingBow,
                bodyRot,
                poseStack,
                buffers,
                light);
        renderHandItem(
                leftItem,
                HumanoidArm.LEFT,
                this.model.leftArm,
                mainArm == HumanoidArm.LEFT && holdingBow,
                bodyRot,
                poseStack,
                buffers,
                light);
    }

    private void renderHandItem(
            ItemStack item,
            HumanoidArm arm,
            ModelPart armPart,
            boolean bow,
            float bodyRot,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        if (item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        armPart.translateAndRotate(poseStack);
        poseStack.translate(0.0F, ITEM_LIFT, ITEM_FORWARD);
        if (bow) {
            poseStack.translate(BOW_SHIFT_X, BOW_SHIFT_Y, 0.0F);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(
                arm == HumanoidArm.LEFT ? -HAND_SIDE_OFFSET : HAND_SIDE_OFFSET, HAND_DOWN_OFFSET, HAND_OUT_OFFSET);
        ItemDisplayContext context = arm == HumanoidArm.RIGHT
                ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        ItemRenderHelper.render(item, context, poseStack, buffers, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPech entity) {
        return TEXTURES[Math.min(entity.getPechType(), TEXTURES.length - 1)];
    }
}
