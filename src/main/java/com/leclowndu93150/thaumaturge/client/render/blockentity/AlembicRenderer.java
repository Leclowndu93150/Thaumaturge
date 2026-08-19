package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AlembicRenderer implements BlockEntityRenderer<BlockEntityAlembic, AlembicRenderState> {

    private static final Identifier LABEL_TEXTURE = Identifier.fromNamespaceAndPath("thaumaturge", "textures/entity/label.png");

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public AlembicRenderState createRenderState() {
        return new AlembicRenderState();
    }

    @Override
    public void submit(AlembicRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.hasFilter)
            submitFilterLabel(state, poseStack, submitNodeCollector);
    }

    @Override
    public void extractRenderState(BlockEntityAlembic blockEntity, AlembicRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.filterTexture = null;
        state.filterColor = -1;
        state.filterAspect = null;
        Level level = blockEntity.getLevel();
        if (level == null)
            return;
        var registry = level.registryAccess();
        state.hasFilter = blockEntity.aspectFilterKey() != null;
        if (blockEntity.aspectFilterKey() != null) {
            Holder<IAspect> filter = EssentiaTransportHelper.resolve(registry, blockEntity.aspectFilterKey());
            if (filter != null) {
                state.filterAspect = filter;
                state.filterTexture = filter.value().texture();
                state.filterColor = 0xFF000000 | (filter.value().color() & 0x00FFFFFF);
                state.facing = blockEntity.facing();
            }
        }
        List<Direction> dirs = new ArrayList<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            IEssentiaTransport connected = level.getCapability(EssentiaCapabilities.TRANSPORT, blockEntity.getBlockPos().relative(dir), dir.getOpposite());
            if (connected != null && connected.isConnectable(dir.getOpposite())) {
                dirs.add(dir);
            }
        }
        state.connectedDirections = dirs.toArray(new Direction[0]);
    }

    public static void addVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color, int light, float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz)
                // Let the .setColor at the end otherwise the vertex consumer is not the good one
                .setColor(color);
    }

    private void submitFilterLabel(AlembicRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        Direction facing = state.facing;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (facing.getAxis() == Direction.Axis.Z)
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(0.0, 0.0, -0.376f);
        int light = state.lightCoords;
        Identifier labelTex = LABEL_TEXTURE;
        RenderType labelType = RenderTypes.entityCutout(labelTex);
        collector.submitCustomGeometry(poseStack, labelType, (pose, buffer) -> labelQuad(buffer, pose, light));
        if (state.filterTexture != null) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(0.0, 0.0, 0.001);
            int filterColor = state.filterColor;
            Identifier aspectTex = state.filterTexture;
            RenderType aspectType = RenderTypes.entityTranslucent(aspectTex);
            collector.submitCustomGeometry(poseStack, aspectType, (pose, buffer) -> aspectIconQuad(buffer, pose, filterColor, light));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void labelQuad(VertexConsumer buffer, PoseStack.Pose pose, int light) {
        float s = 0.18F;
        int white = 0xFFFFFFFF;
        addVertex(buffer, pose, -s, -s, 0.0F, 0.0F, 1.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, -s, 0.0F, 1.0F, 1.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, s, 0.0F, 1.0F, 0.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, -s, s, 0.0F, 0.0F, 0.0F, white, light, 0.0F, 0.0F, -1.0F);
    }

    private static void aspectIconQuad(VertexConsumer buffer, PoseStack.Pose pose, int color, int light) {
        float s = 0.12F;
        addVertex(buffer, pose, -s, -s, 0.0F, 0.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, -s, 0.0F, 1.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, s, 0.0F, 1.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, -s, s, 0.0F, 0.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
    }
}
