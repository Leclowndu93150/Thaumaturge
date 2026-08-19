package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class DeconstructionTableRenderer implements BlockEntityRenderer<BlockEntityDeconstructionTable, DeconstructionTableRenderState> {
    private static final Identifier TABLE_TEXTURE = TCIds.rl("textures/entity/decontable.png");

    private static final float BOOK_Y = 1.02F;
    private static final float BOOK_SCALE = 0.8F;
    private static final float INPUT_Y = 1.15F;
    private static final float ASPECT_Y = 1.081F;
    private static final float ASPECT_SCALE = 0.384F;
    private static final float ASPECT_ALPHA = 0.8F;
    private static final float IN_FRAME_SCALE = 0.5128205F;
    private static final float IN_FRAME_DROP = -0.05F;
    private static final float FLAT_ITEM_LIFT = 0.125F;

    private final DeconTableModel model;
    private final ItemModelResolver itemModelResolver;

    public DeconstructionTableRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new DeconTableModel(context.bakeLayer(TCModelLayers.DECONSTRUCTION_TABLE));
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public DeconstructionTableRenderState createRenderState() {
        return new DeconstructionTableRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityDeconstructionTable table, DeconstructionTableRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(table, state, partialTicks, cameraPosition, breakProgress);
        if (table.getLevel() == null) {
            return;
        }
        state.ticks = (table.getLevel().getGameTime() % 360L) + partialTicks;
        state.book = itemState(table, new ItemStack(TCItems.THAUMOMETER.get()));
        ItemStack input = table.items().getResource(BlockEntityDeconstructionTable.SLOT_INPUT).toStack(1);
        state.input = input.isEmpty() ? null : itemState(table, input);
        state.aspect = resolveAspect(table);
    }

    private @Nullable ItemStackRenderState itemState(BlockEntityDeconstructionTable table, ItemStack stack) {
        ItemStackRenderState itemState = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, table.getLevel(), null, 0);
        return itemState;
    }

    private static @Nullable Holder<IAspect> resolveAspect(BlockEntityDeconstructionTable table) {
        Identifier id = table.resultAspect();
        if (id == null || table.getLevel() == null) {
            return null;
        }
        return table.getLevel().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, id)).map(holder -> (Holder<IAspect>) holder).orElse(null);
    }

    @Override
    public void submit(DeconstructionTableRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        collector.submitModelPart(model.root, poseStack, RenderTypes.entityCutout(TABLE_TEXTURE), light, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.popPose();

        if (state.book != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, BOOK_Y, 0.5F);
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            poseStack.scale(BOOK_SCALE, BOOK_SCALE, BOOK_SCALE);
            poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            state.book.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.input != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, INPUT_Y, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks % 360.0F));
            poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
            poseStack.translate(0.0F, IN_FRAME_DROP + FLAT_ITEM_LIFT, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            state.input.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.aspect != null) {
            Holder<IAspect> aspect = state.aspect;
            poseStack.pushPose();
            poseStack.translate(0.5F, ASPECT_Y, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.ticks % 360.0F));
            poseStack.scale(ASPECT_SCALE, ASPECT_SCALE, ASPECT_SCALE);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(aspect.value().texture()),
                    (pose, buffer) -> AspectTagWorldRenderer.renderQuad(pose, buffer, aspect, ASPECT_ALPHA, false, light));
            poseStack.popPose();
        }
    }
}
