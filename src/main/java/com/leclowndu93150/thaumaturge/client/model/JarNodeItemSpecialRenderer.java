package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.client.render.blockentity.NodeRenderState;
import com.leclowndu93150.thaumaturge.client.render.blockentity.NodeRenderer;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class JarNodeItemSpecialRenderer implements SpecialModelRenderer<NodeData> {
    private static final float NODE_HEIGHT = 0.4F;
    private static final float NODE_SIZE = 1.0F;
    private static final float BASE_ALPHA = 0.5F;
    private static final float TICKS_WRAP = 1000000.0F;
    private static final int PLANE_ORDER_STRIDE = 64;

    @Override
    public @Nullable NodeData extractArgument(ItemStack stack) {
        return stack.get(TCDataComponents.NODE_DATA.get());
    }

    @Override
    public void submit(@Nullable NodeData data, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (data == null || data.aspects().isEmpty()) {
            return;
        }
        NodeRenderState state = new NodeRenderState();
        state.type = data.type();
        state.modifier = data.modifier().orElse(null);
        state.visible = true;
        state.size = NODE_SIZE;
        state.ticks = (Util.getMillis() % (long) TICKS_WRAP) / 50.0F;
        state.frameSeed = 1;
        float alpha = BASE_ALPHA;
        if (state.modifier != null) {
            alpha = switch (state.modifier) {
                case BRIGHT -> alpha * 1.5F;
                case PALE -> alpha * 0.66F;
                case FADING -> alpha * (Mth.sin(state.ticks / 3.0F) * 0.25F + 0.33F);
            };
        }
        state.alpha = Math.min(alpha, 1.0F);
        for (AspectInstance entry : data.aspects().entries()) {
            NodeRenderState.AspectLayer layer = new NodeRenderState.AspectLayer();
            layer.color = entry.aspect().value().color();
            layer.amount = entry.amount();
            layer.blend = entry.aspect().value().blend();
            state.layers.add(layer);
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, NODE_HEIGHT, 0.5F);
        NodeRenderer.submitLayers(state, poseStack, collector, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        NodeRenderer.submitLayers(state, poseStack, collector, PLANE_ORDER_STRIDE);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        NodeRenderer.submitLayers(state, poseStack, collector, PLANE_ORDER_STRIDE * 2);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.1875F, 0.0625F, 0.1875F));
        consumer.accept(new Vector3f(0.8125F, 0.6875F, 0.8125F));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<NodeData> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<NodeData> bake(SpecialModelRenderer.BakingContext context) {
            return new JarNodeItemSpecialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<NodeData>> type() {
            return MAP_CODEC;
        }
    }
}
