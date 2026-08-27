package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.items.IScribeTools;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ResearchTableModel;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class ResearchTableRenderer implements BlockEntityRenderer<BlockEntityResearchTable> {
    private static final ResourceLocation TABLE_TEXTURE = TCIds.rl("textures/entity/restable.png");
    private static final ResourceLocation SCROLL_TEXTURE = TCIds.rl("textures/entity/restable2.png");
    private static final ResourceLocation QUILL_TEXTURE = TCIds.rl("textures/entity/tablequill.png");
    private static final ResourceLocation PARCHMENT_TEXTURE = TCIds.rl("textures/misc/parchment.png");

    private static final int DEFAULT_SCROLL_COLOR = 0x999999;
    private static final float QUILL_THICKNESS = 0.025F;
    private static final int QUILL_TEXELS = 16;
    private static final int PARCHMENT_COUNT = 6;
    private static final float RIBBON_SCALE = 1.2F;

    private final ResearchTableModel model;

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ResearchTableModel(context.bakeLayer(TCModelLayers.RESEARCH_TABLE));
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityResearchTable table) {
        return new AABB(table.getBlockPos()).inflate(1.0);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityResearchTable table) {
        return true;
    }

    @Override
    public void render(
            BlockEntityResearchTable table,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        Direction facing = table.getBlockState().hasProperty(BlockResearchTable.FACING)
                ? table.getBlockState().getValue(BlockResearchTable.FACING)
                : Direction.NORTH;
        boolean hasTools = table.items()
                        .getStackInSlot(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS)
                        .getItem()
                instanceof IScribeTools;
        ItemStack note = table.items().getStackInSlot(BlockEntityResearchTable.SLOT_NOTE);
        ResearchNoteData data = ResearchNotes.dataOf(note);
        boolean hasNote = !note.isEmpty() && data != null;
        int noteColor = data != null ? data.color() : DEFAULT_SCROLL_COLOR;

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        float yaw =
                switch (facing) {
                    case NORTH -> 270.0F;
                    case SOUTH -> 90.0F;
                    case WEST -> 180.0F;
                    default -> 0.0F;
                };
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        model.table.render(
                poseStack, buffers.getBuffer(RenderType.entityCutout(TABLE_TEXTURE)), light, OverlayTexture.NO_OVERLAY);

        if (hasTools) {
            model.inkwell.render(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutout(TABLE_TEXTURE)),
                    light,
                    OverlayTexture.NO_OVERLAY);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(-0.17F, 0.1F, -0.15F);
            poseStack.mulPose(Axis.YP.rotationDegrees(15.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            itemIn2D(poseStack.last(), buffers.getBuffer(RenderType.entityCutout(QUILL_TEXTURE)), light);
            poseStack.popPose();
        }

        for (int a = 0; a < PARCHMENT_COUNT; a++) {
            poseStack.pushPose();
            poseStack.translate(0.1F, -0.01F - a * 0.015F, 0.35F);
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(15 + a % 3 * 2));
            poseStack.scale(0.5F, 0.6F, 0.6F);
            parchmentQuad(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(PARCHMENT_TEXTURE)), light);
            poseStack.popPose();
        }

        if (hasNote) {
            model.scrollTube.render(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutout(SCROLL_TEXTURE)),
                    light,
                    OverlayTexture.NO_OVERLAY);
            poseStack.pushPose();
            poseStack.scale(RIBBON_SCALE, RIBBON_SCALE, RIBBON_SCALE);
            model.scrollRibbon.render(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutout(SCROLL_TEXTURE)),
                    light,
                    OverlayTexture.NO_OVERLAY,
                    0xFF000000 | noteColor);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void parchmentQuad(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        vertexLit(pose, buffer, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        vertexLit(pose, buffer, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        vertexLit(pose, buffer, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        vertexLit(pose, buffer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
    }

    private static void itemIn2D(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        float t = QUILL_THICKNESS;
        int texels = QUILL_TEXELS;
        vertexLit(pose, buffer, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, light);
        vertexLit(pose, buffer, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, light);
        vertexLit(pose, buffer, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, light);
        vertexLit(pose, buffer, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, light);
        vertexLit(pose, buffer, 0.0F, 1.0F, -t, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        vertexLit(pose, buffer, 1.0F, 1.0F, -t, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        vertexLit(pose, buffer, 1.0F, 0.0F, -t, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        vertexLit(pose, buffer, 0.0F, 0.0F, -t, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        float half = 0.5F / texels;
        for (int k = 0; k < texels; k++) {
            float x = k / (float) texels;
            float u = 1.0F - x - half;
            vertexLit(pose, buffer, x, 0.0F, -t, u, 1.0F, -1.0F, 0.0F, 0.0F, light);
            vertexLit(pose, buffer, x, 0.0F, 0.0F, u, 1.0F, -1.0F, 0.0F, 0.0F, light);
            vertexLit(pose, buffer, x, 1.0F, 0.0F, u, 0.0F, -1.0F, 0.0F, 0.0F, light);
            vertexLit(pose, buffer, x, 1.0F, -t, u, 0.0F, -1.0F, 0.0F, 0.0F, light);
        }
        for (int k = 0; k < texels; k++) {
            float x = k / (float) texels;
            float u = 1.0F - x - half;
            float x1 = x + 1.0F / texels;
            vertexLit(pose, buffer, x1, 1.0F, -t, u, 0.0F, 1.0F, 0.0F, 0.0F, light);
            vertexLit(pose, buffer, x1, 1.0F, 0.0F, u, 0.0F, 1.0F, 0.0F, 0.0F, light);
            vertexLit(pose, buffer, x1, 0.0F, 0.0F, u, 1.0F, 1.0F, 0.0F, 0.0F, light);
            vertexLit(pose, buffer, x1, 0.0F, -t, u, 1.0F, 1.0F, 0.0F, 0.0F, light);
        }
        for (int k = 0; k < texels; k++) {
            float y = k / (float) texels;
            float v = 1.0F - y - half;
            float y1 = y + 1.0F / texels;
            vertexLit(pose, buffer, 0.0F, y1, 0.0F, 1.0F, v, 0.0F, 1.0F, 0.0F, light);
            vertexLit(pose, buffer, 1.0F, y1, 0.0F, 0.0F, v, 0.0F, 1.0F, 0.0F, light);
            vertexLit(pose, buffer, 1.0F, y1, -t, 0.0F, v, 0.0F, 1.0F, 0.0F, light);
            vertexLit(pose, buffer, 0.0F, y1, -t, 1.0F, v, 0.0F, 1.0F, 0.0F, light);
        }
        for (int k = 0; k < texels; k++) {
            float y = k / (float) texels;
            float v = 1.0F - y - half;
            vertexLit(pose, buffer, 1.0F, y, 0.0F, 0.0F, v, 0.0F, -1.0F, 0.0F, light);
            vertexLit(pose, buffer, 0.0F, y, 0.0F, 1.0F, v, 0.0F, -1.0F, 0.0F, light);
            vertexLit(pose, buffer, 0.0F, y, -t, 1.0F, v, 0.0F, -1.0F, 0.0F, light);
            vertexLit(pose, buffer, 1.0F, y, -t, 0.0F, v, 0.0F, -1.0F, 0.0F, light);
        }
    }

    private static void vertexLit(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float x,
            float y,
            float z,
            float u,
            float v,
            float nx,
            float ny,
            float nz,
            int light) {
        buffer.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
