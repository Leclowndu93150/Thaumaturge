package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ManaPodModel;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;

public final class ManaPodRenderer implements BlockEntityRenderer<BlockEntityManaPod> {
    private static final ResourceLocation CORE_TEXTURE = TTIds.rl("textures/entity/manapod_0.png");
    private static final ResourceLocation SHELL_TEXTURE = TTIds.rl("textures/entity/manapod_2.png");

    private static final int SHELL_MIN_AGE = 2;
    private static final int CORE_MIN_AGE = 3;
    private static final float HERBA_R = 0.14509805F;
    private static final float HERBA_G = 0.6156863F;
    private static final float HERBA_B = 0.45882353F;
    private static final float SHELL_ALPHA = 0.9F;
    private static final float CORE_LIFT = 0.1F;
    private static final float ANCHOR_Y = 0.75F;
    private static final int FULLBRIGHT = 0xF000F0;

    private final ManaPodModel model;

    public ManaPodRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ManaPodModel(context.bakeLayer(TTModelLayers.MANA_POD));
    }

    @Override
    public void render(
            BlockEntityManaPod pod,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        int age = pod.getBlockState().getValue(BlockManaPod.AGE);
        if (age < SHELL_MIN_AGE) {
            return;
        }
        Holder<IAspect> aspect = pod.aspect();
        float r = HERBA_R;
        float g = HERBA_G;
        float b = HERBA_B;
        if (aspect != null) {
            int aspectColor = aspect.value().color();
            float progress =
                    Mth.clamp((age - SHELL_MIN_AGE) / (float) (BlockEntityManaPod.MAX_AGE - SHELL_MIN_AGE), 0.0F, 1.0F);
            r = Mth.lerp(progress, HERBA_R, ARGB32.red(aspectColor) / 255.0F);
            g = Mth.lerp(progress, HERBA_G, ARGB32.green(aspectColor) / 255.0F);
            b = Mth.lerp(progress, HERBA_B, ARGB32.blue(aspectColor) / 255.0F);
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, ANCHOR_Y, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        if (age > CORE_MIN_AGE - 1) {
            poseStack.pushPose();
            poseStack.translate(0.0F, CORE_LIFT, 0.0F);
            model.core.render(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutout(CORE_TEXTURE)),
                    FULLBRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    -1);
            poseStack.popPose();
        }
        int shellColor = ARGB32.colorFromFloat(SHELL_ALPHA, r, g, b);
        model.shell.render(
                poseStack,
                buffers.getBuffer(RenderType.entityTranslucent(SHELL_TEXTURE)),
                light,
                OverlayTexture.NO_OVERLAY,
                shellColor);
        poseStack.popPose();
    }
}
