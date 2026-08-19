package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class DeconTableItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/decontable.png");
    private static final float BOOK_Y = 1.02F;
    private static final float BOOK_SCALE = 0.8F;
    private static final float IN_FRAME_SCALE = 0.5128205F;

    private final DeconTableModel model;

    public DeconTableItemSpecialRenderer(DeconTableModel model) {
        this.model = model;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        collector.submitModelPart(model.root, poseStack, RenderTypes.entityCutout(TEXTURE), lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.popPose();

        ItemStackRenderState book = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(book, new ItemStack(TCItems.THAUMOMETER.get()), ItemDisplayContext.FIXED, null, null, 0);
        poseStack.pushPose();
        poseStack.translate(0.5F, BOOK_Y, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.scale(BOOK_SCALE, BOOK_SCALE, BOOK_SCALE);
        poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        book.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
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
            return new DeconTableItemSpecialRenderer(new DeconTableModel(context.entityModelSet().bakeLayer(TCModelLayers.DECONSTRUCTION_TABLE)));
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
