package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.PechModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;

public final class PechRenderer extends MobRenderer<EntityPech, PechRenderState, PechModel> {
    private static final Identifier[] TEXTURES = {TCIds.rl("textures/entity/pech_forage.png"), TCIds.rl("textures/entity/pech_thaum.png"), TCIds.rl("textures/entity/pech_stalker.png")};
    private static final float SHADOW = 0.5F;
    private static final float ITEM_LIFT = -0.1F;
    private static final float ITEM_FORWARD = 0.0625F;
    private static final float BOW_SHIFT_X = -0.075F;
    private static final float BOW_SHIFT_Y = -0.1F;
    private static final float HAND_SIDE_OFFSET = 0.0625F;
    private static final float HAND_DOWN_OFFSET = 0.125F;
    private static final float HAND_OUT_OFFSET = -0.625F;

    private final ItemModelResolver itemModelResolver;

    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(TCModelLayers.PECH)), SHADOW);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public PechRenderState createRenderState() {
        return new PechRenderState();
    }

    @Override
    public void extractRenderState(EntityPech entity, PechRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
        state.pechType = entity.getPechType();
        state.mumble = entity.mumble;
        state.holdingBow = entity.getMainHandItem().is(Items.BOW);
    }

    @Override
    public void submit(PechRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        submitHandItem(state, state.rightHandItemState, HumanoidArm.RIGHT, this.model.rightArm, state.mainArm == HumanoidArm.RIGHT && state.holdingBow, poseStack, collector);
        submitHandItem(state, state.leftHandItemState, HumanoidArm.LEFT, this.model.leftArm, state.mainArm == HumanoidArm.LEFT && state.holdingBow, poseStack, collector);
    }

    private void submitHandItem(PechRenderState state, ItemStackRenderState item, HumanoidArm arm, ModelPart armPart, boolean bow, PoseStack poseStack, SubmitNodeCollector collector) {
        if (item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        this.model.setupAnim(state);
        armPart.translateAndRotate(poseStack);
        poseStack.translate(0.0F, ITEM_LIFT, ITEM_FORWARD);
        if (bow) {
            poseStack.translate(BOW_SHIFT_X, BOW_SHIFT_Y, 0.0F);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(arm == HumanoidArm.LEFT ? -HAND_SIDE_OFFSET : HAND_SIDE_OFFSET, HAND_DOWN_OFFSET, HAND_OUT_OFFSET);
        item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(PechRenderState state) {
        return TEXTURES[Math.min(state.pechType, TEXTURES.length - 1)];
    }
}
