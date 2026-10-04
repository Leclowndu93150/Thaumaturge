package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.golem.CopperGolemRig;
import com.leclowndu93150.thaumaturge.client.golem.GolemAccessoryRenderTable;
import com.leclowndu93150.thaumaturge.client.golem.GolemRenderState;
import com.leclowndu93150.thaumaturge.client.golem.GolemRenderer;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class GolemItemSpecialRenderer implements SpecialModelRenderer<GolemProperties> {
    private final CopperGolemRig model;

    public GolemItemSpecialRenderer(BakingContext context) {
        model = new CopperGolemRig(context.entityModelSet().bakeLayer(ModelLayers.COPPER_GOLEM));
    }

    @Override
    public GolemProperties extractArgument(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.GOLEM_PROPERTIES.get(), GolemProperties.createDefault());
    }

    @Override
    public void submit(@Nullable GolemProperties properties, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outlineColor) {
        GolemRenderState state = new GolemRenderState();
        state.props = properties == null ? GolemProperties.createDefault() : properties;
        state.lightCoords = light;
        model.setupAnim(state);
        pose.pushPose();
        pose.translate(0.5F, 0.05F, 0.5F);
        pose.scale(-CopperGolemRig.SCALE, -CopperGolemRig.SCALE, CopperGolemRig.SCALE);
        pose.translate(0.0F, -1.5F, 0.0F);
        GolemRenderer.submitParts(model, GolemAccessoryRenderTable.EMPTY, state, pose, collector, false, -1);
        pose.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.18F, 0.02F, 0.15F));
        consumer.accept(new Vector3f(0.82F, 0.95F, 0.85F));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<GolemProperties> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<GolemProperties> bake(BakingContext context) {
            return new GolemItemSpecialRenderer(context);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<GolemProperties>> type() {
            return MAP_CODEC;
        }
    }
}
