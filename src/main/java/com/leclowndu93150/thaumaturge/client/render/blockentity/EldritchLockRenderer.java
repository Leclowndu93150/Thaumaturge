package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEntityEldritchLock;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class EldritchLockRenderer implements BlockEntityRenderer<BlockEntityEldritchLock> {
    private static final ResourceLocation CUBE_TEXTURE = TTIds.rl("textures/entity/eldritch_cube.png");
    private static final ResourceLocation LOCK_FACE_SPRITE = TTIds.rl("block/eldritch_lock_face");
    private static final ResourceLocation LOCK_SIDE_SPRITE = TTIds.rl("block/eldritch_lock_side");
    private static final int ARM_COUNT = 4;
    private static final int ARM_STAGGER = 5;
    private static final int CUBE_LIFETIME = 20;
    private static final int MAX_CUBES = 4;
    private static final int LAST_CUBE = 4;
    private static final float PULSE_AMPLITUDE = 0.1F;
    private static final float PULSE_PERIOD = 20.0F;
    private static final float PULSE_CUBE_STEP = 10.0F;
    private static final float PULSE_ARM_STEP = 20.0F;
    private static final float END_CUBE_PULSE_BASE = 0.2F;
    private static final float CUBE_LENGTH = 0.5F;
    private static final float CUBE_OFFSET = 0.25F;
    private static final float CENTER = 0.5F;
    private static final float HALF = 0.5F;
    private static final float TEXTURE_SIZE = 64.0F;
    private static final int[][] FACE_RECTANGLES = {
        {16, 0, 32, 16}, {32, 0, 48, 16}, {0, 16, 16, 32}, {16, 16, 32, 32}, {32, 16, 48, 32}, {48, 16, 64, 32}
    };
    private static final Direction[] FACE_DIRECTIONS = {
        Direction.UP, Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH
    };
    private static final int[][] CORNERS = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
    private static final float[] LOCAL_UP = {0.0F, 1.0F, 0.0F};
    private static final int WHITE = 0xFFFFFFFF;
    private static final float FACE_OFFSET = 0.5F;
    private static final float TABLET_LIFT = 0.025F;
    private static final float PORTAL_LIFT = 0.005F;
    private static final float PORTAL_HALF_SPAN = 2.5F;
    private static final float LOCK_HALF_SPAN = 0.5F;
    private static final float PLATE_HALF_DEPTH = 0.25F;
    private static final float PLATE_DEPTH_UV = 8.0F / 16.0F;
    private static final float[] VERTICAL_UP = {0.0F, 0.0F, -1.0F};
    private static final float[][] PORTAL_PANELS = {
        {-PORTAL_HALF_SPAN, -PORTAL_HALF_SPAN, PORTAL_HALF_SPAN, -LOCK_HALF_SPAN},
        {-PORTAL_HALF_SPAN, LOCK_HALF_SPAN, PORTAL_HALF_SPAN, PORTAL_HALF_SPAN},
        {-PORTAL_HALF_SPAN, -LOCK_HALF_SPAN, -LOCK_HALF_SPAN, LOCK_HALF_SPAN},
        {LOCK_HALF_SPAN, -LOCK_HALF_SPAN, PORTAL_HALF_SPAN, LOCK_HALF_SPAN}
    };
    private static final float TABLET_Y = 0.41F;
    private static final float TABLET_SCALE = 1.025641F;
    private static final float FLIP_DEGREES = 180.0F;
    private static final float NORTH_YAW = 180.0F;
    private static final float WEST_YAW = 270.0F;
    private static final float EAST_YAW = 90.0F;
    private static final float BOUNDS_MARGIN = 3.0F;
    private static final int VIEW_DISTANCE = 64;
    private static final int DISPLAY_SEED = 0;

    private @Nullable ItemStack tabletStack;

    public EldritchLockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityEldritchLock lock,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay) {
        int count = lock.getCount();
        Direction facing = lock.getBlockState().getValue(BlockEldritchLock.FACING);
        int light = lock.getLevel() == null
                ? packedLight
                : LevelRenderer.getLightColor(
                        lock.getLevel(), lock.getBlockPos().relative(facing));
        float time = animationTime(partialTick);
        float[] hub = hub(facing);
        writeArms(
                poseStack.last(),
                buffers.getBuffer(RenderType.entityCutout(CUBE_TEXTURE)),
                hub,
                facing,
                count,
                time,
                light);
        Function<ResourceLocation, TextureAtlasSprite> blocks =
                Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        writePlate(
                poseStack.last(),
                buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)),
                hub,
                facing,
                light,
                blocks.apply(LOCK_FACE_SPRITE),
                blocks.apply(LOCK_SIDE_SPRITE));
        if (count >= 0) {
            float lift = PLATE_HALF_DEPTH + TABLET_LIFT;
            if (tabletStack == null) {
                tabletStack = new ItemStack(TTItems.RUNED_TABLET.get());
            }
            poseStack.pushPose();
            poseStack.translate(
                    hub[0] + lift * facing.getStepX(), TABLET_Y + hub[1] - CENTER, hub[2] + lift * facing.getStepZ());
            poseStack.mulPose(Axis.YP.rotationDegrees(tabletYaw(facing) + FLIP_DEGREES));
            poseStack.scale(TABLET_SCALE, TABLET_SCALE, TABLET_SCALE);
            ItemRenderHelper.render(
                    tabletStack,
                    ItemDisplayContext.FIXED,
                    poseStack,
                    buffers,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    DISPLAY_SEED);
            poseStack.popPose();
        }
        writePortal(
                poseStack.last(), buffers.getBuffer(EldritchPortalSurface.surface()), lock.getBlockPos(), facing, hub);
    }

    private static float animationTime(float partialTick) {
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        return viewer == null ? partialTick : viewer.tickCount + partialTick;
    }

    private static float[] hub(Direction facing) {
        return new float[] {
            CENTER + FACE_OFFSET * facing.getStepX(),
            CENTER + FACE_OFFSET * facing.getStepY(),
            CENTER + FACE_OFFSET * facing.getStepZ()
        };
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityEldritchLock lock) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchLock lock) {
        return new AABB(lock.getBlockPos()).inflate(BOUNDS_MARGIN);
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    private static float tabletYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> NORTH_YAW;
            case WEST -> WEST_YAW;
            case EAST -> EAST_YAW;
            default -> 0.0F;
        };
    }

    private static void writePortal(
            PoseStack.Pose pose, VertexConsumer buffer, BlockPos pos, Direction facing, float[] hub) {
        float px = hub[0] + PORTAL_LIFT * facing.getStepX();
        float py = hub[1] + PORTAL_LIFT * facing.getStepY();
        float pz = hub[2] + PORTAL_LIFT * facing.getStepZ();
        for (float[] panel : PORTAL_PANELS) {
            switch (facing.getAxis()) {
                case Z ->
                    EldritchPortalSurface.quad(
                            pose,
                            buffer,
                            pos,
                            px + panel[0],
                            py + panel[1],
                            pz,
                            px + panel[0],
                            py + panel[3],
                            pz,
                            px + panel[2],
                            py + panel[3],
                            pz,
                            px + panel[2],
                            py + panel[1],
                            pz);
                case X ->
                    EldritchPortalSurface.quad(
                            pose,
                            buffer,
                            pos,
                            px,
                            py + panel[1],
                            pz + panel[0],
                            px,
                            py + panel[3],
                            pz + panel[0],
                            px,
                            py + panel[3],
                            pz + panel[2],
                            px,
                            py + panel[1],
                            pz + panel[2]);
                case Y ->
                    EldritchPortalSurface.quad(
                            pose,
                            buffer,
                            pos,
                            px + panel[0],
                            py,
                            pz + panel[1],
                            px + panel[2],
                            py,
                            pz + panel[1],
                            px + panel[2],
                            py,
                            pz + panel[3],
                            px + panel[0],
                            py,
                            pz + panel[3]);
            }
        }
    }

    private static void writePlate(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float[] hub,
            Direction facing,
            int light,
            TextureAtlasSprite faceSprite,
            TextureAtlasSprite sideSprite) {
        float[] normal = {facing.getStepX(), facing.getStepY(), facing.getStepZ()};
        float[] up = facing.getAxis() == Direction.Axis.Y ? VERTICAL_UP : LOCAL_UP;
        float[] right = cross(up, normal);
        writePlateFace(
                pose,
                buffer,
                offset(hub, normal, PLATE_HALF_DEPTH),
                right,
                LOCK_HALF_SPAN,
                up,
                LOCK_HALF_SPAN,
                normal,
                light,
                faceSprite,
                1.0F,
                1.0F);
        for (int sign = -1; sign <= 1; sign += 2) {
            float[] across = scale(right, sign);
            writePlateFace(
                    pose,
                    buffer,
                    offset(hub, across, LOCK_HALF_SPAN),
                    normal,
                    PLATE_HALF_DEPTH,
                    up,
                    LOCK_HALF_SPAN,
                    across,
                    light,
                    sideSprite,
                    PLATE_DEPTH_UV,
                    1.0F);
            float[] vertical = scale(up, sign);
            writePlateFace(
                    pose,
                    buffer,
                    offset(hub, vertical, LOCK_HALF_SPAN),
                    right,
                    LOCK_HALF_SPAN,
                    normal,
                    PLATE_HALF_DEPTH,
                    vertical,
                    light,
                    sideSprite,
                    1.0F,
                    PLATE_DEPTH_UV);
        }
    }

    private static void writePlateFace(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float[] centre,
            float[] uAxis,
            float uHalf,
            float[] vAxis,
            float vHalf,
            float[] normal,
            int light,
            TextureAtlasSprite sprite,
            float uSpan,
            float vSpan) {
        for (int[] corner : CORNERS) {
            float u = corner[0] * uHalf;
            float v = corner[1] * vHalf;
            buffer.addVertex(
                            pose,
                            centre[0] + uAxis[0] * u + vAxis[0] * v,
                            centre[1] + uAxis[1] * u + vAxis[1] * v,
                            centre[2] + uAxis[2] * u + vAxis[2] * v)
                    .setColor(WHITE)
                    .setUv(sprite.getU((corner[0] + 1) * HALF * uSpan), sprite.getV((1 - corner[1]) * HALF * vSpan))
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, normal[0], normal[1], normal[2]);
        }
    }

    private static float[] cross(float[] a, float[] b) {
        return new float[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    private static float[] offset(float[] origin, float[] direction, float distance) {
        return new float[] {
            origin[0] + direction[0] * distance,
            origin[1] + direction[1] * distance,
            origin[2] + direction[2] * distance
        };
    }

    private static float[] scale(float[] vector, float factor) {
        return new float[] {vector[0] * factor, vector[1] * factor, vector[2] * factor};
    }

    private static void writeArms(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float[] hub,
            Direction facing,
            int count,
            float time,
            int light) {
        float[] axis = {facing.getStepX(), facing.getStepY(), facing.getStepZ()};
        boolean vertical = facing.getAxis() == Direction.Axis.Y;
        for (int index = 0; index < ARM_COUNT; index++) {
            int turns = vertical ? 0 : index;
            int cubes = MAX_CUBES - (count + ARM_STAGGER * index) / CUBE_LIFETIME;
            for (int cube = 1; cube <= cubes; cube++) {
                float pulse = PULSE_AMPLITUDE
                        * Mth.sin((time + PULSE_CUBE_STEP * cube + PULSE_ARM_STEP * index) / PULSE_PERIOD);
                if (cube == 1 || cube == LAST_CUBE) {
                    pulse = pulse / 2.0F + END_CUBE_PULSE_BASE;
                }
                writeCube(pose, buffer, hub, axis, turns, cube, pulse, light);
            }
        }
    }

    private static float[] turn(float[] axis, float[] vector, int turns) {
        float[] result = vector;
        for (int step = 0; step < turns; step++) {
            result = quarterTurn(axis, result);
        }
        return result;
    }

    private static float[] quarterTurn(float[] axis, float[] vector) {
        float dot = axis[0] * vector[0] + axis[1] * vector[1] + axis[2] * vector[2];
        return new float[] {
            axis[1] * vector[2] - axis[2] * vector[1] + axis[0] * dot,
            axis[2] * vector[0] - axis[0] * vector[2] + axis[1] * dot,
            axis[0] * vector[1] - axis[1] * vector[0] + axis[2] * dot
        };
    }

    private static void writeCube(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float[] hub,
            float[] axis,
            int turns,
            int cube,
            float pulse,
            int light) {
        float[] direction = turn(axis, LOCAL_UP, turns);
        float distance = CUBE_OFFSET + CUBE_LENGTH * cube;
        float[] centre = {
            hub[0] + direction[0] * distance, hub[1] + direction[1] * distance, hub[2] + direction[2] * distance
        };
        float[] half = {(CUBE_LENGTH + pulse) * HALF, CUBE_LENGTH * HALF, (CUBE_LENGTH + pulse) * HALF};
        for (int face = 0; face < FACE_DIRECTIONS.length; face++) {
            writeFace(pose, buffer, FACE_DIRECTIONS[face], FACE_RECTANGLES[face], half, centre, axis, turns, light);
        }
    }

    private static void writeFace(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            Direction face,
            int[] rectangle,
            float[] half,
            float[] centre,
            float[] axis,
            int turns,
            int light) {
        float[] normal = {face.getStepX(), face.getStepY(), face.getStepZ()};
        float[] up = face.getAxis() == Direction.Axis.Y ? new float[] {0.0F, 0.0F, 1.0F} : LOCAL_UP;
        float[] right = {
            up[1] * normal[2] - up[2] * normal[1],
            up[2] * normal[0] - up[0] * normal[2],
            up[0] * normal[1] - up[1] * normal[0]
        };
        float[] worldNormal = turn(axis, normal, turns);
        float uMin = rectangle[0] / TEXTURE_SIZE;
        float vMin = rectangle[1] / TEXTURE_SIZE;
        float uMax = rectangle[2] / TEXTURE_SIZE;
        float vMax = rectangle[3] / TEXTURE_SIZE;
        float[] us = {uMin, uMax, uMax, uMin};
        float[] vs = {vMax, vMax, vMin, vMin};
        for (int corner = 0; corner < CORNERS.length; corner++) {
            int r = CORNERS[corner][0];
            int u = CORNERS[corner][1];
            float[] offset = {
                half[0] * (normal[0] + r * right[0] + u * up[0]),
                half[1] * (normal[1] + r * right[1] + u * up[1]),
                half[2] * (normal[2] + r * right[2] + u * up[2])
            };
            float[] world = turn(axis, offset, turns);
            buffer.addVertex(pose, centre[0] + world[0], centre[1] + world[1], centre[2] + world[2])
                    .setColor(WHITE)
                    .setUv(us[corner], vs[corner])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, worldNormal[0], worldNormal[1], worldNormal[2]);
        }
    }
}
