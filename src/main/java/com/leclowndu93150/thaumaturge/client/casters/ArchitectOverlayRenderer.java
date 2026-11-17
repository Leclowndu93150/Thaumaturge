package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.AreaPreviewer;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ArchitectOverlayRenderer {
    private static final ResourceLocation FRAME_CORNER = ParticleTextures.FRAME_CORNER;
    private static final ResourceLocation FRAME_SIDE = TTIds.rl("textures/misc/frame_side.png");
    private static final ResourceLocation ARROWS = TTIds.rl("textures/misc/architect_arrows.png");

    private static final RenderType SIDE_TYPE = TTRenderTypes.translucentTexturedNoDepth(FRAME_SIDE);
    private static final RenderType CORNER_TYPE = TTRenderTypes.translucentTexturedNoDepth(FRAME_CORNER);
    private static final RenderType ARROWS_TYPE = TTRenderTypes.fxAlphaAdditiveNoDepth(ARROWS);

    private static final int[][] MOS = {
        {4, 5, 6, 7}, {0, 1, 2, 3}, {0, 1, 4, 5}, {2, 3, 6, 7}, {0, 2, 4, 6}, {1, 3, 5, 7}
    };
    private static final int[][] ROTMAT = {
        {0, 90, 270, 180}, {270, 180, 0, 90}, {180, 90, 270, 0},
        {0, 270, 90, 180}, {270, 180, 0, 90}, {180, 270, 90, 0}
    };

    private static final int HASH_TICK_STEP = 5;
    private static final float HALF = 0.5F;
    private static final float SIDE_ALPHA = 0.1F;
    private static final float CORNER_ALPHA = 0.55F;
    private static final float AXIS_R_BASE = 0.3F;
    private static final float AXIS_G_BASE = 0.3F;
    private static final float AXIS_B_BASE = 0.8F;
    private static final float AXIS_PULSE = 0.2F;
    private static final float AXIS_R_PERIOD = 4.0F;
    private static final float AXIS_G_PERIOD = 3.0F;
    private static final float AXIS_B_PERIOD = 2.0F;

    private static final PreviewCache PREVIEW_CACHE = new PreviewCache();
    private static List<BlockPos> architectBlocks = new ArrayList<>();
    private static final Set<BlockPos> architectSet = new HashSet<>();
    private static final Map<BlockPos, boolean[]> bmCache = new HashMap<>();

    private ArchitectOverlayRenderer() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null || mc.options.hideGui) {
            return;
        }
        ItemStack stack = heldArchitect(player);
        if (!(stack.getItem() instanceof AreaPreviewer architect)) {
            return;
        }
        HitResult target = architect.traceAnchor(stack, mc.level, player);
        if (!(target instanceof BlockHitResult hit) || target.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos anchor = hit.getBlockPos();
        int bucket = player.tickCount / HASH_TICK_STEP;
        if (PREVIEW_CACHE.update(mc.level, LevelSpaces.frameOf(mc.level, anchor), anchor, hit.getDirection(), bucket)) {
            bmCache.clear();
            architectBlocks = architect.previewArea(stack, mc.level, anchor, hit.getDirection(), player);
            architectSet.clear();
            architectSet.addAll(architectBlocks);
        }
        if (architectBlocks.isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        Vec3 corner = Vec3.atLowerCornerOf(anchor);
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        poseStack.pushPose();
        ClientSpaces.applyFrame(poseStack, mc.level, corner, camera);
        drawArchitectAxis(
                poseStack,
                buffers,
                player,
                anchor,
                corner,
                architect.showsAxisArrows(stack, mc.level, player, hit.getDirection(), Direction.Axis.X),
                architect.showsAxisArrows(stack, mc.level, player, hit.getDirection(), Direction.Axis.Y),
                architect.showsAxisArrows(stack, mc.level, player, hit.getDirection(), Direction.Axis.Z));
        for (BlockPos pos : architectBlocks) {
            drawOverlayBlock(poseStack, buffers, pos, corner);
        }
        buffers.endBatch();
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        ItemStack stack = heldArchitect(player);
        if (!(stack.getItem() instanceof AreaPreviewer architect) || !architect.hidesBlockOutline(stack)) {
            return;
        }
        BlockHitResult hit = event.getTarget();
        if (!architect
                .previewArea(stack, mc.level, hit.getBlockPos(), hit.getDirection(), player)
                .isEmpty()) {
            event.setCanceled(true);
        }
    }

    static ItemStack heldArchitect(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof AreaPreviewer ? stack : player.getOffhandItem();
    }

    private static boolean isConnected(BlockPos pos) {
        return architectSet.contains(pos);
    }

    private static boolean[] connectedSides(BlockPos pos) {
        boolean[] cached = bmCache.get(pos);
        if (cached != null) {
            return cached;
        }
        boolean[] bitMatrix = {
            !isConnected(pos.offset(-1, 0, 0))
                    && !isConnected(pos.offset(0, 0, -1))
                    && !isConnected(pos.offset(0, 1, 0)),
            !isConnected(pos.offset(1, 0, 0))
                    && !isConnected(pos.offset(0, 0, -1))
                    && !isConnected(pos.offset(0, 1, 0)),
            !isConnected(pos.offset(-1, 0, 0))
                    && !isConnected(pos.offset(0, 0, 1))
                    && !isConnected(pos.offset(0, 1, 0)),
            !isConnected(pos.offset(1, 0, 0)) && !isConnected(pos.offset(0, 0, 1)) && !isConnected(pos.offset(0, 1, 0)),
            !isConnected(pos.offset(-1, 0, 0))
                    && !isConnected(pos.offset(0, 0, -1))
                    && !isConnected(pos.offset(0, -1, 0)),
            !isConnected(pos.offset(1, 0, 0))
                    && !isConnected(pos.offset(0, 0, -1))
                    && !isConnected(pos.offset(0, -1, 0)),
            !isConnected(pos.offset(-1, 0, 0))
                    && !isConnected(pos.offset(0, 0, 1))
                    && !isConnected(pos.offset(0, -1, 0)),
            !isConnected(pos.offset(1, 0, 0)) && !isConnected(pos.offset(0, 0, 1)) && !isConnected(pos.offset(0, -1, 0))
        };
        bmCache.put(pos.immutable(), bitMatrix);
        return bitMatrix;
    }

    static void translateToBlockCenter(PoseStack poseStack, BlockPos pos, Vec3 origin) {
        // Sable plot coordinates are too large to add the half block in float precision.
        Vec3 center = Vec3.atCenterOf(pos).subtract(origin);
        poseStack.translate(center.x, center.y, center.z);
    }

    private static void drawOverlayBlock(PoseStack poseStack, MultiBufferSource buffers, BlockPos pos, Vec3 origin) {
        boolean[] bitMatrix = connectedSides(pos);
        poseStack.pushPose();
        translateToBlockCenter(poseStack, pos, origin);
        for (Direction face : Direction.values()) {
            if (isConnected(pos.relative(face))) {
                continue;
            }
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf()
                    .rotationAxis((float) Math.toRadians(90.0), -face.getStepY(), face.getStepX(), -face.getStepZ()));
            poseStack.translate(0.0, 0.0, face.getStepZ() < 0 ? -HALF : HALF);
            poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), 0.0F, 0.0F, -1.0F));
            drawQuad(poseStack, buffers.getBuffer(SIDE_TYPE), 1.0F, 1.0F, 1.0F, SIDE_ALPHA);
            for (int a = 0; a < 4; a++) {
                if (bitMatrix[MOS[face.ordinal()][a]]) {
                    poseStack.pushPose();
                    poseStack.mulPose(new Quaternionf()
                            .rotationAxis((float) Math.toRadians(ROTMAT[face.ordinal()][a]), 0.0F, 0.0F, 1.0F));
                    drawQuad(poseStack, buffers.getBuffer(CORNER_TYPE), 1.0F, 1.0F, 1.0F, CORNER_ALPHA);
                    poseStack.popPose();
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void drawArchitectAxis(
            PoseStack poseStack,
            MultiBufferSource buffers,
            LocalPlayer player,
            BlockPos pos,
            Vec3 origin,
            boolean dx,
            boolean dy,
            boolean dz) {
        if (!dx && !dy && !dz) {
            return;
        }
        float r = Mth.sin(player.tickCount / AXIS_R_PERIOD + pos.getX()) * AXIS_PULSE + AXIS_R_BASE;
        float g = Mth.sin(player.tickCount / AXIS_G_PERIOD + pos.getY()) * AXIS_PULSE + AXIS_G_BASE;
        float b = Mth.sin(player.tickCount / AXIS_B_PERIOD + pos.getZ()) * AXIS_PULSE + AXIS_B_BASE;
        VertexConsumer buffer = buffers.getBuffer(ARROWS_TYPE);
        poseStack.pushPose();
        translateToBlockCenter(poseStack, pos, origin);
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
        int color = ARGB32.colorFromFloat(alpha, Math.min(r, 1.0F), Math.min(g, 1.0F), Math.min(b, 1.0F));
        PoseStack.Pose pose = poseStack.last();
        // The particle format used by the arrows requires a lightmap value.
        buffer.addVertex(pose, -HALF, HALF, 0.0F)
                .setUv(1.0F, 1.0F)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(pose, HALF, HALF, 0.0F)
                .setUv(1.0F, 0.0F)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(pose, HALF, -HALF, 0.0F)
                .setUv(0.0F, 0.0F)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(pose, -HALF, -HALF, 0.0F)
                .setUv(0.0F, 1.0F)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
    }

    static final class PreviewCache {
        private Level level;
        private SpaceFrame frame;
        private BlockPos anchor;
        private Direction face;
        private int bucket;

        boolean update(Level nextLevel, SpaceFrame nextFrame, BlockPos nextAnchor, Direction nextFace, int nextBucket) {
            if (nextLevel == level
                    && nextFrame.equals(frame)
                    && nextAnchor.equals(anchor)
                    && nextFace == face
                    && nextBucket == bucket) {
                return false;
            }
            level = nextLevel;
            frame = nextFrame;
            anchor = nextAnchor.immutable();
            face = nextFace;
            bucket = nextBucket;
            return true;
        }
    }
}
