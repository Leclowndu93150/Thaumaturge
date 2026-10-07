package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.MatrixCubeModel;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.joml.Matrix4f;

public final class InfusionMatrixRenderer implements BlockEntityRenderer<BlockEntityInfusionMatrix> {
    private static final ResourceLocation TEX_NORMAL = TTIds.rl("textures/block/infuser_normal.png");
    private static final ResourceLocation TEX_ANCIENT = TTIds.rl("textures/block/infuser_ancient.png");
    private static final ResourceLocation TEX_ELDRITCH = TTIds.rl("textures/block/infuser_eldritch.png");

    private static final float MATRIX_SCALE = 0.8F;
    private static final float CUBE_OFFSET = 5.0F / 16.0F;
    private static final float CUBE_SCALE = 0.5F;
    private static final float CORE_SCALE = 12.0F / 16.0F;
    private static final float ACTIVE_LIFT = 0.3F;
    private static final int IDLE_BOB_PERIOD = 40;
    private static final float IDLE_BOB_DIVISOR = 4.5F;
    private static final int CRAFT_BOB_PERIOD = 25;
    private static final float CRAFT_BOB_DIVISOR = 3.5F;
    private static final float BOB_BLEND_TICKS = 15.0F;
    private static final float SPIN_Z_DIVISOR = 4.0F;
    private static final float TILT = 45.0F;
    private static final float JITTER_SCALE = 0.01F;
    private static final float GLOW_RED = 0.8F;
    private static final float GLOW_GREEN = 0.1F;
    private static final float GLOW_BLUE = 1.0F;
    private static final long HALO_SEED = 245L;
    private static final int HALO_FANS_FANCY = 20;
    private static final int HALO_FANS_FAST = 10;
    private static final float HALO_FADE_TICKS = 500.0F;
    private static final float HALO_RAMP_TICKS = 50.0F;

    private static final RenderType GLOW_NORMAL = TTRenderTypes.entityAdditiveEmissive(TEX_NORMAL);
    private static final RenderType GLOW_ANCIENT = TTRenderTypes.entityAdditiveEmissive(TEX_ANCIENT);
    private static final RenderType GLOW_ELDRITCH = TTRenderTypes.entityAdditiveEmissive(TEX_ELDRITCH);
    private static final RenderType HALO_TYPE = TTRenderTypes.SPARKLE_CULLED;

    private final MatrixCubeModel model;
    private final RandomSource haloRandom = RandomSource.create();

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MatrixCubeModel(context.bakeLayer(TTModelLayers.MATRIX_CUBE));
    }

    @Override
    public void render(
            BlockEntityInfusionMatrix matrix,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        float animationTime = viewEntity == null ? partialTick : viewEntity.tickCount + partialTick;
        float startUp = matrix.clientStartUp;
        float stability = matrix.stability();
        int craftTicks = matrix.clientCraftTicks;
        boolean active = matrix.isActive();
        boolean crafting = matrix.isCrafting();
        boolean fancyGraphics = Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST;
        ResourceLocation texture = pickTexture(matrix);

        RenderType type = RenderType.entityCutout(texture);
        RenderType glowType = glowTypeFor(texture);
        float instability = Math.min(
                6.0F, 1.0F + (stability < 0.0F ? -stability * 0.66F : 1.0F) * (Math.min(craftTicks, 50) / 50.0F));
        poseStack.pushPose();
        float craftBlend = Math.min(craftTicks, BOB_BLEND_TICKS) / BOB_BLEND_TICKS;
        float bob = Mth.lerp(
                craftBlend,
                oscillate(animationTime, IDLE_BOB_PERIOD) / IDLE_BOB_DIVISOR,
                oscillate(animationTime, CRAFT_BOB_PERIOD) / CRAFT_BOB_DIVISOR);
        poseStack.translate(0.5F, 0.5F + (ACTIVE_LIFT + bob) * startUp, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(animationTime % 360.0F * startUp));
        poseStack.mulPose(Axis.ZP.rotationDegrees(animationTime / SPIN_Z_DIVISOR % 360.0F * startUp));
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT * startUp));
        poseStack.mulPose(Axis.ZP.rotationDegrees(TILT * startUp));
        poseStack.scale(MATRIX_SCALE, MATRIX_SCALE, MATRIX_SCALE);
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {
                    float jx = 0.0F;
                    float jy = 0.0F;
                    float jz = 0.0F;
                    if (active) {
                        jx = Mth.sin((animationTime + a * 10) / 15.0F) * JITTER_SCALE * startUp * instability;
                        jy = Mth.sin((animationTime + b * 10) / 14.0F) * JITTER_SCALE * startUp * instability;
                        jz = Mth.sin((animationTime + c * 10) / 13.0F) * JITTER_SCALE * startUp * instability;
                    }
                    int aa = a == 0 ? -1 : 1;
                    int bb = b == 0 ? -1 : 1;
                    int cc = c == 0 ? -1 : 1;
                    poseStack.pushPose();
                    poseStack.translate(jx + aa * CUBE_OFFSET, jy + bb * CUBE_OFFSET, jz + cc * CUBE_OFFSET);
                    if (a > 0) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    }
                    if (b > 0) {
                        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                    }
                    if (c > 0) {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    }
                    poseStack.scale(CUBE_SCALE, CUBE_SCALE, CUBE_SCALE);
                    renderCube(
                            poseStack,
                            buffers,
                            type,
                            glowType,
                            light,
                            active,
                            animationTime,
                            startUp,
                            a * 2 + b * 3 + c * 4);
                    poseStack.popPose();
                }
            }
        }
        poseStack.pushPose();
        poseStack.scale(CORE_SCALE, CORE_SCALE, CORE_SCALE);
        renderCube(poseStack, buffers, type, glowType, light, active, animationTime, startUp, 0);
        poseStack.popPose();
        poseStack.popPose();
        if (crafting) {
            drawHalo(craftTicks, fancyGraphics, poseStack, buffers);
        }
    }

    private void renderCube(
            PoseStack poseStack,
            MultiBufferSource buffers,
            RenderType type,
            RenderType glowType,
            int light,
            boolean active,
            float animationTime,
            float startUp,
            int phase) {
        model.cube.render(poseStack, buffers.getBuffer(type), light, OverlayTexture.NO_OVERLAY, -1);
        if (active) {
            float glowAlpha = (Mth.sin((animationTime + phase) / 4.0F) * 0.1F + 0.2F) * startUp;
            model.glow.render(
                    poseStack,
                    buffers.getBuffer(glowType),
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    ARGB32.colorFromFloat(glowAlpha, GLOW_RED, GLOW_GREEN, GLOW_BLUE));
        }
    }

    private static float oscillate(float time, int period) {
        return Mth.sin(Mth.TWO_PI * (time % period) / period);
    }

    private static ResourceLocation pickTexture(BlockEntityInfusionMatrix matrix) {
        Level level = matrix.getLevel();
        if (level == null) {
            return TEX_NORMAL;
        }
        BlockPos corner = matrix.getBlockPos().offset(-1, -2, -1);
        Block block = level.getBlockState(corner).getBlock();
        if (block == TTBlocks.PILLAR_ANCIENT.get()) {
            return TEX_ANCIENT;
        }
        if (block == TTBlocks.PILLAR_ELDRITCH.get()) {
            return TEX_ELDRITCH;
        }
        return TEX_NORMAL;
    }

    private static RenderType glowTypeFor(ResourceLocation texture) {
        if (texture.equals(TEX_ANCIENT)) {
            return GLOW_ANCIENT;
        }
        if (texture.equals(TEX_ELDRITCH)) {
            return GLOW_ELDRITCH;
        }
        return GLOW_NORMAL;
    }

    private void drawHalo(int craftTicks, boolean fancyGraphics, PoseStack poseStack, MultiBufferSource buffers) {
        int fans = fancyGraphics ? HALO_FANS_FANCY : HALO_FANS_FAST;
        float f1 = craftTicks / HALO_FADE_TICKS;
        float ramp = Math.min(craftTicks, HALO_RAMP_TICKS) / HALO_RAMP_TICKS;
        float centerAlpha = Math.max(0.0F, 1.0F - f1);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        haloRandom.setSeed(HALO_SEED);
        VertexConsumer buffer = buffers.getBuffer(HALO_TYPE);
        for (int i = 0; i < fans; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(haloRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(haloRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(haloRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(haloRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(haloRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(haloRandom.nextFloat() * 360.0F + f1 * 360.0F));
            float fa = (haloRandom.nextFloat() * 20.0F + 5.0F) / 20.0F * ramp;
            float f4 = (haloRandom.nextFloat() * 2.0F + 1.0F) / 20.0F * ramp;
            Matrix4f mat = poseStack.last().pose();
            float bx1 = -0.866F * f4;
            float bz1 = -0.5F * f4;
            float bx2 = 0.866F * f4;
            float bz3 = f4;
            buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, centerAlpha);
            buffer.addVertex(mat, bx1, fa, bz1).setColor(1.0F, 0.0F, 1.0F, 0.0F);
            buffer.addVertex(mat, bx2, fa, bz1).setColor(1.0F, 0.0F, 1.0F, 0.0F);
            buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, centerAlpha);
            buffer.addVertex(mat, bx2, fa, bz1).setColor(1.0F, 0.0F, 1.0F, 0.0F);
            buffer.addVertex(mat, 0.0F, fa, bz3).setColor(1.0F, 0.0F, 1.0F, 0.0F);
            buffer.addVertex(mat, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, centerAlpha);
            buffer.addVertex(mat, 0.0F, fa, bz3).setColor(1.0F, 0.0F, 1.0F, 0.0F);
            buffer.addVertex(mat, bx1, fa, bz1).setColor(1.0F, 0.0F, 1.0F, 0.0F);
        }
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
