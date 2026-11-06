package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.TTShaders;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class FluxRiftRenderer extends EntityRenderer<EntityFluxRift> {
    private static final ResourceLocation END_PORTAL = TheEndPortalRenderer.END_PORTAL_LOCATION;

    private static final RenderType RIFT_GLOW_TYPE = riftType(
            "tt_rift_glow",
            RenderStateShard.ADDITIVE_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE);
    private static final RenderType RIFT_GLOW_NO_DEPTH_TYPE = riftType(
            "tt_rift_glow_no_depth",
            RenderStateShard.ADDITIVE_TRANSPARENCY,
            RenderStateShard.NO_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE);
    private static final RenderType RIFT_CORE_TYPE = riftCoreType();

    private static final int GLOW_PASSES = 3;
    private static final float[] GLOW_SCALES = {2.2F, 1.75F, 1.4F};

    public FluxRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    private static RenderType riftType(
            String name,
            RenderStateShard.TransparencyStateShard transparency,
            RenderStateShard.DepthTestStateShard depthTest,
            RenderStateShard.WriteMaskStateShard writeMask) {
        return RenderType.create(
                name,
                DefaultVertexFormat.POSITION,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(TTShaders::ender))
                        .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                                .add(END_PORTAL, false, false)
                                .add(END_PORTAL, false, false)
                                .build())
                        .setTransparencyState(transparency)
                        .setDepthTestState(depthTest)
                        .setWriteMaskState(writeMask)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false));
    }

    private static RenderType riftCoreType() {
        return RenderType.create(
                "tt_rift_core",
                DefaultVertexFormat.POSITION,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(TTShaders::ender))
                        .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                                .add(END_PORTAL, false, false)
                                .add(END_PORTAL, false, false)
                                .build())
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false));
    }

    static RenderType renderTypeForPass(int pass, boolean goggles) {
        if (pass >= GLOW_PASSES) {
            return RIFT_CORE_TYPE;
        }
        return pass == GLOW_PASSES - 1 && goggles ? RIFT_GLOW_NO_DEPTH_TYPE : RIFT_GLOW_TYPE;
    }

    @Override
    public void render(
            EntityFluxRift entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        List<Vec3> outline = entity.outline;
        List<Float> widths = entity.outlineWidths;
        int count = Math.min(outline.size(), widths.size());
        if (count < 2) {
            return;
        }
        FluxRiftGeometry.Tube tube =
                FluxRiftGeometry.build(outline, widths, entity.stabilityValue(), entity.tickCount + partialTicks);
        boolean goggles = Minecraft.getInstance().player != null
                && GogglesAccess.wearsRevealingGear(Minecraft.getInstance().player);
        for (int pass = 0; pass < GLOW_PASSES; pass++) {
            float scale = GLOW_SCALES[pass];
            LateWorldRenderQueue.drawOwnShader(
                    poseStack,
                    buffers,
                    renderTypeForPass(pass, goggles),
                    (pose, buffer) -> renderTube(buffer, pose.pose(), tube, scale));
        }
        LateWorldRenderQueue.drawOwnShader(
                poseStack,
                buffers,
                renderTypeForPass(GLOW_PASSES, goggles),
                (pose, buffer) -> renderTube(buffer, pose.pose(), tube, 1.0F));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFluxRift entity) {
        return END_PORTAL;
    }

    private static void renderTube(VertexConsumer buffer, Matrix4f pose, FluxRiftGeometry.Tube tube, float scale) {
        for (int point = 0; point < tube.count() - 1; point++) {
            for (int side = 0; side < 6; side++) {
                int next = (side + 1) % 6;
                addVertex(buffer, pose, tube, point, side, scale);
                addVertex(buffer, pose, tube, point, next, scale);
                addVertex(buffer, pose, tube, point + 1, next, scale);
                addVertex(buffer, pose, tube, point + 1, side, scale);
            }
        }
    }

    private static void addVertex(
            VertexConsumer buffer, Matrix4f pose, FluxRiftGeometry.Tube tube, int point, int side, float scale) {
        buffer.addVertex(
                pose,
                tube.vertexX(point, side, scale),
                tube.vertexY(point, side, scale),
                tube.vertexZ(point, side, scale));
    }
}
