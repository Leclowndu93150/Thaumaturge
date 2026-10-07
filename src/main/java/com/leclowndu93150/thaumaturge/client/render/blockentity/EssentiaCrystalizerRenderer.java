package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEntityEssentiaCrystalizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public final class EssentiaCrystalizerRenderer implements BlockEntityRenderer<BlockEntityEssentiaCrystalizer, EssentiaCrystalizerRenderState> {
    public static final Identifier CRYSTAL_MODEL_ID = TTIds.rl("block/crystalizer_crystal");
    public static final StandaloneModelKey<BlockStateModel> CRYSTAL_MODEL = new StandaloneModelKey<>(CRYSTAL_MODEL_ID::toString);
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float CRADLE_TOP = 11.0F * PIXEL;
    private static final float SIDE_CRYSTAL_SCALE = 0.7F;
    private static final List<CrystalPlacement> CRYSTALS = List.of(new CrystalPlacement(0.0F, 0.0F, 0.0F, 1.0F), new CrystalPlacement(-3.0F, -2.0F, 120.0F, SIDE_CRYSTAL_SCALE),
            new CrystalPlacement(2.0F, 3.0F, 240.0F, SIDE_CRYSTAL_SCALE));
    private static final int MIN_BLOCK_LIGHT = 12;

    public EssentiaCrystalizerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public EssentiaCrystalizerRenderState createRenderState() {
        return new EssentiaCrystalizerRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEssentiaCrystalizer crystalizer, EssentiaCrystalizerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(crystalizer, state, partialTicks, cameraPosition, breakProgress);
        state.facing = crystalizer.getBlockState().getValue(BlockStateProperties.FACING);
        state.active = crystalizer.aspectKey() != null;
        state.spin = crystalizer.rotation() + crystalizer.rotationSpeed() * partialTicks;
        state.tints[0] = ARGB.colorFromFloat(1.0F, crystalizer.crystalRed(), crystalizer.crystalGreen(), crystalizer.crystalBlue());
        state.crystalParts.clear();
        BlockStateModel model = Minecraft.getInstance().getModelManager().getStandaloneModel(CRYSTAL_MODEL);
        if (state.active && model != null && crystalizer.getLevel() instanceof ClientLevel clientLevel) {
            model.collectParts(clientLevel, crystalizer.getBlockPos(), crystalizer.getBlockState(), clientLevel.getRandom(), state.crystalParts);
        }
    }

    @Override
    public void submit(EssentiaCrystalizerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.active || state.crystalParts.isEmpty()) {
            return;
        }
        int light = LightCoordsUtil.pack(Math.max(LightCoordsUtil.block(state.lightCoords), MIN_BLOCK_LIGHT), LightCoordsUtil.sky(state.lightCoords));
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        BlockFacingPose.downBased(poseStack, state.facing);
        poseStack.translate(0.0F, CRADLE_TOP - 0.5F, 0.0F);
        for (CrystalPlacement crystal : CRYSTALS) {
            poseStack.pushPose();
            poseStack.translate(crystal.x() * PIXEL, 0.0F, crystal.z() * PIXEL);
            poseStack.mulPose(Axis.YP.rotationDegrees(crystal.turn() + state.spin));
            poseStack.scale(crystal.scale(), crystal.scale(), crystal.scale());
            poseStack.translate(-0.5F, 0.0F, -0.5F);
            collector.submitMultiLayerBlockModel(poseStack, state.crystalParts, true, state.tints, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private record CrystalPlacement(float x, float z, float turn, float scale) {
    }
}
