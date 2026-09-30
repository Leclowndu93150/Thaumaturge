package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.ToIntFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EldritchCapRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public static final ResourceLocation CAP_TEXTURE = TCIds.rl("textures/entity/obelisk_cap.png");
    public static final ResourceLocation CAP_TEXTURE_OUTER = TCIds.rl("textures/entity/obelisk_cap_2.png");
    public static final ResourceLocation ALTAR_TEXTURE = TCIds.rl("textures/entity/obelisk_cap_altar.png");

    private static final float EYE_OFFSET = 0.46F;
    private static final float EYE_HEIGHT = 0.2F;
    private static final float EYE_TILT = 18.0F;
    private static final float FLAT_ITEM_LIFT = 0.125F;
    private static final float IN_FRAME_SCALE = 0.5128205F;
    private static final float IN_FRAME_DROP = -0.05F;

    private final ResourceLocation texture;
    private final ResourceLocation textureOuter;
    private final ToIntFunction<T> eyeCount;
    private ItemStack eyeStack = ItemStack.EMPTY;

    public EldritchCapRenderer(
            BlockEntityRendererProvider.Context context,
            ResourceLocation texture,
            ResourceLocation textureOuter,
            ToIntFunction<T> eyeCount) {
        this.texture = texture;
        this.textureOuter = textureOuter;
        this.eyeCount = eyeCount;
    }

    @Override
    public void render(
            T cap, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        boolean outerLands = cap.getLevel() != null && cap.getLevel().dimension() == OuterLands.DIMENSION;
        RenderType type = RenderType.entityCutout(outerLands ? textureOuter : texture);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        EldritchObeliskRenderer.renderCap(poseStack, buffers, type, light);
        poseStack.popPose();

        int eyes = eyeCount.applyAsInt(cap);
        if (eyes <= 0) {
            return;
        }
        if (eyeStack.isEmpty()) {
            eyeStack = new ItemStack(TCItems.ELDRITCH_EYE.get());
        }
        for (int a = 0; a < eyes; a++) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(a * 90.0F));
            poseStack.translate(EYE_OFFSET, EYE_HEIGHT, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.XN.rotationDegrees(EYE_TILT));
            poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
            poseStack.translate(0.0F, IN_FRAME_DROP + FLAT_ITEM_LIFT, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            ItemRenderHelper.render(eyeStack, ItemDisplayContext.FIXED, poseStack, buffers, light, overlay, 0);
            poseStack.popPose();
        }
    }
}
