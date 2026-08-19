package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.render.blockentity.GolemBuilderRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class GolemBuilderItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final float SCALE = 0.5F;
    private static final float MESH_CENTER_X = 0.549F;
    private static final float MESH_CENTER_Z = 0.5439F;

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(-MESH_CENTER_X, 0.0F, -MESH_CENTER_Z);
        GolemBuilderRenderer.submitParts(0, poseStack, collector, lightCoords);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        consumer.accept(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new GolemBuilderItemSpecialRenderer();
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
