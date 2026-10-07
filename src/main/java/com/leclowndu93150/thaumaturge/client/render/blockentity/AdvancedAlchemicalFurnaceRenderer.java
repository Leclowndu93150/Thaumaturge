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

    private static final int VENT_FIRE_LIGHT = LightTexture.pack(14, 0);
    private static final int VENT_BACKING_LIGHT = LightTexture.pack(9, 0);
    private static final int TANK_GOO_LIGHT = LightTexture.pack(12, 0);

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
        if (!furnace.assembled()) {
            return;
        }

        boolean hot = furnace.heat() > 100;
        boolean charged = !furnace.aspects().isEmpty();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        renderMesh(hot, charged, poseStack, buffers, light, furnace);
        if (charged) {
            renderStoredEssentia(furnace.aspects().totalAmount(), poseStack, buffers);
        }
        if (hot) {
            renderHeatVents(furnace.heat(), poseStack, buffers);
        }
        poseStack.popPose();
    }

    /** Fills the mesh's four sloped vent openings with animated fire. */
    private static void renderHeatVents(int heat, PoseStack poseStack, MultiBufferSource buffers) {
        TextureAtlasSprite fire = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/fire_0"));
        TextureAtlasSprite backing = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TTIds.rl("block/base_metal"));
        VertexConsumer fireBuffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        VertexConsumer backingBuffer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        float base = 1.0F - Math.min(1.0F, heat / (float) BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
        for (int rotation = 0; rotation < 4; rotation++) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 1.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(135.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.translate(-0.5F, 0.0F, -1.0F);
            spriteQuad(fireBuffer, poseStack.last(), fire, base, VENT_FIRE_LIGHT);
            spriteQuadBackface(fireBuffer, poseStack.last(), fire, base, VENT_FIRE_LIGHT);
            poseStack.translate(0.0F, 0.0F, 0.05F);
            spriteQuad(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            spriteQuadBackface(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            poseStack.popPose();
        }
    }

    /** Renders stored essentia behind the tank windows. */
    private static void renderStoredEssentia(int stored, PoseStack poseStack, MultiBufferSource buffers) {
        TextureAtlasSprite goo = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TTIds.rl("block/flux_goo"));
        TextureAtlasSprite backing = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TTIds.rl("block/base_metal"));
        VertexConsumer gooBuffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        VertexConsumer backingBuffer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        float fillBase = 1.0F - Math.min(1.0F, stored / (float) BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA);

        // Liquid surface visible in the central opening.
        poseStack.pushPose();
        poseStack.translate(0.5F, -0.5F, 1.1F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        spriteQuad(gooBuffer, poseStack.last(), goo, 0.0F, TANK_GOO_LIGHT);
        spriteQuadBackface(gooBuffer, poseStack.last(), goo, 0.0F, TANK_GOO_LIGHT);
        poseStack.popPose();

        // Each corner tank has two window faces. The on-texture intentionally leaves
        // these slits transparent so this backing + animated fill can be seen through them.
        for (int rotation = 0; rotation < 4; rotation++) {
            poseStack.pushPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * rotation));
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            poseStack.translate(0.85F, -1.8F, -1.4F);
            poseStack.scale(0.3F, 0.6F, 1.0F);
            spriteQuad(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            spriteQuadBackface(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            poseStack.translate(0.0F, 0.0F, -0.01F);
            spriteQuad(gooBuffer, poseStack.last(), goo, fillBase, TANK_GOO_LIGHT);
            spriteQuadBackface(gooBuffer, poseStack.last(), goo, fillBase, TANK_GOO_LIGHT);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F * rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.translate(1.15F, 1.8F, -1.4F);
            poseStack.scale(-0.3F, -0.6F, -1.0F);
            spriteQuad(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            spriteQuadBackface(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            poseStack.translate(0.0F, 0.0F, 0.01F);
            spriteQuad(gooBuffer, poseStack.last(), goo, fillBase, TANK_GOO_LIGHT);
            spriteQuadBackface(gooBuffer, poseStack.last(), goo, fillBase, TANK_GOO_LIGHT);
            poseStack.popPose();

            poseStack.popPose();
        }
    }

    private static void spriteQuad(
            VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, float base, int light) {
        vertex(buffer, pose, 0.0F, 1.0F, sprite.getU0(), sprite.getV0(), light, 1.0F);
        vertex(buffer, pose, 1.0F, 1.0F, sprite.getU1(), sprite.getV0(), light, 1.0F);
        vertex(buffer, pose, 1.0F, base, sprite.getU1(), sprite.getV1(), light, 1.0F);
        vertex(buffer, pose, 0.0F, base, sprite.getU0(), sprite.getV1(), light, 1.0F);
    }

    private static void spriteQuadBackface(
            VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, float base, int light) {
        vertex(buffer, pose, 0.0F, 1.0F, sprite.getU0(), sprite.getV0(), light, -1.0F);
        vertex(buffer, pose, 0.0F, base, sprite.getU0(), sprite.getV1(), light, -1.0F);
        vertex(buffer, pose, 1.0F, base, sprite.getU1(), sprite.getV1(), light, -1.0F);
        vertex(buffer, pose, 1.0F, 1.0F, sprite.getU1(), sprite.getV0(), light, -1.0F);
    }

    private static void vertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light, float normalZ) {
        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, normalZ);
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
            normal.mulDirection(worldTransform).normalize();
            // Sample just outside each surface, rather than inside the enclosed controller cell.
            center.fma(0.01F, normal);
            int light =
                    LevelRenderer.getLightColor(furnace.getLevel(), BlockPos.containing(center.x, center.y, center.z));
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
        return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityAdvancedAlchemicalFurnace furnace) {
        return true;
    }
}
