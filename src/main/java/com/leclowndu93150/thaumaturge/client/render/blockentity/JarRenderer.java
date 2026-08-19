package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class JarRenderer implements BlockEntityRenderer<BlockEntityJar, JarRenderState> {
    private static final Identifier BRINE_TEXTURE = Identifier.fromNamespaceAndPath("thaumaturge", "textures/entity/jarbrine.png");
    private static final Identifier LABEL_TEXTURE = Identifier.fromNamespaceAndPath("thaumaturge", "textures/entity/label.png");
    public static final Identifier ANIMATED_GLOW_LOCATION = Identifier.fromNamespaceAndPath("thaumaturge", "block/animatedglow");
    public static final SpriteId ANIMATED_GLOW_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, ANIMATED_GLOW_LOCATION);

    public static final float FLUID_MIN = 4.0F / 16.0F;
    public static final float FLUID_MAX = 12.0F / 16.0F;
    public static final float FLUID_BASE_Y = 1.0F / 16.0F;
    public static final float FLUID_MAX_HEIGHT = 10.0F / 16.0F;

    private static final float BRACE_MIN_XZ = 5F / 16.0F;
    private static final float BRACE_MAX_XZ = 11.25F / 16.0F;
    private static final float BRACE_BOTTOM_Y = 12F / 16.0F;
    private static final float BRACE_TOP_Y = 14.25F / 16.0F;
    private static final float BRACE_STRETCH = 1.001F;

    private static final float LID_EXT_MIN_XZ = (8.0F - 2.0F * 0.9F) / 16.0F;
    private static final float LID_EXT_MAX_XZ = (8.0F + 2.0F * 0.9F) / 16.0F;
    private static final float LID_EXT_BOTTOM_Y = 14.0F / 16.0F;
    private static final float LID_EXT_TOP_Y = 16.0F / 16.0F;

    public JarRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public JarRenderState createRenderState() {
        return new JarRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityJar blockEntity, JarRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.amount = blockEntity.amount();
        state.capacity = blockEntity.capacity();
        state.braced = blockEntity.isBlocked();
        state.hasFilter = blockEntity.aspectFilterKey() != null;
        state.facing = blockEntity.facing();
        Level level = blockEntity.getLevel();
        state.aspectColor = -1;
        state.filterTexture = null;
        state.filterColor = -1;
        state.filterAspect = null;
        state.connectedAbove = false;
        if (level == null)
            return;
        var registry = level.registryAccess();
        if (blockEntity.aspectKey() != null) {
            Holder<IAspect> aspect = EssentiaTransportHelper.resolve(registry, blockEntity.aspectKey());
            if (aspect != null) {
                state.aspectColor = 0xFF000000 | (aspect.value().color() & 0x00FFFFFF);
            }
        }
        if (blockEntity.aspectFilterKey() != null) {
            Holder<IAspect> filter = EssentiaTransportHelper.resolve(registry, blockEntity.aspectFilterKey());
            if (filter != null) {
                state.filterAspect = filter;
                state.filterTexture = filter.value().texture();
                state.filterColor = 0xFF000000 | (filter.value().color() & 0x00FFFFFF);
            }
        }
        BlockPos above = blockEntity.getBlockPos().above();
        state.connectedAbove = EssentiaFlowHandler.transport(level, above, Direction.DOWN) != null;
    }

    @Override
    public void submit(JarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.amount > 0 && state.aspectColor != -1) {
            submitFluid(state.amount, state.capacity, state.aspectColor, state.lightCoords, Minecraft.getInstance().getAtlasManager().get(ANIMATED_GLOW_SPRITE), poseStack, submitNodeCollector);
        }
        if (state.hasFilter && state.filterTexture != null) {
            submitFilterLabel(state, poseStack, submitNodeCollector);
        }
        if (state.braced) {
            submitBrace(state, poseStack, submitNodeCollector);
        }
        if (state.connectedAbove) {
            submitLidExtension(state, poseStack, submitNodeCollector);
        }
    }

    public static void submitFluid(float amount, int capacity, int aspectColor, int lightCoords, TextureAtlasSprite sprite, PoseStack poseStack, SubmitNodeCollector collector) {
        float ratio = Math.min(1.0F, amount / (float) capacity);
        float height = FLUID_BASE_Y + ratio * FLUID_MAX_HEIGHT;
        RenderType type = Sheets.translucentBlockItemSheet();
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            VertexConsumer wrapped = sprite.wrap(buffer);
            fluidQuadTop(wrapped, pose, height, aspectColor, lightCoords);
            fluidQuadBottom(wrapped, pose, aspectColor, lightCoords);
            fluidQuadSide(wrapped, pose, FLUID_MIN, FLUID_MIN, FLUID_MIN, FLUID_MAX, height, aspectColor, lightCoords, -1.0F, 0.0F, 0.0F);
            fluidQuadSide(wrapped, pose, FLUID_MAX, FLUID_MAX, FLUID_MAX, FLUID_MIN, height, aspectColor, lightCoords, 1.0F, 0.0F, 0.0F);
            fluidQuadSide(wrapped, pose, FLUID_MAX, FLUID_MIN, FLUID_MIN, FLUID_MIN, height, aspectColor, lightCoords, 0.0F, 0.0F, -1.0F);
            fluidQuadSide(wrapped, pose, FLUID_MIN, FLUID_MAX, FLUID_MAX, FLUID_MAX, height, aspectColor, lightCoords, 0.0F, 0.0F, 1.0F);
        });
    }

    public static void fluidQuadTop(VertexConsumer buffer, PoseStack.Pose pose, float y, int color, int light) {
        addVertex(buffer, pose, FLUID_MIN, y, FLUID_MIN, FLUID_MIN, FLUID_MIN, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MIN, y, FLUID_MAX, FLUID_MIN, FLUID_MAX, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, y, FLUID_MAX, FLUID_MAX, FLUID_MAX, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, y, FLUID_MIN, FLUID_MAX, FLUID_MIN, color, light, 0.0F, 1.0F, 0.0F);
    }

    public static void fluidQuadBottom(VertexConsumer buffer, PoseStack.Pose pose, int color, int light) {
        addVertex(buffer, pose, FLUID_MIN, FLUID_BASE_Y, FLUID_MIN, FLUID_MIN, FLUID_MIN, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, FLUID_BASE_Y, FLUID_MIN, FLUID_MAX, FLUID_MIN, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, FLUID_BASE_Y, FLUID_MAX, FLUID_MAX, FLUID_MAX, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MIN, FLUID_BASE_Y, FLUID_MAX, FLUID_MIN, FLUID_MAX, color, light, 0.0F, -1.0F, 0.0F);
    }

    public static void fluidQuadSide(VertexConsumer buffer, PoseStack.Pose pose, float x0, float z0, float x1, float z1, float yTop, int color, int light, float nx, float ny, float nz) {
        float minV = 1 - FLUID_BASE_Y;
        float maxV = minV - yTop;
        addVertex(buffer, pose, x0, FLUID_BASE_Y, z0, FLUID_MIN, maxV, color, light, nx, ny, nz);
        addVertex(buffer, pose, x1, FLUID_BASE_Y, z1, FLUID_MAX, maxV, color, light, nx, ny, nz);
        addVertex(buffer, pose, x1, yTop, z1, FLUID_MAX, minV, color, light, nx, ny, nz);
        addVertex(buffer, pose, x0, yTop, z0, FLUID_MIN, minV, color, light, nx, ny, nz);
    }

    public static void addVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color, int light, float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz)
                // Let the .setColor at the end otherwise the vertex consumer is not the good one
                .setColor(color);
    }

    private void submitFilterLabel(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        Direction facing = state.facing;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (facing.getAxis() == Direction.Axis.Z)
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(0.0, -0.1, -0.315 - 0.001);
        float rot = (state.blockPos.getX() + state.facing.ordinal()) % 4 - 2;
        poseStack.mulPose(Axis.ZN.rotationDegrees(rot));
        int light = state.lightCoords;
        Identifier labelTex = LABEL_TEXTURE;
        RenderType labelType = RenderTypes.entityCutout(labelTex);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        collector.submitCustomGeometry(poseStack, labelType, (pose, buffer) -> labelQuad(buffer, pose, light));
        if (state.filterTexture != null) {
            poseStack.pushPose();
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
        float s = 0.25F;
        int white = 0xFFFFFFFF;
        addVertex(buffer, pose, -s, -s, 0.0F, 0.0F, 1.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, -s, 0.0F, 1.0F, 1.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, s, 0.0F, 1.0F, 0.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, -s, s, 0.0F, 0.0F, 0.0F, white, light, 0.0F, 0.0F, -1.0F);
    }

    private static void aspectIconQuad(VertexConsumer buffer, PoseStack.Pose pose, int color, int light) {
        float s = 0.19F;
        addVertex(buffer, pose, -s, -s, 0.0F, 0.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, -s, 0.0F, 1.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, s, 0.0F, 1.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, -s, s, 0.0F, 0.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
    }

    private void submitBrace(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        int light = state.lightCoords;
        int color = 0xFFFFFFFF;
        RenderType type = RenderTypes.entityCutout(BRINE_TEXTURE);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            float minX = 0.5F - (BRACE_MAX_XZ - BRACE_MIN_XZ) * 0.5F * BRACE_STRETCH;
            float maxX = 0.5F + (BRACE_MAX_XZ - BRACE_MIN_XZ) * 0.5F * BRACE_STRETCH;
            float minZ = minX;
            float maxZ = maxX;
            float bottom = BRACE_BOTTOM_Y;
            float top = BRACE_TOP_Y;
            braceCuboid(buffer, pose, minX, bottom, minZ, maxX, top, maxZ, color, light);
        });
    }

    private void submitLidExtension(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        int light = state.lightCoords;
        int color = 0xFFFFFFFF;
        RenderType type = RenderTypes.entityCutout(BRINE_TEXTURE);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            cuboid(buffer, pose, LID_EXT_MIN_XZ, LID_EXT_BOTTOM_Y, LID_EXT_MIN_XZ, LID_EXT_MAX_XZ, LID_EXT_TOP_Y, LID_EXT_MAX_XZ, color, light);
        });
    }

    private static void braceCuboid(VertexConsumer buffer, PoseStack.Pose pose, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, int light) {
        addVertex(buffer, pose, minX, maxY, minZ, 38 / 64F, 24 / 32F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, minX, maxY, maxZ, 38 / 64F, 30 / 32F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, maxZ, 44 / 64F, 30 / 32F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, minZ, 44 / 64F, 24 / 32F, color, light, 0.0F, 1.0F, 0.0F);

        addVertex(buffer, pose, minX, minY, minZ, 32 / 64F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, minX, maxY, minZ, 32 / 64F, 30 / 32F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, maxX, maxY, minZ, 38 / 64F, 30 / 32F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, maxX, minY, minZ, 38 / 64F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);

        addVertex(buffer, pose, maxX, minY, maxZ, 50 / 64F, 1.0F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, maxX, maxY, maxZ, 50 / 64F, 30 / 32F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, minX, maxY, maxZ, 44 / 64F, 30 / 32F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, minX, minY, maxZ, 44 / 64F, 1.0F, color, light, 0.0F, 0.0F, 1.0F);

        addVertex(buffer, pose, minX, minY, maxZ, 38 / 64F, 1.0F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, maxY, maxZ, 38 / 64F, 30 / 32F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, maxY, minZ, 44 / 64F, 30 / 32F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, minY, minZ, 44 / 64F, 1.0F, color, light, -1.0F, 0.0F, 0.0F);

        addVertex(buffer, pose, maxX, minY, minZ, 56 / 64F, 1.0F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, minZ, 56 / 64F, 30 / 32F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, maxZ, 50 / 64F, 30 / 32F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, minY, maxZ, 50 / 64F, 1.0F, color, light, 1.0F, 0.0F, 0.0F);
    }

    private static void cuboid(VertexConsumer buffer, PoseStack.Pose pose, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, int light) {

        addVertex(buffer, pose, minX, minY, minZ, 0.0F, 29 / 32F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, minX, maxY, minZ, 0.0F, 27 / 32F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, maxX, maxY, minZ, 4 / 64F, 27 / 32F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, maxX, minY, minZ, 4 / 64F, 29 / 32F, color, light, 0.0F, 0.0F, -1.0F);

        addVertex(buffer, pose, maxX, minY, maxZ, 8 / 64F, 29 / 32F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, maxX, maxY, maxZ, 8 / 64F, 27 / 32F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, minX, maxY, maxZ, 12 / 64F, 27 / 32F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, minX, minY, maxZ, 12 / 64F, 29 / 32F, color, light, 0.0F, 0.0F, 1.0F);

        addVertex(buffer, pose, minX, minY, maxZ, 4 / 64F, 29 / 32F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, maxY, maxZ, 4 / 64F, 27 / 32F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, maxY, minZ, 8 / 64F, 27 / 32F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, minY, minZ, 8 / 64F, 29 / 32F, color, light, -1.0F, 0.0F, 0.0F);

        addVertex(buffer, pose, maxX, minY, minZ, 12 / 64F, 29 / 32F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, minZ, 12 / 64F, 27 / 32F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, maxZ, 16 / 64F, 27 / 32F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, minY, maxZ, 16 / 64F, 29 / 32F, color, light, 1.0F, 0.0F, 0.0F);
    }
}
