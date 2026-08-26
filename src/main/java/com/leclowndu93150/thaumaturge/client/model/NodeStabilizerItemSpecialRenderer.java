package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.render.blockentity.NodeStabilizerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class NodeStabilizerItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private final boolean advanced;
    private final boolean transducer;

    public NodeStabilizerItemSpecialRenderer(boolean advanced, boolean transducer) {
        this.advanced = advanced;
        this.transducer = transducer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        if (transducer) {
            poseStack.translate(0.5F, 1.0F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            NodeStabilizerRenderer.submitTransducerParts(0, 0, 0.0F, poseStack, collector, lightCoords);
        } else {
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            NodeStabilizerRenderer.submitParts(0, advanced, 0.0F, poseStack, collector, lightCoords);
        }
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        consumer.accept(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    public record Unbaked(boolean advanced, boolean transducer) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
                .group(Codec.BOOL.fieldOf("advanced").forGetter(Unbaked::advanced), Codec.BOOL.optionalFieldOf("transducer", false).forGetter(Unbaked::transducer)).apply(instance, Unbaked::new));

        public Unbaked(boolean advanced) {
            this(advanced, false);
        }

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new NodeStabilizerItemSpecialRenderer(advanced, transducer);
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
