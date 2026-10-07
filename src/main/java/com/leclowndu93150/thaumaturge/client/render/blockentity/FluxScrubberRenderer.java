package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockEntityFluxScrubber;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FluxScrubberRenderer implements BlockEntityRenderer<BlockEntityFluxScrubber, FluxScrubberRenderState> {
    private static final Identifier MODEL = TTIds.rl("models/mesh/flux_scrubber.ttmesh");
    private static final RenderType TIP = RenderTypes.entityCutout(TTIds.rl("textures/block/flux_scrubber.png"));
    private static final String PART_TIP = "Tip";
    private static final float BOB_RATE = 8.0F;
    private static final float BOB_AMPLITUDE = 0.075F;
    private static final int PHASE_RANGE = 1000;

    public FluxScrubberRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public FluxScrubberRenderState createRenderState() {
        return new FluxScrubberRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityFluxScrubber scrubber, FluxScrubberRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(scrubber, state, partialTicks, cameraPosition, breakProgress);
        state.facing = scrubber.getBlockState().getValue(BlockStateProperties.FACING);
        long gameTime = scrubber.getLevel() == null ? 0L : scrubber.getLevel().getGameTime();
        float time = gameTime + partialTicks + Math.floorMod(scrubber.getBlockPos().asLong(), PHASE_RANGE);
        state.bob = Mth.sin(time / BOB_RATE) * BOB_AMPLITUDE + BOB_AMPLITUDE;
    }

    @Override
    public void submit(FluxScrubberRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        poseStack.pushPose();
        LegacyFacingPose.apply(poseStack, state.facing);
        poseStack.translate(0.0F, 0.0F, -state.bob);
        for (TTMeshPart part : GolemMeshes.get(MODEL).parts()) {
            if (PART_TIP.equals(part.name())) {
                collector.submitCustomGeometry(poseStack, TIP, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, -1));
            }
        }
        poseStack.popPose();
    }
}
