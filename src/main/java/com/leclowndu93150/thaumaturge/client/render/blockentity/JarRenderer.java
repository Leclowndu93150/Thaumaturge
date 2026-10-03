package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;

public final class JarRenderer implements BlockEntityRenderer<BlockEntityJar> {
    private static final ResourceLocation BRINE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "textures/entity/jarbrine.png");
    private static final ResourceLocation LABEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "textures/entity/label.png");
    public static final ResourceLocation ANIMATED_GLOW_LOCATION =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "block/animatedglow");
    public static final Material ANIMATED_GLOW_MATERIAL =
            new Material(InventoryMenu.BLOCK_ATLAS, ANIMATED_GLOW_LOCATION);

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
    public void render(
            BlockEntityJar jar,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        int amount = jar.amount();
        boolean braced = jar.isBlocked();
        boolean hasFilter = jar.aspectFilterKey() != null;
        Direction facing = jar.facing();
        Level level = jar.getLevel();
        int aspectColor = -1;
        ResourceLocation filterTexture = null;
        int filterColor = -1;
        boolean connectedAbove = false;
        if (level != null) {
            var registry = level.registryAccess();
            if (jar.aspectKey() != null) {
                Holder<IAspect> aspect = EssentiaTransportHelper.resolve(registry, jar.aspectKey());
                if (aspect != null) {
                    aspectColor = 0xFF000000 | (aspect.value().color() & 0x00FFFFFF);
                }
            }
            if (jar.aspectFilterKey() != null) {
                Holder<IAspect> filter = EssentiaTransportHelper.resolve(registry, jar.aspectFilterKey());
                if (filter != null) {
                    filterTexture = filter.value().texture();
                    filterColor = 0xFF000000 | (filter.value().color() & 0x00FFFFFF);
                }
            }
            BlockPos above = jar.getBlockPos().above();
            connectedAbove = EssentiaFlowHandler.transport(level, above, Direction.DOWN) != null;
        }

        if (amount > 0 && aspectColor != -1) {
            submitFluid(
                    amount, jar.capacity(), aspectColor, light, ANIMATED_GLOW_MATERIAL.sprite(), poseStack, buffers);
        }
        if (hasFilter && filterTexture != null) {
            submitFilterLabel(facing, filterTexture, filterColor, jar.getBlockPos(), poseStack, buffers, light);
        }
        if (braced) {
            submitBrace(poseStack, buffers, light);
        }
        if (connectedAbove) {
            submitLidExtension(poseStack, buffers, light);
        }
    }

    public static void submitFluid(
            float amount,
            int capacity,
            int aspectColor,
            int lightCoords,
            TextureAtlasSprite sprite,
            PoseStack poseStack,
            MultiBufferSource buffers) {
        float ratio = Math.min(1.0F, amount / (float) capacity);
        float height = FLUID_BASE_Y + ratio * FLUID_MAX_HEIGHT;
        VertexConsumer wrapped = sprite.wrap(buffers.getBuffer(Sheets.translucentCullBlockSheet()));
        PoseStack.Pose pose = poseStack.last();
        fluidQuadTop(wrapped, pose, height, aspectColor, lightCoords);
        fluidQuadBottom(wrapped, pose, aspectColor, lightCoords);
        fluidQuadSide(
                wrapped,
                pose,
                FLUID_MIN,
                FLUID_MIN,
                FLUID_MIN,
                FLUID_MAX,
                height,
                aspectColor,
                lightCoords,
                -1.0F,
                0.0F,
                0.0F);
        fluidQuadSide(
                wrapped,
                pose,
                FLUID_MAX,
                FLUID_MAX,
                FLUID_MAX,
                FLUID_MIN,
                height,
                aspectColor,
                lightCoords,
                1.0F,
                0.0F,
                0.0F);
        fluidQuadSide(
                wrapped,
                pose,
                FLUID_MAX,
                FLUID_MIN,
                FLUID_MIN,
                FLUID_MIN,
                height,
                aspectColor,
                lightCoords,
                0.0F,
                0.0F,
                -1.0F);
        fluidQuadSide(
                wrapped,
                pose,
                FLUID_MIN,
                FLUID_MAX,
                FLUID_MAX,
                FLUID_MAX,
                height,
                aspectColor,
                lightCoords,
                0.0F,
                0.0F,
                1.0F);
    }

    public static void fluidQuadTop(VertexConsumer buffer, PoseStack.Pose pose, float y, int color, int light) {
        addVertex(buffer, pose, FLUID_MIN, y, FLUID_MIN, FLUID_MIN, FLUID_MIN, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MIN, y, FLUID_MAX, FLUID_MIN, FLUID_MAX, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, y, FLUID_MAX, FLUID_MAX, FLUID_MAX, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, y, FLUID_MIN, FLUID_MAX, FLUID_MIN, color, light, 0.0F, 1.0F, 0.0F);
    }

    public static void fluidQuadBottom(VertexConsumer buffer, PoseStack.Pose pose, int color, int light) {
        addVertex(
                buffer,
                pose,
                FLUID_MIN,
                FLUID_BASE_Y,
                FLUID_MIN,
                FLUID_MIN,
                FLUID_MIN,
                color,
                light,
                0.0F,
                -1.0F,
                0.0F);
        addVertex(
                buffer,
                pose,
                FLUID_MAX,
                FLUID_BASE_Y,
                FLUID_MIN,
                FLUID_MAX,
                FLUID_MIN,
                color,
                light,
                0.0F,
                -1.0F,
                0.0F);
        addVertex(
                buffer,
                pose,
                FLUID_MAX,
                FLUID_BASE_Y,
                FLUID_MAX,
                FLUID_MAX,
                FLUID_MAX,
                color,
                light,
                0.0F,
                -1.0F,
                0.0F);
        addVertex(
                buffer,
                pose,
                FLUID_MIN,
                FLUID_BASE_Y,
                FLUID_MAX,
                FLUID_MIN,
                FLUID_MAX,
                color,
                light,
                0.0F,
                -1.0F,
                0.0F);
    }

    public static void fluidQuadSide(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x0,
            float z0,
            float x1,
            float z1,
            float yTop,
            int color,
            int light,
            float nx,
            float ny,
            float nz) {
        float minV = 1 - FLUID_BASE_Y;
        float maxV = minV - yTop;
        addVertex(buffer, pose, x0, FLUID_BASE_Y, z0, FLUID_MIN, maxV, color, light, nx, ny, nz);
        addVertex(buffer, pose, x1, FLUID_BASE_Y, z1, FLUID_MAX, maxV, color, light, nx, ny, nz);
        addVertex(buffer, pose, x1, yTop, z1, FLUID_MAX, minV, color, light, nx, ny, nz);
        addVertex(buffer, pose, x0, yTop, z0, FLUID_MIN, minV, color, light, nx, ny, nz);
    }

    public static void addVertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color,
            int light,
            float nx,
            float ny,
            float nz) {
        buffer.addVertex(pose, x, y, z)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz)
                // Let the .setColor at the end otherwise the vertex consumer is not the good one
                .setColor(color);
    }

    private void submitFilterLabel(
            Direction facing,
            ResourceLocation filterTexture,
            int filterColor,
            BlockPos blockPos,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (facing.getAxis() == Direction.Axis.Z) poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(0.0, -0.1, -0.315 - 0.001);
        float rot = (blockPos.getX() + facing.ordinal()) % 4 - 2;
        poseStack.mulPose(Axis.ZN.rotationDegrees(rot));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        labelQuad(buffers.getBuffer(RenderType.entityCutout(LABEL_TEXTURE)), poseStack.last(), light);
        if (filterTexture != null) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 0.001);
            aspectIconQuad(
                    buffers.getBuffer(RenderType.entityTranslucent(filterTexture)),
                    poseStack.last(),
                    filterColor,
                    light);
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

    private void submitBrace(PoseStack poseStack, MultiBufferSource buffers, int light) {
        int color = 0xFFFFFFFF;
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(BRINE_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        float minX = 0.5F - (BRACE_MAX_XZ - BRACE_MIN_XZ) * 0.5F * BRACE_STRETCH;
        float maxX = 0.5F + (BRACE_MAX_XZ - BRACE_MIN_XZ) * 0.5F * BRACE_STRETCH;
        float minZ = minX;
        float maxZ = maxX;
        braceCuboid(buffer, pose, minX, BRACE_BOTTOM_Y, minZ, maxX, BRACE_TOP_Y, maxZ, color, light);
    }

    private void submitLidExtension(PoseStack poseStack, MultiBufferSource buffers, int light) {
        int color = 0xFFFFFFFF;
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(BRINE_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        cuboid(
                buffer,
                pose,
                LID_EXT_MIN_XZ,
                LID_EXT_BOTTOM_Y,
                LID_EXT_MIN_XZ,
                LID_EXT_MAX_XZ,
                LID_EXT_TOP_Y,
                LID_EXT_MAX_XZ,
                color,
                light);
    }

    private static void braceCuboid(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            int color,
            int light) {
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

    private static void cuboid(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            int color,
            int light) {

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
