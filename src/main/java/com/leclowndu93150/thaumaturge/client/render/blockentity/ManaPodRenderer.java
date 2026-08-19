package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ManaPodModel;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ManaPodRenderer implements BlockEntityRenderer<BlockEntityManaPod, ManaPodRenderState> {
    private static final Identifier CORE_TEXTURE = TCIds.rl("textures/entity/manapod_0.png");
    private static final Identifier SHELL_TEXTURE = TCIds.rl("textures/entity/manapod_2.png");

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
        this.model = new ManaPodModel(context.bakeLayer(TCModelLayers.MANA_POD));
    }

    @Override
    public ManaPodRenderState createRenderState() {
        return new ManaPodRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityManaPod pod, ManaPodRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pod, state, partialTicks, cameraPosition, breakProgress);
        state.age = pod.getBlockState().getValue(BlockManaPod.AGE);
        Holder<IAspect> aspect = pod.aspect();
        state.hasAspect = aspect != null;
        state.aspectColor = aspect == null ? -1 : aspect.value().color();
    }

    @Override
    public void submit(ManaPodRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.age < SHELL_MIN_AGE) {
            return;
        }
        float r = HERBA_R;
        float g = HERBA_G;
        float b = HERBA_B;
        if (state.hasAspect) {
            float progress = Mth.clamp((state.age - SHELL_MIN_AGE) / (float) (BlockEntityManaPod.MAX_AGE - SHELL_MIN_AGE), 0.0F, 1.0F);
            r = Mth.lerp(progress, HERBA_R, ARGB.red(state.aspectColor) / 255.0F);
            g = Mth.lerp(progress, HERBA_G, ARGB.green(state.aspectColor) / 255.0F);
            b = Mth.lerp(progress, HERBA_B, ARGB.blue(state.aspectColor) / 255.0F);
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, ANCHOR_Y, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        if (state.age > CORE_MIN_AGE - 1) {
            poseStack.pushPose();
            poseStack.translate(0.0F, CORE_LIFT, 0.0F);
            collector.submitModelPart(model.core, poseStack, RenderTypes.entityCutout(CORE_TEXTURE), FULLBRIGHT, OverlayTexture.NO_OVERLAY, null, -1, null);
            poseStack.popPose();
        }
        int shellColor = ARGB.colorFromFloat(SHELL_ALPHA, r, g, b);
        collector.submitModelPart(model.shell, poseStack, RenderTypes.entityTranslucent(SHELL_TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null, shellColor, null);
        poseStack.popPose();
    }
}
