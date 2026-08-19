package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.ToIntFunction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EldritchCapRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, EldritchCapRenderState> {
    public static final Identifier CAP_TEXTURE = TCIds.rl("textures/entity/obelisk_cap.png");
    public static final Identifier CAP_TEXTURE_OUTER = TCIds.rl("textures/entity/obelisk_cap_2.png");
    public static final Identifier ALTAR_TEXTURE = TCIds.rl("textures/entity/obelisk_cap_altar.png");

    private static final float EYE_OFFSET = 0.46F;
    private static final float EYE_HEIGHT = 0.2F;
    private static final float EYE_TILT = 18.0F;
    private static final float FLAT_ITEM_LIFT = 0.125F;
    private static final float IN_FRAME_SCALE = 0.5128205F;
    private static final float IN_FRAME_DROP = -0.05F;

    private final Identifier texture;
    private final Identifier textureOuter;
    private final ToIntFunction<T> eyeCount;
    private final ItemModelResolver itemModelResolver;
    private ItemStack eyeStack = ItemStack.EMPTY;

    public EldritchCapRenderer(BlockEntityRendererProvider.Context context, Identifier texture, Identifier textureOuter, ToIntFunction<T> eyeCount) {
        this.texture = texture;
        this.textureOuter = textureOuter;
        this.eyeCount = eyeCount;
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public EldritchCapRenderState createRenderState() {
        return new EldritchCapRenderState();
    }

    @Override
    public void extractRenderState(T cap, EldritchCapRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(cap, state, partialTicks, cameraPosition, breakProgress);
        state.eyes = eyeCount.applyAsInt(cap);
        state.outerLands = cap.getLevel() != null && cap.getLevel().dimension() == OuterLands.DIMENSION;
        if (state.eyes > 0) {
            if (eyeStack.isEmpty()) {
                eyeStack = new ItemStack(TCItems.ELDRITCH_EYE.get());
            }
            ItemStackRenderState itemState = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(itemState, eyeStack, ItemDisplayContext.FIXED, cap.getLevel(), null, 0);
            state.eye = itemState;
        } else {
            state.eye = null;
        }
    }

    @Override
    public void submit(EldritchCapRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RenderType type = RenderTypes.entityTranslucent(state.outerLands ? textureOuter : texture);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        EldritchObeliskRenderer.submitCap(poseStack, collector, type, state.lightCoords);
        poseStack.popPose();
        if (state.eye == null) {
            return;
        }
        for (int a = 0; a < state.eyes; a++) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(a * 90.0F));
            poseStack.translate(EYE_OFFSET, EYE_HEIGHT, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.XN.rotationDegrees(EYE_TILT));
            poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
            poseStack.translate(0.0F, IN_FRAME_DROP + FLAT_ITEM_LIFT, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            state.eye.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
