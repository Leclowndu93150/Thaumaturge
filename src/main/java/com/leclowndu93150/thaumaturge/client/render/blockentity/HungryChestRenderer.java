package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityHungryChest;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class HungryChestRenderer implements BlockEntityRenderer<BlockEntityHungryChest, HungryChestRenderState> {
    private static final SpriteId SPRITE = Sheets.CHEST_MAPPER.apply(TCIds.rl("hungry"));

    private final SpriteGetter sprites;
    private final ChestModel model;

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.model = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
    }

    @Override
    public HungryChestRenderState createRenderState() {
        return new HungryChestRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityHungryChest chest, HungryChestRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(chest, state, partialTicks, cameraPosition, breakProgress);
        state.facing = chest.getBlockState().getValue(ChestBlock.FACING);
        state.open = chest.getOpenNess(partialTicks);
    }

    @Override
    public void submit(HungryChestRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(ChestRenderer.modelTransformation(state.facing));
        float open = 1.0F - state.open;
        open = 1.0F - open * open * open;
        collector.submitModel(model, open, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, SPRITE, sprites, 0, state.breakProgress);
        poseStack.popPose();
    }
}
