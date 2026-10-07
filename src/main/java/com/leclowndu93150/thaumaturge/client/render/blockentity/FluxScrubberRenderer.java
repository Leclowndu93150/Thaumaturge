package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockEntityFluxScrubber;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class FluxScrubberRenderer implements BlockEntityRenderer<BlockEntityFluxScrubber> {
    private static final ResourceLocation MODEL = TTIds.rl("models/mesh/flux_scrubber.ttmesh");
    private static final RenderType TIP = RenderType.entityCutout(TTIds.rl("textures/block/flux_scrubber.png"));

    public FluxScrubberRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityFluxScrubber scrubber,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        long gameTime = scrubber.getLevel() == null ? 0 : scrubber.getLevel().getGameTime();
        float time =
                gameTime + partialTick + Math.floorMod(scrubber.getBlockPos().asLong(), 1000);
        float bob = Mth.sin(time / 8.0F) * 0.075F + 0.075F;
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        BlockFacingPose.northBased(pose, scrubber.getBlockState().getValue(BlockStateProperties.FACING));
        pose.translate(0, 0, -0.5F - bob);
        for (TTMeshPart part : GolemMeshes.get(MODEL).parts())
            if (part.name().equals("Tip")) GolemMeshes.renderPart(part, pose.last(), buffers.getBuffer(TIP), light, -1);
        pose.popPose();
    }
}
