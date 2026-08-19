package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCFXPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ArchitectOverlayRenderer {
    private static final Identifier FRAME_CORNER = TCIds.rl("textures/misc/frame_corner.png");
    private static final Identifier FRAME_SIDE = TCIds.rl("textures/misc/frame_side.png");
    private static final Identifier ARROWS = TCIds.rl("textures/misc/architect_arrows.png");

    private static final RenderPipeline PIPELINE = TCFXPipelines.additiveTexturedNoDepth(TCIds.rl("pipeline/architect_overlay"));
    private static final RenderType SIDE_TYPE = makeType("thaumaturge_architect_side", FRAME_SIDE);
    private static final RenderType CORNER_TYPE = makeType("thaumaturge_architect_corner", FRAME_CORNER);
    private static final RenderType ARROWS_TYPE = makeType("thaumaturge_architect_arrows", ARROWS);

    private static final int[][] MOS = {{4, 5, 6, 7}, {0, 1, 2, 3}, {0, 1, 4, 5}, {2, 3, 6, 7}, {0, 2, 4, 6}, {1, 3, 5, 7}};
    private static final int[][] ROTMAT = {{0, 90, 270, 180}, {270, 180, 0, 90}, {180, 90, 270, 0}, {0, 270, 90, 180}, {270, 180, 0, 90}, {180, 270, 90, 0}};

    private static final int HASH_TICK_STEP = 5;
    private static final float HALF = 0.5F;
    private static final float SIDE_ALPHA = 0.1F;
    private static final float CORNER_ALPHA = 0.66F;
    private static final float AXIS_R_BASE = 0.3F;
    private static final float AXIS_G_BASE = 0.3F;
    private static final float AXIS_B_BASE = 0.8F;
    private static final float AXIS_PULSE = 0.2F;
    private static final float AXIS_R_PERIOD = 4.0F;
    private static final float AXIS_G_PERIOD = 3.0F;
    private static final float AXIS_B_PERIOD = 2.0F;

    private static int lastArcHash;
    private static List<BlockPos> architectBlocks = new ArrayList<>();
    private static final Set<BlockPos> architectSet = new HashSet<>();
    private static final Map<BlockPos, boolean[]> bmCache = new HashMap<>();

    private ArchitectOverlayRenderer() {}

    private static RenderType makeType(String name, Identifier texture) {
        return RenderType.create(name, RenderSetup.builder(PIPELINE).withTexture("Sampler0", texture).createRenderSetup());
    }

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null || mc.options.hideGui) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IArchitect)) {
            stack = player.getOffhandItem();
        }
        if (!(stack.getItem() instanceof IArchitect architect) || architect.useBlockHighlight(stack)) {
            return;
        }
        HitResult target = architect.getArchitectMOP(stack, mc.level, player);
        if (!(target instanceof BlockHitResult hit) || target.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos anchor = hit.getBlockPos();
        int hash = (anchor.getX() + "" + anchor.getY() + "" + anchor.getZ() + "" + hit.getDirection() + "" + player.tickCount / HASH_TICK_STEP).hashCode();
        if (hash != lastArcHash) {
            lastArcHash = hash;
            bmCache.clear();
            architectBlocks = architect.getArchitectBlocks(stack, mc.level, anchor, hit.getDirection(), player);
            architectSet.clear();
            architectSet.addAll(architectBlocks);
        }
        if (architectBlocks.isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        drawArchitectAxis(poseStack, buffers, player, anchor, cam, architect.showAxis(stack, mc.level, player, hit.getDirection(), IArchitect.EnumAxis.X),
                architect.showAxis(stack, mc.level, player, hit.getDirection(), IArchitect.EnumAxis.Y), architect.showAxis(stack, mc.level, player, hit.getDirection(), IArchitect.EnumAxis.Z));
        for (BlockPos pos : architectBlocks) {
            drawOverlayBlock(poseStack, buffers, pos, cam);
        }
        buffers.endBatch();
    }

    private static boolean isConnected(BlockPos pos) {
        return architectSet.contains(pos);
    }

    private static boolean[] connectedSides(BlockPos pos) {
        boolean[] cached = bmCache.get(pos);
        if (cached != null) {
            return cached;
        }
        boolean[] bitMatrix = {!isConnected(pos.offset(-1, 0, 0)) && !isConnected(pos.offset(0, 0, -1)) && !isConnected(pos.offset(0, 1, 0)),
                !isConnected(pos.offset(1, 0, 0)) && !isConnected(pos.offset(0, 0, -1)) && !isConnected(pos.offset(0, 1, 0)),
                !isConnected(pos.offset(-1, 0, 0)) && !isConnected(pos.offset(0, 0, 1)) && !isConnected(pos.offset(0, 1, 0)),
                !isConnected(pos.offset(1, 0, 0)) && !isConnected(pos.offset(0, 0, 1)) && !isConnected(pos.offset(0, 1, 0)),
                !isConnected(pos.offset(-1, 0, 0)) && !isConnected(pos.offset(0, 0, -1)) && !isConnected(pos.offset(0, -1, 0)),
                !isConnected(pos.offset(1, 0, 0)) && !isConnected(pos.offset(0, 0, -1)) && !isConnected(pos.offset(0, -1, 0)),
                !isConnected(pos.offset(-1, 0, 0)) && !isConnected(pos.offset(0, 0, 1)) && !isConnected(pos.offset(0, -1, 0)),
                !isConnected(pos.offset(1, 0, 0)) && !isConnected(pos.offset(0, 0, 1)) && !isConnected(pos.offset(0, -1, 0))};
        bmCache.put(pos.immutable(), bitMatrix);
        return bitMatrix;
    }

    private static void drawOverlayBlock(PoseStack poseStack, MultiBufferSource buffers, BlockPos pos, Vec3 cam) {
        boolean[] bitMatrix = connectedSides(pos);
        poseStack.pushPose();
        poseStack.translate(pos.getX() + HALF - cam.x, pos.getY() + HALF - cam.y, pos.getZ() + HALF - cam.z);
        for (Direction face : Direction.values()) {
            if (isConnected(pos.relative(face))) {
                continue;
            }
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), -face.getStepY(), face.getStepX(), -face.getStepZ()));
            poseStack.translate(0.0, 0.0, face.getStepZ() < 0 ? -HALF : HALF);
            poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), 0.0F, 0.0F, -1.0F));
            drawQuad(poseStack, buffers.getBuffer(SIDE_TYPE), 1.0F, 1.0F, 1.0F, SIDE_ALPHA);
            for (int a = 0; a < 4; a++) {
                if (bitMatrix[MOS[face.ordinal()][a]]) {
                    poseStack.pushPose();
                    poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(ROTMAT[face.ordinal()][a]), 0.0F, 0.0F, 1.0F));
                    drawQuad(poseStack, buffers.getBuffer(CORNER_TYPE), 1.0F, 1.0F, 1.0F, CORNER_ALPHA);
                    poseStack.popPose();
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void drawArchitectAxis(PoseStack poseStack, MultiBufferSource buffers, LocalPlayer player, BlockPos pos, Vec3 cam, boolean dx, boolean dy, boolean dz) {
        if (!dx && !dy && !dz) {
            return;
        }
        float r = Mth.sin(player.tickCount / AXIS_R_PERIOD + pos.getX()) * AXIS_PULSE + AXIS_R_BASE;
        float g = Mth.sin(player.tickCount / AXIS_G_PERIOD + pos.getY()) * AXIS_PULSE + AXIS_G_BASE;
        float b = Mth.sin(player.tickCount / AXIS_B_PERIOD + pos.getZ()) * AXIS_PULSE + AXIS_B_BASE;
        VertexConsumer buffer = buffers.getBuffer(ARROWS_TYPE);
        poseStack.pushPose();
        poseStack.translate(pos.getX() + HALF - cam.x, pos.getY() + HALF - cam.y, pos.getZ() + HALF - cam.z);
        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(90.0)));
        if (dz) {
            poseStack.pushPose();
            drawQuad(poseStack, buffer, r, g, b, 1.0F);
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(90.0)));
            drawQuad(poseStack, buffer, r, g, b, 1.0F);
            poseStack.popPose();
        }
        if (dx) {
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(90.0)));
            drawQuad(poseStack, buffer, r, g, b, 1.0F);
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(90.0)));
            drawQuad(poseStack, buffer, r, g, b, 1.0F);
            poseStack.popPose();
        }
        if (dy) {
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(90.0)));
            drawQuad(poseStack, buffer, r, g, b, 1.0F);
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(90.0)));
            drawQuad(poseStack, buffer, r, g, b, 1.0F);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void drawQuad(PoseStack poseStack, VertexConsumer buffer, float r, float g, float b, float alpha) {
        int color = ARGB.colorFromFloat(alpha, Math.min(r, 1.0F), Math.min(g, 1.0F), Math.min(b, 1.0F));
        PoseStack.Pose pose = poseStack.last();
        buffer.addVertex(pose, -HALF, HALF, 0.0F).setUv(1.0F, 1.0F).setColor(color);
        buffer.addVertex(pose, HALF, HALF, 0.0F).setUv(1.0F, 0.0F).setColor(color);
        buffer.addVertex(pose, HALF, -HALF, 0.0F).setUv(0.0F, 0.0F).setColor(color);
        buffer.addVertex(pose, -HALF, -HALF, 0.0F).setUv(0.0F, 1.0F).setColor(color);
    }
}
