package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class JarBrainItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final Identifier TEX_BRAIN = TCIds.rl("textures/entity/brain2.png");
    private static final Identifier TEX_BRINE = TCIds.rl("textures/entity/jarbrine.png");
    private static final float BRAIN_SCALE = 0.4F;
    private static final float BRAIN_LIFT = -0.77F;

    private final BrainModel brain;
    private final JarBrineModel brine;

    public JarBrainItemSpecialRenderer(BrainModel brain, JarBrineModel brine) {
        this.brain = brain;
        this.brine = brine;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.01F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        poseStack.pushPose();
        poseStack.translate(0.0F, BRAIN_LIFT, 0.0F);
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        poseStack.scale(BRAIN_SCALE, BRAIN_SCALE, BRAIN_SCALE);
        collector.submitModelPart(brain.root, poseStack, RenderTypes.entityCutout(TEX_BRAIN), lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.popPose();

        collector.submitModelPart(brine.root, poseStack, RenderTypes.entityTranslucent(TEX_BRINE), lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.1875F, 0.0625F, 0.1875F));
        consumer.accept(new Vector3f(0.8125F, 0.6875F, 0.8125F));
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new JarBrainItemSpecialRenderer(new BrainModel(context.entityModelSet().bakeLayer(TCModelLayers.BRAIN)),
                    new JarBrineModel(context.entityModelSet().bakeLayer(TCModelLayers.JAR_BRINE)));
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
