package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigArea;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCFXPipelines;
import com.leclowndu93150.thaumaturge.content.golem.seals.ClientSealHolder;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class SealWorldRenderer {
    private static final Identifier AREA_RING = TCIds.rl("textures/misc/seal_area.png");
    private static final Identifier CORNER_FRAME = TCIds.rl("textures/misc/frame_corner.png");
    private static final double MAX_DIST_SQR = 256.0;
    private static final float RING_SCALE = 0.9F;
    private static final float ICON_SCALE = 0.5F;
    private static final float RING_ALPHA = 0.8F;
    private static final float CORNER_ALPHA = 0.7F;

    private static final RenderPipeline PIPELINE = TCFXPipelines.translucentTextured(TCIds.rl("pipeline/seal_overlay"));
    private static final Map<Identifier, RenderType> TYPES = new ConcurrentHashMap<>();
    private static final RenderType ICON_TYPE = RenderType.create("tc_seal_icon", RenderSetup.builder(PIPELINE).withTexture("Sampler0", TextureAtlas.LOCATION_ITEMS).createRenderSetup());

    private static final Direction[][] ROT_FACES = {{Direction.DOWN, Direction.NORTH, Direction.WEST}, {Direction.UP, Direction.NORTH, Direction.WEST},
            {Direction.DOWN, Direction.NORTH, Direction.EAST}, {Direction.UP, Direction.NORTH, Direction.EAST}, {Direction.DOWN, Direction.SOUTH, Direction.EAST},
            {Direction.UP, Direction.SOUTH, Direction.EAST}, {Direction.DOWN, Direction.SOUTH, Direction.WEST}, {Direction.UP, Direction.SOUTH, Direction.WEST}};
    private static final int[][] ROT_MAT = {{0, 270, 0}, {270, 180, 270}, {90, 0, 90}, {180, 90, 180}, {180, 180, 0}, {90, 270, 270}, {270, 90, 90}, {0, 0, 180}};

    private SealWorldRenderer() {}

    private static RenderType typeFor(Identifier texture) {
        return TYPES.computeIfAbsent(texture, tex -> RenderType.create("tc_seal_overlay_" + tex.getPath().hashCode(), RenderSetup.builder(PIPELINE).withTexture("Sampler0", tex).createRenderSetup()));
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null || mc.options.hideGui) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof ISealDisplayer) && !(player.getOffhandItem().getItem() instanceof ISealDisplayer)) {
            return;
        }
        if (ClientSealHolder.all().isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float time = player.tickCount % 360 + partialTicks;
        for (SealEntity seal : ClientSealHolder.all().values()) {
            BlockPos pos = seal.getSealPos().pos();
            double distSqr = player.distanceToSqr(Vec3.atCenterOf(pos));
            if (distSqr > MAX_DIST_SQR) {
                continue;
            }
            float alpha = 1.0F - (float) (distSqr / MAX_DIST_SQR);
            boolean inactive = seal.isStoppedByRedstone(mc.level);
            drawSealIcon(poseStack, buffers, seal, cam, alpha, inactive);
            drawSealRing(poseStack, buffers, seal, cam, alpha, time);
            if (seal.getSeal() instanceof ISealConfigArea) {
                drawAreaCorners(poseStack, buffers, seal, cam, alpha, time);
            }
        }
        buffers.endBatch();
    }

    private static void drawSealIcon(PoseStack poseStack, MultiBufferSource buffers, SealEntity seal, Vec3 cam, float alpha, boolean inactive) {
        BlockPos pos = seal.getSealPos().pos();
        Direction face = seal.getSealPos().face();
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5 - cam.x, pos.getY() + 0.5 - cam.y, pos.getZ() + 0.5 - cam.z);
        poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), -face.getStepY(), face.getStepX(), -face.getStepZ()));
        poseStack.translate(0.0, 0.0, face.getStepZ() < 0 ? -0.55 : 0.55);
        float shade = inactive ? 0.5F : 1.0F;
        int color = ARGB.colorFromFloat(alpha, shade, shade, shade);
        TextureAtlasSprite sprite = iconSprite(seal.getSeal().getSealIcon());
        drawQuad(poseStack, buffers.getBuffer(ICON_TYPE), ICON_SCALE / 2.0F, color, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
        poseStack.popPose();
    }

    private static TextureAtlasSprite iconSprite(Identifier icon) {
        String path = icon.getPath();
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).getSprite(Identifier.fromNamespaceAndPath(icon.getNamespace(), path));
    }

    private static void drawSealRing(PoseStack poseStack, MultiBufferSource buffers, SealEntity seal, Vec3 cam, float alpha, float time) {
        BlockPos pos = seal.getSealPos().pos();
        Direction face = seal.getSealPos().face();
        float r;
        float g;
        float b;
        if (seal.getColor() > 0) {
            int dye = DyeColor.byId(seal.getColor() - 1).getTextureDiffuseColor();
            r = ARGB.red(dye) / 255.0F;
            g = ARGB.green(dye) / 255.0F;
            b = ARGB.blue(dye) / 255.0F;
        } else {
            r = 0.7F + Mth.sin((time + pos.getX()) / 4.0F) * 0.1F;
            g = 0.7F + Mth.sin((time + pos.getY()) / 5.0F) * 0.1F;
            b = 0.7F + Mth.sin((time + pos.getZ()) / 6.0F) * 0.1F;
        }
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5 - cam.x, pos.getY() + 0.5 - cam.y, pos.getZ() + 0.5 - cam.z);
        poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), -face.getStepY(), face.getStepX(), -face.getStepZ()));
        poseStack.translate(0.0, 0.0, face.getStepZ() < 0 ? -0.51 : 0.51);
        poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(time), 0.0F, 0.0F, 1.0F));
        drawQuad(poseStack, buffers.getBuffer(typeFor(AREA_RING)), RING_SCALE / 2.0F, ARGB.colorFromFloat(alpha * RING_ALPHA, r, g, b));
        poseStack.popPose();
    }

    private static void drawAreaCorners(PoseStack poseStack, MultiBufferSource buffers, SealEntity seal, Vec3 cam, float alpha, float time) {
        BlockPos pos = seal.getSealPos().pos();
        Direction face = seal.getSealPos().face();
        float r = 0.7F + Mth.sin((time + pos.getX()) / 4.0F) * 0.1F;
        float g = 0.7F + Mth.sin((time + pos.getY()) / 5.0F) * 0.1F;
        float b = 0.7F + Mth.sin((time + pos.getZ()) / 6.0F) * 0.1F;
        AABB area = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).move(face.getStepX(), face.getStepY(), face.getStepZ())
                .expandTowards(face.getStepX() != 0 ? (seal.getArea().getX() - 1) * face.getStepX() : 0.0, face.getStepY() != 0 ? (seal.getArea().getY() - 1) * face.getStepY() : 0.0,
                        face.getStepZ() != 0 ? (seal.getArea().getZ() - 1) * face.getStepZ() : 0.0)
                .inflate(face.getStepX() == 0 ? seal.getArea().getX() - 1 : 0.0, face.getStepY() == 0 ? seal.getArea().getY() - 1 : 0.0, face.getStepZ() == 0 ? seal.getArea().getZ() - 1 : 0.0);
        double[][] corners = {{area.minX, area.minY, area.minZ}, {area.minX, area.maxY - 1.0, area.minZ}, {area.maxX - 1.0, area.minY, area.minZ}, {area.maxX - 1.0, area.maxY - 1.0, area.minZ},
                {area.maxX - 1.0, area.minY, area.maxZ - 1.0}, {area.maxX - 1.0, area.maxY - 1.0, area.maxZ - 1.0}, {area.minX, area.minY, area.maxZ - 1.0},
                {area.minX, area.maxY - 1.0, area.maxZ - 1.0}};
        int color = ARGB.colorFromFloat(alpha * CORNER_ALPHA, r, g, b);
        VertexConsumer buffer = buffers.getBuffer(typeFor(CORNER_FRAME));
        for (int q = 0; q < corners.length; q++) {
            poseStack.pushPose();
            poseStack.translate(corners[q][0] + 0.5 - cam.x, corners[q][1] + 0.5 - cam.y, corners[q][2] + 0.5 - cam.z);
            for (int w = 0; w < ROT_FACES[q].length; w++) {
                Direction cornerFace = ROT_FACES[q][w];
                poseStack.pushPose();
                poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), -cornerFace.getStepY(), cornerFace.getStepX(), -cornerFace.getStepZ()));
                poseStack.translate(0.0, 0.0, cornerFace.getStepZ() < 0 ? -0.49 : 0.49);
                poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), 0.0F, 0.0F, -1.0F));
                poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(ROT_MAT[q][w]), 0.0F, 0.0F, 1.0F));
                drawQuad(poseStack, buffer, 0.5F, color);
                poseStack.popPose();
            }
            poseStack.popPose();
        }
    }

    private static void drawQuad(PoseStack poseStack, VertexConsumer buffer, float half, int color) {
        drawQuad(poseStack, buffer, half, color, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    private static void drawQuad(PoseStack poseStack, VertexConsumer buffer, float half, int color, float u0, float v0, float u1, float v1) {
        PoseStack.Pose pose = poseStack.last();
        buffer.addVertex(pose, -half, half, 0.0F).setUv(u1, v1).setColor(color);
        buffer.addVertex(pose, half, half, 0.0F).setUv(u1, v0).setColor(color);
        buffer.addVertex(pose, half, -half, 0.0F).setUv(u0, v0).setColor(color);
        buffer.addVertex(pose, -half, -half, 0.0F).setUv(u0, v1).setColor(color);
    }
}
