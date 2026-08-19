package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchLock;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class EldritchLockRenderer implements BlockEntityRenderer<BlockEntityEldritchLock, EldritchLockRenderState> {
    private static final Identifier CUBE_TEXTURE = TCIds.rl("textures/entity/eldritch_cube.png");

    private static final int ARMS = 4;
    private static final int ARM_CUBES = 4;
    private static final float ARM_STEP = 0.5F;
    private static final float ARM_BASE_OFFSET = 0.25F;
    private static final float CUBE_SCALE = 0.5F;
    private static final float PULSE_AMPLITUDE = 0.1F;
    private static final float PULSE_PERIOD = 20.0F;
    private static final float END_CUBE_SWELL = 0.2F;
    private static final int RETRACT_TICKS_PER_CUBE = 20;
    private static final int ARM_RETRACT_STAGGER = 5;
    private static final float TABLET_FACE_OFFSET = 0.525F;
    private static final float TABLET_HEIGHT = 0.285F;
    private static final float FLAT_ITEM_LIFT = 0.125F;
    private static final float TABLET_SCALE = 1.0256410F;
    private static final float DOOR_MIN = -2.0F;
    private static final float DOOR_MAX = 3.0F;
    private static final float DOOR_PLANE = 0.5F;

    private final ItemModelResolver itemModelResolver;
    private ItemStack tabletStack = ItemStack.EMPTY;

    public EldritchLockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public EldritchLockRenderState createRenderState() {
        return new EldritchLockRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEldritchLock lock, EldritchLockRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(lock, state, partialTicks, cameraPosition, breakProgress);
        state.count = lock.getCount();
        state.facing = lock.getBlockState().getValue(BlockEldritchLock.FACING);
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        state.animationTime = viewEntity == null ? partialTicks : viewEntity.tickCount + partialTicks;
        if (state.count >= 0) {
            if (tabletStack.isEmpty()) {
                tabletStack = new ItemStack(TCItems.RUNED_TABLET.get());
            }
            ItemStackRenderState itemState = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(itemState, tabletStack, ItemDisplayContext.FIXED, lock.getLevel(), null, 0);
            state.tablet = itemState;
        } else {
            state.tablet = null;
        }
    }

    @Override
    public void submit(EldritchLockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        submitArms(state, poseStack, collector);
        submitTablet(state, poseStack, collector);
        submitDoorFace(state, poseStack, collector);
    }

    private static void submitArms(EldritchLockRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        RenderType type = RenderTypes.entityCutout(CUBE_TEXTURE);
        Direction dir = state.facing;
        Axis armAxis = Axis.of(new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ()));
        for (int u = 0; u < ARMS; u++) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(armAxis.rotationDegrees(90.0F * u));
            int cubes = ARM_CUBES + 1 - (state.count + u * ARM_RETRACT_STAGGER) / RETRACT_TICKS_PER_CUBE;
            for (int a = 1; a < cubes; a++) {
                poseStack.pushPose();
                poseStack.translate(0.0F, ARM_BASE_OFFSET + ARM_STEP * a, 0.0F);
                float w = Mth.sin((state.animationTime + a * 10 + u * 20) / PULSE_PERIOD) * PULSE_AMPLITUDE;
                if (a == 1 || a == ARM_CUBES) {
                    w = w / 2.0F + END_CUBE_SWELL;
                }
                poseStack.scale(CUBE_SCALE + w, CUBE_SCALE, CUBE_SCALE + w);
                collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> cube(pose, buffer, state.lightCoords));
                poseStack.popPose();
            }
            poseStack.popPose();
        }
    }

    private void submitTablet(EldritchLockRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (state.tablet == null) {
            return;
        }
        Direction dir = state.facing;
        poseStack.pushPose();
        poseStack.translate(0.5F + dir.getStepX() * TABLET_FACE_OFFSET, TABLET_HEIGHT, 0.5F + dir.getStepZ() * TABLET_FACE_OFFSET);
        float yRot = switch (dir) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.translate(0.0F, FLAT_ITEM_LIFT, 0.0F);
        poseStack.scale(TABLET_SCALE, TABLET_SCALE, TABLET_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        state.tablet.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static void submitDoorFace(EldritchLockRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        boolean zAxis = state.facing.getAxis() == Direction.Axis.Z;
        collector.submitCustomGeometry(poseStack, EldritchPortalSurface.SURFACE, (pose, buffer) -> {
            if (zAxis) {
                EldritchPortalSurface.quad(pose, buffer, state.blockPos, DOOR_MIN, DOOR_MIN, DOOR_PLANE, DOOR_MIN, DOOR_MAX, DOOR_PLANE, DOOR_MAX, DOOR_MAX, DOOR_PLANE, DOOR_MAX, DOOR_MIN,
                        DOOR_PLANE);
            } else {
                EldritchPortalSurface.quad(pose, buffer, state.blockPos, DOOR_PLANE, DOOR_MIN, DOOR_MIN, DOOR_PLANE, DOOR_MAX, DOOR_MIN, DOOR_PLANE, DOOR_MAX, DOOR_MAX, DOOR_PLANE, DOOR_MIN,
                        DOOR_MAX);
            }
        });
    }

    private static void cube(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        for (Direction dir : Direction.values()) {
            cubeFace(pose, buffer, dir, light);
        }
    }

    private static void cubeFace(PoseStack.Pose pose, VertexConsumer buffer, Direction dir, int light) {
        Vector3f normal = new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ());
        Vector3f up = dir.getAxis() == Direction.Axis.Y ? new Vector3f(0.0F, 0.0F, 1.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f right = new Vector3f(up).cross(normal);
        float[] uv = faceUV(dir);
        cubeVertex(pose, buffer, normal, right, up, -1.0F, -1.0F, uv[0], uv[3], light);
        cubeVertex(pose, buffer, normal, right, up, 1.0F, -1.0F, uv[2], uv[3], light);
        cubeVertex(pose, buffer, normal, right, up, 1.0F, 1.0F, uv[2], uv[1], light);
        cubeVertex(pose, buffer, normal, right, up, -1.0F, 1.0F, uv[0], uv[1], light);
    }

    private static float[] faceUV(Direction dir) {
        float tex = 64.0F;
        int[] px = switch (dir) {
            case UP -> new int[]{16, 0, 32, 16};
            case DOWN -> new int[]{32, 0, 48, 16};
            case WEST -> new int[]{0, 16, 16, 32};
            case NORTH -> new int[]{16, 16, 32, 32};
            case EAST -> new int[]{32, 16, 48, 32};
            case SOUTH -> new int[]{48, 16, 64, 32};
        };
        return new float[]{px[0] / tex, px[1] / tex, px[2] / tex, px[3] / tex};
    }

    private static void cubeVertex(PoseStack.Pose pose, VertexConsumer buffer, Vector3f normal, Vector3f right, Vector3f up, float r, float u, float texU, float texV, int light) {
        float half = 0.5F;
        float x = (normal.x + right.x * r + up.x * u) * half;
        float y = (normal.y + right.y * r + up.y * u) * half;
        float z = (normal.z + right.z * r + up.z * u) * half;
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(texU, texV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, normal.x, normal.y, normal.z);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchLock lock) {
        return new AABB(lock.getBlockPos()).inflate(2.5);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
