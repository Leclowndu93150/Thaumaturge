package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.golem.seals.ClientSealHolder;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class SealWorldRenderer {
    private static final String SEAL_ICON_PREFIX = "textures/item/seal_";
    private static final String SEAL_ICON_SUFFIX = ".png";
    private static final ResourceLocation AREA_RING = TTIds.rl("textures/misc/seal_area.png");
    private static final ResourceLocation CORNER_FRAME = TTIds.rl("textures/misc/frame_corner.png");
    private static final double MAX_DIST_SQR = 256.0;
    private static final float RING_SCALE = 0.9F;
    private static final float ICON_SCALE = 0.5F;
    private static final float ICON_THICKNESS = ICON_SCALE / 16.0F;
    private static final float ICON_PIXEL = 1.0F / 16.0F;
    private static final float RING_ALPHA = 0.8F;
    private static final float CORNER_ALPHA = 0.7F;
    private static final RenderType ICON_TYPE = TTRenderTypes.translucentTextured(InventoryMenu.BLOCK_ATLAS);
    private static final RenderType ICON_NO_DEPTH_TYPE =
            TTRenderTypes.translucentTexturedNoDepth(InventoryMenu.BLOCK_ATLAS);
    private static final MultiBufferSource.BufferSource SEAL_BUFFERS =
            MultiBufferSource.immediate(new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE));

    private static final Direction[][] ROT_FACES = {
        {Direction.DOWN, Direction.NORTH, Direction.WEST},
        {Direction.UP, Direction.NORTH, Direction.WEST},
        {Direction.DOWN, Direction.NORTH, Direction.EAST},
        {Direction.UP, Direction.NORTH, Direction.EAST},
        {Direction.DOWN, Direction.SOUTH, Direction.EAST},
        {Direction.UP, Direction.SOUTH, Direction.EAST},
        {Direction.DOWN, Direction.SOUTH, Direction.WEST},
        {Direction.UP, Direction.SOUTH, Direction.WEST}
    };
    private static final int[][] ROT_MAT = {
        {0, 270, 0}, {270, 180, 270}, {90, 0, 90}, {180, 90, 180},
        {180, 180, 0}, {90, 270, 270}, {270, 90, 90}, {0, 0, 180}
    };

    private SealWorldRenderer() {}

    private static RenderType typeFor(ResourceLocation texture) {
        return TTRenderTypes.translucentTextured(texture);
    }

    private static RenderType typeFor(ResourceLocation texture, boolean seeThrough) {
        return seeThrough ? TTRenderTypes.translucentTexturedNoDepth(texture) : typeFor(texture);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        // Water writes depth during the translucent terrain pass, so draw seals before it.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || IrisCompat.isShadowPass()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null || mc.options.hideGui) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof ISealDisplayer)
                && !(player.getOffhandItem().getItem() instanceof ISealDisplayer)) {
            return;
        }
        if (ClientSealHolder.all().isEmpty()) {
            return;
        }
        // Iris defers chest batches past this stage; establish their depth before drawing seals.
        mc.renderBuffers().bufferSource().endBatch();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource effectBuffers = IrisCompat.entityEffectBuffers(SEAL_BUFFERS);
        float partialTicks = mc.getTimer().getGameTimeDeltaPartialTick(false);
        float time = player.tickCount % 360 + partialTicks;
        boolean seeThrough = player.isShiftKeyDown();
        for (SealEntity seal : ClientSealHolder.all().values()) {
            BlockPos pos = seal.pos().pos();
            Vec3 localCenter = Vec3.atCenterOf(pos);
            Quaternionfc orientation = ClientSpaces.renderOrientation(mc.level, localCenter);
            Vec3 center = renderPoint(mc.level, localCenter, orientation, localCenter);
            double distSqr = player.position().distanceToSqr(center);
            if (distSqr > MAX_DIST_SQR) {
                continue;
            }
            float alpha = 1.0F - (float) (distSqr / MAX_DIST_SQR);
            drawSealRing(poseStack, effectBuffers, mc.level, seal, orientation, cam, alpha, time, seeThrough);
        }
        SEAL_BUFFERS.endBatch(typeFor(AREA_RING, seeThrough));
        for (SealEntity seal : ClientSealHolder.all().values()) {
            BlockPos pos = seal.pos().pos();
            Vec3 localCenter = Vec3.atCenterOf(pos);
            Quaternionfc orientation = ClientSpaces.renderOrientation(mc.level, localCenter);
            Vec3 center = renderPoint(mc.level, localCenter, orientation, localCenter);
            double distSqr = player.position().distanceToSqr(center);
            if (distSqr > MAX_DIST_SQR) {
                continue;
            }
            float alpha = 1.0F - (float) (distSqr / MAX_DIST_SQR);
            boolean inactive = seal.isStoppedByRedstone(mc.level);
            drawSealIcon(poseStack, effectBuffers, mc.level, seal, orientation, cam, alpha, inactive, seeThrough);
            if (seal.type().hasArea()) {
                drawAreaCorners(poseStack, effectBuffers, mc.level, seal, orientation, cam, alpha, time, seeThrough);
            }
        }
        SEAL_BUFFERS.endBatch();
    }

    private static void drawSealIcon(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Level level,
            SealEntity seal,
            Quaternionfc orientation,
            Vec3 cam,
            float alpha,
            boolean inactive,
            boolean seeThrough) {
        BlockPos pos = seal.pos().pos();
        Direction face = seal.pos().face();
        Vec3 anchor = Vec3.atCenterOf(pos);
        Vec3 center = renderPoint(level, anchor, orientation, anchor);
        poseStack.pushPose();
        poseStack.translate(center.x - cam.x, center.y - cam.y, center.z - cam.z);
        poseStack.mulPose(new Quaternionf(orientation).mul(iconFaceRotation(face)));
        poseStack.translate(0.0, 0.0, 0.55);
        float shade = inactive ? 0.5F : 1.0F;
        int color = ARGB32.colorFromFloat(alpha, shade, shade, shade);
        TextureAtlasSprite sprite = iconSprite(seal.type().icon().orElseGet(() -> defaultIcon(seal.typeId())));
        drawExtrudedIcon(poseStack, buffers.getBuffer(seeThrough ? ICON_NO_DEPTH_TYPE : ICON_TYPE), sprite, color);
        poseStack.popPose();
    }

    private static void drawExtrudedIcon(
            PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int color) {
        float half = ICON_SCALE / 2.0F;
        float depth = ICON_THICKNESS;
        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();
        PoseStack.Pose pose = poseStack.last();

        emitQuad(
                pose, buffer, color, -half, half, 0.0F, u0, v0, half, half, 0.0F, u1, v0, half, -half, 0.0F, u1, v1,
                -half, -half, 0.0F, u0, v1);
        emitQuad(
                pose, buffer, color, -half, half, -depth, u0, v0, half, half, -depth, u1, v0, half, -half, -depth, u1,
                v1, -half, -half, -depth, u0, v1);

        float du = (u1 - u0) * ICON_PIXEL;
        float dv = (v1 - v0) * ICON_PIXEL;
        emitQuad(
                pose, buffer, color, -half, half, 0.0F, u0, v0, half, half, 0.0F, u1, v0, half, half, -depth, u1,
                v0 + dv, -half, half, -depth, u0, v0 + dv);
        emitQuad(
                pose, buffer, color, -half, -half, 0.0F, u0, v1, half, -half, 0.0F, u1, v1, half, -half, -depth, u1,
                v1 - dv, -half, -half, -depth, u0, v1 - dv);
        emitQuad(
                pose, buffer, color, -half, half, 0.0F, u0, v0, -half, -half, 0.0F, u0, v1, -half, -half, -depth,
                u0 + du, v1, -half, half, -depth, u0 + du, v0);
        emitQuad(
                pose, buffer, color, half, half, 0.0F, u1, v0, half, -half, 0.0F, u1, v1, half, -half, -depth, u1 - du,
                v1, half, half, -depth, u1 - du, v0);

        int width = sprite.contents().width();
        int height = sprite.contents().height();
        float pixelWidth = ICON_SCALE / width;
        float pixelHeight = ICON_SCALE / height;
        for (int y = 0; y < height; y++) {
            float yTop = half - y * pixelHeight;
            float yBottom = yTop - pixelHeight;
            float vTop = sprite.getV((float) y / height);
            float vBottom = sprite.getV((float) (y + 1) / height);
            for (int x = 0; x < width; x++) {
                if (!pixelOpaque(sprite, x, y, width, height)) {
                    continue;
                }
                float xLeft = -half + x * pixelWidth;
                float xRight = xLeft + pixelWidth;
                float uLeft = sprite.getU((float) x / width);
                float uRight = sprite.getU((float) (x + 1) / width);
                emitOutlineSide(
                        pose, buffer, color, sprite, x, y, width, height, xLeft, xRight, yTop, yBottom, uLeft, uRight,
                        vTop, vBottom, depth);
            }
        }
    }

    private static void emitOutlineSide(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            int color,
            TextureAtlasSprite sprite,
            int x,
            int y,
            int width,
            int height,
            float xLeft,
            float xRight,
            float yTop,
            float yBottom,
            float uLeft,
            float uRight,
            float vTop,
            float vBottom,
            float depth) {
        if (x > 0 && !pixelOpaque(sprite, x - 1, y, width, height)) {
            emitQuad(
                    pose, buffer, color, xLeft, yTop, 0.0F, uLeft, vTop, xLeft, yTop, -depth, uRight, vTop, xLeft,
                    yBottom, -depth, uRight, vBottom, xLeft, yBottom, 0.0F, uLeft, vBottom);
        }
        if (x + 1 < width && !pixelOpaque(sprite, x + 1, y, width, height)) {
            emitQuad(
                    pose, buffer, color, xRight, yTop, 0.0F, uRight, vTop, xRight, yTop, -depth, uLeft, vTop, xRight,
                    yBottom, -depth, uLeft, vBottom, xRight, yBottom, 0.0F, uRight, vBottom);
        }
        if (y > 0 && !pixelOpaque(sprite, x, y - 1, width, height)) {
            emitQuad(
                    pose, buffer, color, xLeft, yTop, 0.0F, uLeft, vTop, xRight, yTop, 0.0F, uRight, vTop, xRight, yTop,
                    -depth, uRight, vBottom, xLeft, yTop, -depth, uLeft, vBottom);
        }
        if (y + 1 < height && !pixelOpaque(sprite, x, y + 1, width, height)) {
            emitQuad(
                    pose, buffer, color, xLeft, yBottom, 0.0F, uLeft, vBottom, xRight, yBottom, 0.0F, uRight, vBottom,
                    xRight, yBottom, -depth, uRight, vTop, xLeft, yBottom, -depth, uLeft, vTop);
        }
    }

    private static boolean pixelOpaque(TextureAtlasSprite sprite, int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height && ABGR32.alpha(sprite.getPixelRGBA(0, x, y)) > 0;
    }

    private static void emitQuad(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            int color,
            float x0,
            float y0,
            float z0,
            float u0,
            float v0,
            float x1,
            float y1,
            float z1,
            float u1,
            float v1,
            float x2,
            float y2,
            float z2,
            float u2,
            float v2,
            float x3,
            float y3,
            float z3,
            float u3,
            float v3) {
        buffer.addVertex(pose, x0, y0, z0).setUv(u0, v0).setColor(color);
        buffer.addVertex(pose, x1, y1, z1).setUv(u1, v1).setColor(color);
        buffer.addVertex(pose, x2, y2, z2).setUv(u2, v2).setColor(color);
        buffer.addVertex(pose, x3, y3, z3).setUv(u3, v3).setColor(color);
    }

    private static ResourceLocation defaultIcon(ResourceLocation typeId) {
        return typeId.withPath(path -> SEAL_ICON_PREFIX + path + SEAL_ICON_SUFFIX);
    }

    private static TextureAtlasSprite iconSprite(ResourceLocation icon) {
        String path = icon.getPath();
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        ResourceLocation spriteId = ResourceLocation.fromNamespaceAndPath(icon.getNamespace(), path);
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(spriteId);
    }

    private static void drawSealRing(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Level level,
            SealEntity seal,
            Quaternionfc orientation,
            Vec3 cam,
            float alpha,
            float time,
            boolean seeThrough) {
        BlockPos pos = seal.pos().pos();
        Direction face = seal.pos().face();
        float r;
        float g;
        float b;
        if (seal.color() > 0) {
            int dye = DyeColor.byId(seal.color() - 1).getTextureDiffuseColor();
            r = ARGB32.red(dye) / 255.0F;
            g = ARGB32.green(dye) / 255.0F;
            b = ARGB32.blue(dye) / 255.0F;
        } else {
            r = 0.7F + Mth.sin((time + pos.getX()) / 4.0F) * 0.1F;
            g = 0.7F + Mth.sin((time + pos.getY()) / 5.0F) * 0.1F;
            b = 0.7F + Mth.sin((time + pos.getZ()) / 6.0F) * 0.1F;
        }
        Vec3 anchor = Vec3.atCenterOf(pos);
        Vec3 center = renderPoint(level, anchor, orientation, anchor);
        poseStack.pushPose();
        poseStack.translate(center.x - cam.x, center.y - cam.y, center.z - cam.z);
        poseStack.mulPose(new Quaternionf(orientation).mul(planeFaceRotation(face)));
        poseStack.translate(0.0, 0.0, face.getStepZ() < 0 ? -0.51 : 0.51);
        poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(time), 0.0F, 0.0F, 1.0F));
        drawQuad(
                poseStack,
                buffers.getBuffer(typeFor(AREA_RING, seeThrough)),
                RING_SCALE / 2.0F,
                ARGB32.colorFromFloat(alpha * RING_ALPHA, r, g, b));
        poseStack.popPose();
    }

    private static void drawAreaCorners(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Level level,
            SealEntity seal,
            Quaternionfc orientation,
            Vec3 cam,
            float alpha,
            float time,
            boolean seeThrough) {
        BlockPos pos = seal.pos().pos();
        float r = 0.7F + Mth.sin((time + pos.getX()) / 4.0F) * 0.1F;
        float g = 0.7F + Mth.sin((time + pos.getY()) / 5.0F) * 0.1F;
        float b = 0.7F + Mth.sin((time + pos.getZ()) / 6.0F) * 0.1F;
        AABB area = SealArea.bounds(seal);
        double[][] corners = {
            {area.minX, area.minY, area.minZ},
            {area.minX, area.maxY - 1.0, area.minZ},
            {area.maxX - 1.0, area.minY, area.minZ},
            {area.maxX - 1.0, area.maxY - 1.0, area.minZ},
            {area.maxX - 1.0, area.minY, area.maxZ - 1.0},
            {area.maxX - 1.0, area.maxY - 1.0, area.maxZ - 1.0},
            {area.minX, area.minY, area.maxZ - 1.0},
            {area.minX, area.maxY - 1.0, area.maxZ - 1.0}
        };
        int color = ARGB32.colorFromFloat(alpha * CORNER_ALPHA, r, g, b);
        VertexConsumer buffer = buffers.getBuffer(typeFor(CORNER_FRAME, seeThrough));
        Vec3 anchor = Vec3.atCenterOf(pos);
        for (int q = 0; q < corners.length; q++) {
            Vec3 center = renderPoint(
                    level,
                    anchor,
                    orientation,
                    new Vec3(corners[q][0] + 0.5, corners[q][1] + 0.5, corners[q][2] + 0.5));
            poseStack.pushPose();
            poseStack.translate(center.x - cam.x, center.y - cam.y, center.z - cam.z);
            for (int w = 0; w < ROT_FACES[q].length; w++) {
                Direction cornerFace = ROT_FACES[q][w];
                poseStack.pushPose();
                poseStack.mulPose(new Quaternionf(orientation).mul(planeFaceRotation(cornerFace)));
                poseStack.translate(0.0, 0.0, cornerFace.getStepZ() < 0 ? -0.49 : 0.49);
                poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), 0.0F, 0.0F, -1.0F));
                poseStack.mulPose(
                        new Quaternionf().rotationAxis((float) Math.toRadians(ROT_MAT[q][w]), 0.0F, 0.0F, 1.0F));
                drawQuad(poseStack, buffer, 0.5F, color);
                poseStack.popPose();
            }
            poseStack.popPose();
        }
    }

    private static Quaternionf iconFaceRotation(Direction face) {
        float quarterTurn = (float) (Math.PI / 2.0);
        return switch (face) {
            case DOWN -> new Quaternionf().rotationX(quarterTurn);
            case UP -> new Quaternionf().rotationX(-quarterTurn);
            case NORTH -> new Quaternionf().rotationY((float) Math.PI);
            case EAST -> new Quaternionf().rotationY(quarterTurn);
            case WEST -> new Quaternionf().rotationY(-quarterTurn);
            case SOUTH -> new Quaternionf();
        };
    }

    private static Quaternionf planeFaceRotation(Direction face) {
        return new Quaternionf()
                .rotationAxis((float) Math.toRadians(90.0), -face.getStepY(), face.getStepX(), -face.getStepZ());
    }

    private static Vec3 renderPoint(Level level, Vec3 anchor, Quaternionfc orientation, Vec3 point) {
        Vector3f offset = new Quaternionf(orientation)
                .transform(new Vector3f(
                        (float) (point.x - anchor.x), (float) (point.y - anchor.y), (float) (point.z - anchor.z)));
        return ClientSpaces.renderPosition(level, anchor).add(offset.x, offset.y, offset.z);
    }

    private static void drawQuad(PoseStack poseStack, VertexConsumer buffer, float half, int color) {
        drawQuad(poseStack, buffer, half, color, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    private static void drawQuad(
            PoseStack poseStack, VertexConsumer buffer, float half, int color, float u0, float v0, float u1, float v1) {
        PoseStack.Pose pose = poseStack.last();
        buffer.addVertex(pose, -half, half, 0.0F).setUv(u1, v1).setColor(color);
        buffer.addVertex(pose, half, half, 0.0F).setUv(u1, v0).setColor(color);
        buffer.addVertex(pose, half, -half, 0.0F).setUv(u0, v0).setColor(color);
        buffer.addVertex(pose, -half, -half, 0.0F).setUv(u0, v1).setColor(color);
    }
}
