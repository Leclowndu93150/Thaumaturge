package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/** Renders the Advanced Alchemical Furnace model around its modern controller. */
public final class AdvancedAlchemicalFurnaceRenderer
        implements BlockEntityRenderer<BlockEntityAdvancedAlchemicalFurnace> {
    private static final ResourceLocation MODEL = TTIds.rl("models/mesh/advanced_alchemical_furnace.ttmesh");
    private static final RenderType BASE =
            RenderType.entityCutout(TTIds.rl("textures/block/advanced_alchemical_furnace.png"));
    private static final RenderType BASE_HOT =
            RenderType.entityCutout(TTIds.rl("textures/block/advanced_alchemical_furnace_on.png"));
    private static final RenderType TANK =
            RenderType.entityCutout(TTIds.rl("textures/block/advanced_alchemical_furnace_tank.png"));
    private static final RenderType TANK_FILLED =
            RenderType.entityCutout(TTIds.rl("textures/block/advanced_alchemical_furnace_tank_on.png"));
    private static final RenderType TANK_TRIM = RenderType.entityCutout(TTIds.rl("textures/block/metal_thaumium.png"));
    private static final String PART_BASE = "Base";
    private static final String PART_TANK = "Tank";
    private static final String PART_TANK_TRIM = "TankTrim";
    private static final int WHITE = 0xFFFFFFFF;
    private static final int SIDES = 4;
    private static final float SIDE_ANGLE = 90.0F;

    private static final ResourceLocation GOO_SPRITE = TTIds.rl("block/flux_goo");
    private static final ResourceLocation BACKING_SPRITE = TTIds.rl("block/base_metal");
    private static final ResourceLocation FLAME_SPRITE = ResourceLocation.withDefaultNamespace("block/fire_0");
    private static final float CENTER = 0.5F;
    private static final float NORMAL_OFFSET = 0.01F;
    private static final int GOO_BLOCK_LIGHT = 12;
    private static final int BACKING_BLOCK_LIGHT = 9;
    private static final int FLAME_BLOCK_LIGHT = 15;
    private static final float BACKING_LIFT = 0.003F;
    private static final float FRONT_LIFT = 0.006F;
    private static final float GOO_SURFACE_Y = 1.9375F;
    private static final float GOO_SURFACE_HALF = 0.375F;
    private static final float WINDOW_FACE_X = 1.875F;
    private static final float WINDOW_BOTTOM = 1.1875F;
    private static final float WINDOW_HEIGHT = 0.5F;
    private static final float WINDOW_SPAN = 0.25F;
    private static final float WINDOW_A_Z = -0.375F;
    private static final float WINDOW_B_Z = 1.625F;
    private static final float VENT_FACE_X = 1.9375F;
    private static final float VENT_BOTTOM = 0.4375F;
    private static final float VENT_HEIGHT = 0.25F;
    private static final float VENT_SPAN = 0.25F;
    private static final float VENT_LEFT_Z = 0.625F;
    private static final float FULL_SHARE = 1.0F;
    private static final int GOO_LIGHT = LightTexture.pack(GOO_BLOCK_LIGHT, 0);
    private static final int BACKING_LIGHT = LightTexture.pack(BACKING_BLOCK_LIGHT, 0);
    private static final int FLAME_LIGHT = LightTexture.pack(FLAME_BLOCK_LIGHT, 0);
    private static final float PANEL_FULL = 0.0F;
    private static final float BOUNDS_MARGIN_LOW = 1.0F;
    private static final float BOUNDS_MARGIN_HIGH = 2.0F;
    private static final float BOUNDS_HEIGHT = 2.0F;
    private static final PanelPlane GOO_SURFACE = new PanelPlane(
            CENTER - GOO_SURFACE_HALF,
            GOO_SURFACE_Y,
            CENTER - GOO_SURFACE_HALF,
            GOO_SURFACE_HALF * 2.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            GOO_SURFACE_HALF * 2.0F);
    private static final PanelPlane WINDOW_A_BACKING = windowPlane(WINDOW_A_Z, BACKING_LIFT, FULL_SHARE);
    private static final PanelPlane WINDOW_B_BACKING = windowPlane(WINDOW_B_Z, BACKING_LIFT, FULL_SHARE);
    private static final PanelPlane FLAME_BACKING = ventPlane(BACKING_LIFT, FULL_SHARE);

    public AdvancedAlchemicalFurnaceRenderer(BlockEntityRendererProvider.Context context) {}

    /** Renders the inactive complete furnace for inventory and recipe-viewer previews. */
    public static void renderPreview(
            BlockState state, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        renderMesh(false, false, poseStack, buffers, light, null);
    }

    @Override
    public void render(
            BlockEntityAdvancedAlchemicalFurnace furnace,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (!furnace.isAssembled()) {
            return;
        }

        boolean hot = furnace.heat() > BlockEntityAdvancedAlchemicalFurnace.HOT_HEAT;
        boolean charged = !furnace.aspects().isEmpty();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        renderMesh(hot, charged, poseStack, buffers, light, furnace);
        poseStack.popPose();
        if (charged) {
            renderStoredEssentia(furnace.aspects().totalAmount(), poseStack, buffers);
        }
        if (hot) {
            renderHeatVents(furnace.heat(), poseStack, buffers);
        }
    }

    private static void renderStoredEssentia(int stored, PoseStack poseStack, MultiBufferSource buffers) {
        float share = Mth.clamp((float) stored / BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA, 0.0F, FULL_SHARE);
        RenderType goo = Sheets.translucentItemSheet();
        RenderType backing = Sheets.cutoutBlockSheet();
        renderPanel(poseStack, buffers, goo, GOO_SPRITE, GOO_LIGHT, GOO_SURFACE, PANEL_FULL);
        PanelPlane gooA = windowPlane(WINDOW_A_Z, FRONT_LIFT, share);
        PanelPlane gooB = windowPlane(WINDOW_B_Z, FRONT_LIFT, share);
        for (int side = 0; side < SIDES; side++) {
            poseStack.pushPose();
            turnAboutCenter(poseStack, side);
            renderPanel(poseStack, buffers, backing, BACKING_SPRITE, BACKING_LIGHT, WINDOW_A_BACKING, PANEL_FULL);
            renderPanel(poseStack, buffers, backing, BACKING_SPRITE, BACKING_LIGHT, WINDOW_B_BACKING, PANEL_FULL);
            if (share > 0.0F) {
                renderPanel(poseStack, buffers, goo, GOO_SPRITE, GOO_LIGHT, gooA, PANEL_FULL);
                renderPanel(poseStack, buffers, goo, GOO_SPRITE, GOO_LIGHT, gooB, PANEL_FULL);
            }
            poseStack.popPose();
        }
    }

    private static void renderHeatVents(int heat, PoseStack poseStack, MultiBufferSource buffers) {
        float share = Mth.clamp((float) heat / BlockEntityAdvancedAlchemicalFurnace.MAX_POWER, 0.0F, FULL_SHARE);
        RenderType cutout = Sheets.cutoutBlockSheet();
        PanelPlane flame = ventPlane(FRONT_LIFT, share);
        for (int side = 0; side < SIDES; side++) {
            poseStack.pushPose();
            turnAboutCenter(poseStack, side);
            renderPanel(poseStack, buffers, cutout, BACKING_SPRITE, BACKING_LIGHT, FLAME_BACKING, PANEL_FULL);
            if (share > 0.0F) {
                renderPanel(poseStack, buffers, cutout, FLAME_SPRITE, FLAME_LIGHT, flame, PANEL_FULL);
            }
            poseStack.popPose();
        }
    }

    static PanelPlane windowPlane(float leftZ, float lift, float share) {
        return risingPlane(WINDOW_FACE_X + lift, WINDOW_BOTTOM, leftZ, WINDOW_SPAN, WINDOW_HEIGHT * share);
    }

    static PanelPlane ventPlane(float lift, float share) {
        return risingPlane(VENT_FACE_X + lift, VENT_BOTTOM, VENT_LEFT_Z, VENT_SPAN, VENT_HEIGHT * share);
    }

    private static PanelPlane risingPlane(float faceX, float bottomY, float leftZ, float span, float height) {
        return new PanelPlane(faceX, bottomY, leftZ, 0.0F, 0.0F, -span, 0.0F, height, 0.0F);
    }

    static void turnAboutCenter(PoseStack poseStack, int side) {
        poseStack.translate(CENTER, 0.0F, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * SIDE_ANGLE));
        poseStack.translate(-CENTER, 0.0F, -CENTER);
    }

    private static void renderPanel(
            PoseStack poseStack,
            MultiBufferSource buffers,
            RenderType sheet,
            ResourceLocation spriteId,
            int light,
            PanelPlane plane,
            float bottom) {
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(spriteId);
        writePanel(sprite.wrap(buffers.getBuffer(sheet)), poseStack.last(), plane, bottom, light);
    }

    static void writePanel(VertexConsumer buffer, PoseStack.Pose pose, PanelPlane plane, float bottom, int light) {
        Vector3f normal = plane.normal();
        float nx = normal.x;
        float ny = normal.y;
        float nz = normal.z;
        panelVertex(buffer, pose, plane, 0.0F, bottom, 0.0F, 1.0F, light, nx, ny, nz);
        panelVertex(buffer, pose, plane, 1.0F, bottom, 1.0F, 1.0F, light, nx, ny, nz);
        panelVertex(buffer, pose, plane, 1.0F, 1.0F, 1.0F, 0.0F, light, nx, ny, nz);
        panelVertex(buffer, pose, plane, 0.0F, 1.0F, 0.0F, 0.0F, light, nx, ny, nz);
        panelVertex(buffer, pose, plane, 0.0F, 1.0F, 0.0F, 0.0F, light, -nx, -ny, -nz);
        panelVertex(buffer, pose, plane, 1.0F, 1.0F, 1.0F, 0.0F, light, -nx, -ny, -nz);
        panelVertex(buffer, pose, plane, 1.0F, bottom, 1.0F, 1.0F, light, -nx, -ny, -nz);
        panelVertex(buffer, pose, plane, 0.0F, bottom, 0.0F, 1.0F, light, -nx, -ny, -nz);
    }

    private static void panelVertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            PanelPlane plane,
            float px,
            float py,
            float u,
            float v,
            int light,
            float nx,
            float ny,
            float nz) {
        buffer.addVertex(pose, plane.x(px, py), plane.y(px, py), plane.z(px, py))
                .setUv(u, v)
                .setColor(WHITE)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    record PanelPlane(
            float originX,
            float originY,
            float originZ,
            float rightX,
            float rightY,
            float rightZ,
            float downX,
            float downY,
            float downZ) {
        float x(float px, float py) {
            return originX + px * rightX + py * downX;
        }

        float y(float px, float py) {
            return originY + px * rightY + py * downY;
        }

        float z(float px, float py) {
            return originZ + px * rightZ + py * downZ;
        }

        Vector3f normal() {
            Vector3f normal = new Vector3f(rightX, rightY, rightZ).cross(downX, downY, downZ);
            return normal.lengthSquared() > 0.0F ? normal.normalize() : normal.set(0.0F, 1.0F, 0.0F);
        }
    }

    private static void renderMesh(
            boolean hot,
            boolean filled,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            @Nullable BlockEntityAdvancedAlchemicalFurnace furnace) {
        for (TTMeshPart part : GolemMeshes.get(MODEL).parts()) {
            if (PART_BASE.equals(part.name())) {
                renderPart(part, pose.last(), buffers.getBuffer(hot ? BASE_HOT : BASE), light, furnace, 0);
            } else if (PART_TANK.equals(part.name())) {
                renderTankPart(part, filled ? TANK_FILLED : TANK, pose, buffers, light, furnace);
            } else if (PART_TANK_TRIM.equals(part.name())) {
                renderTankPart(part, TANK_TRIM, pose, buffers, light, furnace);
            }
        }
    }

    private static void renderTankPart(
            TTMeshPart part,
            RenderType type,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            @Nullable BlockEntityAdvancedAlchemicalFurnace furnace) {
        for (int side = 0; side < SIDES; side++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(SIDE_ANGLE * side));
            renderPart(part, pose.last(), buffers.getBuffer(type), light, furnace, side);
            pose.popPose();
        }
    }

    private static void renderPart(
            TTMeshPart part,
            PoseStack.Pose pose,
            VertexConsumer buffer,
            int previewLight,
            @Nullable BlockEntityAdvancedAlchemicalFurnace furnace,
            int side) {
        if (furnace == null || furnace.getLevel() == null) {
            GolemMeshes.renderPart(part, pose, buffer, previewLight, WHITE);
            return;
        }
        BlockPos origin = furnace.getBlockPos();
        Matrix4f worldTransform = new Matrix4f()
                .translate(origin.getX() + 0.5F, origin.getY(), origin.getZ() + 0.5F)
                .rotateX(-Mth.HALF_PI)
                .rotateZ(side * Mth.HALF_PI);
        float[] positions = part.positions();
        float[] normals = part.normals();
        float[] uvs = part.uvs();
        Vector3f center = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int quad = 0; quad < part.quadCount(); quad++) {
            center.zero();
            normal.zero();
            for (int corner = 0; corner < 4; corner++) {
                int index = (quad * 4 + corner) * 3;
                center.add(positions[index], positions[index + 1], positions[index + 2]);
                normal.add(normals[index], normals[index + 1], normals[index + 2]);
            }
            center.mul(0.25F).mulPosition(worldTransform);
            normal.mulDirection(worldTransform);
            if (normal.lengthSquared() > 0.0F) {
                normal.normalize();
            }
            // Sample just outside each surface, rather than inside the enclosed controller cell.
            center.fma(NORMAL_OFFSET, normal);
            BlockPos lightPos = BlockPos.containing(center.x, center.y, center.z);
            if (normal.y > 0.5F
                    && lightPos.equals(origin.above())
                    && furnace.getLevel().getBlockState(lightPos).isSolidRender(furnace.getLevel(), lightPos)) {
                lightPos = lightPos.above();
            }
            int light = LevelRenderer.getLightColor(furnace.getLevel(), lightPos);
            for (int corner = 0; corner < 4; corner++) {
                int vertex = quad * 4 + corner;
                int index = vertex * 3;
                buffer.addVertex(pose, positions[index], positions[index + 1], positions[index + 2])
                        .setColor(WHITE)
                        .setUv(uvs == null ? 0.0F : uvs[vertex * 2], uvs == null ? 0.0F : 1.0F - uvs[vertex * 2 + 1])
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, normals[index], normals[index + 1], normals[index + 2]);
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityAdvancedAlchemicalFurnace furnace) {
        var pos = furnace.getBlockPos();
        return new AABB(
                pos.getX() - BOUNDS_MARGIN_LOW,
                pos.getY(),
                pos.getZ() - BOUNDS_MARGIN_LOW,
                pos.getX() + BOUNDS_MARGIN_HIGH,
                pos.getY() + BOUNDS_HEIGHT,
                pos.getZ() + BOUNDS_MARGIN_HIGH);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityAdvancedAlchemicalFurnace furnace) {
        return true;
    }
}
