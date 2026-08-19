package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.client.render.blockentity.JarRenderer;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.cuboid.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class JarItemSpecialRenderer implements SpecialModelRenderer<JarItemSpecialRenderer.JarFill> {

    public record JarFill(AspectInstance contents, int capacity) {
    }

    private final BakingContext context;

    public JarItemSpecialRenderer(BakingContext context) {
        this.context = context;
    }

    @Override
    public void submit(@Nullable JarFill fill, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int i1, boolean b, int i2) {
        if (fill == null)
            return;
        AspectInstance contents = fill.contents();
        JarRenderer.submitFluid(contents.amount(), fill.capacity(), contents.aspect().value().color(), lightCoords, context.sprites().get(JarRenderer.ANIMATED_GLOW_SPRITE), poseStack,
                submitNodeCollector);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        AbstractEndPortalRenderer.getExtents(consumer);
    }

    @Override
    public @Nullable JarFill extractArgument(ItemStack itemStack) {
        var essentiaList = itemStack.get(TCDataComponents.ESSENTIA_CONTENTS.get());
        if (essentiaList == null || essentiaList.isEmpty())
            return null;
        int capacity = itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IEssentiaJar jar ? jar.jarCapacity() : IEssentiaJar.DEFAULT_CAPACITY;
        return new JarFill(essentiaList.contents().entries().getFirst(), capacity);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<JarFill> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable SpecialModelRenderer<JarFill> bake(BakingContext bakingContext) {
            return new JarItemSpecialRenderer(bakingContext);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<JarFill>> type() {
            return MAP_CODEC;
        }
    }
}
