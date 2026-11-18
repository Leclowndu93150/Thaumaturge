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
    private static final float IDLE_BOB_DIVISOR = 9.0F;
    private static final int CRAFT_BOB_PERIOD = 25;
    private static final float CRAFT_BOB_DIVISOR = 3.5F;
    private static final float BOB_BLEND_TICKS = 15.0F;
    private static final float SPIN_Z_DIVISOR = 4.0F;
    private static final float TILT = 45.0F;
    private static final float JITTER_SCALE = 0.025F;
    private static final float CRAFT_SWAY = 2.2F;
    private static final float HALO_GROW_TICKS = 80.0F;
    private static final float HALO_TURN_TICKS = 1600.0F;
    private static final float HALO_CORE_ALPHA = 0.85F;
    private static final int HALO_TIP_RGB = 0xE23CC9;
    private static final int HALO_FANCY_SPIKES = InfusionMatrixRenderMath.spikeCount();
    private static final int HALO_FAST_SPIKES = 14;
    private static final int HALO_AXES = 3;
    private static final int HALO_SPIKE_CORNERS = 3;
    private static final float[] HALO_SPIKES = InfusionMatrixRenderMath.buildSpikes();

    private static final RenderType GLOW_NORMAL = TTRenderTypes.entityAdditiveEmissive(TEX_NORMAL);
    private static final RenderType GLOW_ANCIENT = TTRenderTypes.entityAdditiveEmissive(TEX_ANCIENT);
    private static final RenderType GLOW_ELDRITCH = TTRenderTypes.entityAdditiveEmissive(TEX_ELDRITCH);
    private static final RenderType HALO_TYPE = TTRenderTypes.SPARKLE_CULLED;

    private final MatrixCubeModel model;

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
        matrix.restoreClientAnimation();
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
        float instability = InfusionMatrixRenderMath.instability(stability, craftTicks);
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
                    int index = a << 2 | b << 1 | c;
                    float jitter = active ? JITTER_SCALE * startUp * instability : 0.0F;
                    float jx = offset(a)
                            * jitter
                            * InfusionMatrixRenderMath.drift(animationTime, index, InfusionMatrixRenderMath.SALT_X);
                    float jy = offset(b)
                            * jitter
                            * InfusionMatrixRenderMath.drift(animationTime, index, InfusionMatrixRenderMath.SALT_Y);
                    float jz = offset(c)
                            * jitter
                            * InfusionMatrixRenderMath.drift(animationTime, index, InfusionMatrixRenderMath.SALT_Z);
                    int aa = a == 0 ? -1 : 1;
                    int bb = b == 0 ? -1 : 1;
                    int cc = c == 0 ? -1 : 1;
                    poseStack.pushPose();
                    poseStack.translate(jx + aa * CUBE_OFFSET, jy + bb * CUBE_OFFSET, jz + cc * CUBE_OFFSET);
                    poseStack.mulPose(Axis.XP.rotationDegrees(quarterTurn(index, InfusionMatrixRenderMath.SALT_X)));
                    poseStack.mulPose(Axis.YP.rotationDegrees(quarterTurn(index, InfusionMatrixRenderMath.SALT_Y)));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(quarterTurn(index, InfusionMatrixRenderMath.SALT_Z)));
                    poseStack.scale(CUBE_SCALE, CUBE_SCALE, CUBE_SCALE);
                    renderCube(poseStack, buffers, type, glowType, light, animationTime, startUp, index + 1);
                    poseStack.popPose();
                }
            }
        }
        poseStack.pushPose();
        poseStack.scale(CORE_SCALE, CORE_SCALE, CORE_SCALE);
        renderCube(poseStack, buffers, type, glowType, light, animationTime, startUp, 0);
        poseStack.popPose();
        poseStack.popPose();
        if (crafting) {
            drawHalo(craftTicks, fancyGraphics, startUp, 0.5F + (ACTIVE_LIFT + bob) * startUp, poseStack, buffers);
        }
    }

    private void renderCube(
            PoseStack poseStack,
            MultiBufferSource buffers,
            RenderType type,
            RenderType glowType,
            int light,
            float animationTime,
            float startUp,
            int phase) {
        model.cube.render(poseStack, buffers.getBuffer(type), light, OverlayTexture.NO_OVERLAY, -1);
        float glowAlpha = InfusionMatrixRenderMath.glow(animationTime, startUp, phase);
        if (glowAlpha > 0.0F) {
            model.glow.render(
                    poseStack,
                    buffers.getBuffer(glowType),
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    ARGB32.color(Math.round(Math.clamp(glowAlpha, 0.0F, 1.0F) * 255.0F), 0xC54CFF));
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

    private void drawHalo(
            int craftTicks,
            boolean fancyGraphics,
            float startUp,
            float matrixY,
            PoseStack poseStack,
            MultiBufferSource buffers) {
        float fade = Math.max(0.0F, 1.0F - craftTicks / HALO_TURN_TICKS);
        float coreAlpha = HALO_CORE_ALPHA * fade * startUp;
        float grow = Math.min(craftTicks, HALO_GROW_TICKS) / HALO_GROW_TICKS;
        if (coreAlpha <= 0.0F || grow <= 0.0F) {
            return;
        }
        int spikes = fancyGraphics ? HALO_FANCY_SPIKES : HALO_FAST_SPIKES;
        float reach = 1.6F * grow;
        int coreColor = ARGB32.colorFromFloat(coreAlpha, 1.0F, 1.0F, 1.0F);
        int tipColor = ARGB32.color(0, HALO_TIP_RGB);
        poseStack.pushPose();
        poseStack.translate(0.5F, matrixY, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(craftTicks / HALO_TURN_TICKS * 360.0F));
        VertexConsumer buffer = buffers.getBuffer(HALO_TYPE);
        int stride = InfusionMatrixRenderMath.spikeStride();
        for (int spike = 0; spike < spikes; spike++) {
            int base = spike * stride + HALO_AXES;
            for (int corner = 0; corner < HALO_SPIKE_CORNERS; corner++) {
                int from = base + corner * HALO_AXES;
                int to = base + (corner + 1) % HALO_SPIKE_CORNERS * HALO_AXES;
                Matrix4f matrix = poseStack.last().pose();
                buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(coreColor);
                buffer.addVertex(
                                matrix,
                                HALO_SPIKES[to] * reach,
                                HALO_SPIKES[to + 1] * reach,
                                HALO_SPIKES[to + 2] * reach)
                        .setColor(tipColor);
                buffer.addVertex(
                                matrix,
                                HALO_SPIKES[from] * reach,
                                HALO_SPIKES[from + 1] * reach,
                                HALO_SPIKES[from + 2] * reach)
                        .setColor(tipColor);
            }
        }
        poseStack.popPose();
    }

    private static float offset(int flag) {
        return flag == 1 ? CUBE_OFFSET : -CUBE_OFFSET;
    }

    private static float quarterTurn(int index, int salt) {
        return InfusionMatrixRenderMath.quarterTurns(index, salt) * 90.0F;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
