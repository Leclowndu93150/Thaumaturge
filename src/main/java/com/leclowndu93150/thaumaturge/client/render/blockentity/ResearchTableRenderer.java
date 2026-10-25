package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.entity.ResearchTableModel;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ResearchTableRenderer implements BlockEntityRenderer<BlockEntityResearchTable> {
    private static final ResourceLocation INKWELL_MESH = TTIds.rl("models/mesh/research_inkwell.ttmesh");
    private static final ResourceLocation INKWELL_TEXTURE = TTIds.rl("textures/entity/research_inkwell.png");
    private static final ResourceLocation TABLE_TEXTURE = TTIds.rl("textures/entity/restable2.png");
    private static final ResourceLocation QUILL_TEXTURE = TTIds.rl("textures/entity/tablequill.png");

    private static final int DEFAULT_SCROLL_COLOR = 0x999999;
    private static final float BOUNDS_MARGIN = 0.5F;
    private static final float INKWELL_X = -0.28125F;
    private static final float INKWELL_Z = 0.28125F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final float HALF_TURN = 180.0F;
    private static final float QUILL_X = -0.226F;
    private static final float QUILL_Y = -0.095F;
    private static final float QUILL_Z = 0.076F;
    private static final float QUILL_YAW = -105.0F;
    private static final float QUILL_SCALE = 0.5F;
    private static final float QUILL_THICKNESS = 0.025F;
    private static final int QUILL_TEXELS = 16;
    private static final float RIBBON_SCALE = 1.2F;

    private final ResearchTableModel model;

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ResearchTableModel(context.bakeLayer(TTModelLayers.RESEARCH_TABLE));
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityResearchTable table) {
        BlockPos pos = table.getBlockPos();
        BlockState state = table.getBlockState();
        Direction facing = state.hasProperty(BlockResearchTable.FACING)
                ? state.getValue(BlockResearchTable.FACING)
                : Direction.NORTH;
        return renderBounds(pos, facing);
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
                .is(TTItemTags.SCRIBING_TOOLS);
        ItemStack note = table.items().getStackInSlot(BlockEntityResearchTable.SLOT_NOTE);
        ResearchNoteData data = ResearchNotes.dataOf(note);
        boolean hasNote = !note.isEmpty() && data != null;
        int noteColor = data != null ? data.color() : DEFAULT_SCROLL_COLOR;

        poseStack.pushPose();
        ResearchTableTop.pose(poseStack, facing);

        if (hasTools) {
            renderInkwell(poseStack, buffers, light);
            poseStack.pushPose();
            applyQuillPose(poseStack);
            itemIn2D(poseStack.last(), buffers.getBuffer(RenderType.entityCutout(QUILL_TEXTURE)), light);
            poseStack.popPose();
        }

        ResearchTableTop.renderSheets(poseStack, buffers, light);

        if (hasNote) {
            model.scrollTube.render(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutout(TABLE_TEXTURE)),
                    light,
                    OverlayTexture.NO_OVERLAY);
            poseStack.pushPose();
            poseStack.scale(RIBBON_SCALE, RIBBON_SCALE, RIBBON_SCALE);
            model.scrollRibbon.render(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutout(TABLE_TEXTURE)),
                    light,
                    OverlayTexture.NO_OVERLAY,
                    0xFF000000 | noteColor);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    static void applyQuillPose(PoseStack poseStack) {
        poseStack.translate(QUILL_X, QUILL_Y, QUILL_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(QUILL_YAW));
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        poseStack.scale(QUILL_SCALE, QUILL_SCALE, QUILL_SCALE);
    }

    static AABB renderBounds(BlockPos pos, Direction facing) {
        return new AABB(pos).minmax(new AABB(pos.relative(facing)).inflate(BOUNDS_MARGIN));
    }

    private static void renderInkwell(PoseStack poseStack, MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        poseStack.translate(INKWELL_X, 0.0F, INKWELL_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(INKWELL_TEXTURE));
        for (TTMeshPart part : GolemMeshes.get(INKWELL_MESH).parts()) {
            GolemMeshes.renderPart(part, poseStack.last(), buffer, light, WHITE);
        }
        poseStack.popPose();
    }

    private static void itemIn2D(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        float t = QUILL_THICKNESS;
        int texels = QUILL_TEXELS;
        ResearchTableTop.vertex(pose, buffer, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, 1.0F, -t, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, 1.0F, -t, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, 0.0F, -t, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, 0.0F, -t, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        float half = 0.5F / texels;
        for (int k = 0; k < texels; k++) {
            float x = k / (float) texels;
            float u = 1.0F - x - half;
            ResearchTableTop.vertex(pose, buffer, x, 0.0F, -t, u, 1.0F, -1.0F, 0.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, x, 0.0F, 0.0F, u, 1.0F, -1.0F, 0.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, x, 1.0F, 0.0F, u, 0.0F, -1.0F, 0.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, x, 1.0F, -t, u, 0.0F, -1.0F, 0.0F, 0.0F, light);
        }
        for (int k = 0; k < texels; k++) {
            float x = k / (float) texels;
            float u = 1.0F - x - half;
            float x1 = x + 1.0F / texels;
            ResearchTableTop.vertex(pose, buffer, x1, 1.0F, -t, u, 0.0F, 1.0F, 0.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, x1, 1.0F, 0.0F, u, 0.0F, 1.0F, 0.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, x1, 0.0F, 0.0F, u, 1.0F, 1.0F, 0.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, x1, 0.0F, -t, u, 1.0F, 1.0F, 0.0F, 0.0F, light);
        }
        for (int k = 0; k < texels; k++) {
            float y = k / (float) texels;
            float v = 1.0F - y - half;
            float y1 = y + 1.0F / texels;
            ResearchTableTop.vertex(pose, buffer, 0.0F, y1, 0.0F, 1.0F, v, 0.0F, 1.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, 1.0F, y1, 0.0F, 0.0F, v, 0.0F, 1.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, 1.0F, y1, -t, 0.0F, v, 0.0F, 1.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, 0.0F, y1, -t, 1.0F, v, 0.0F, 1.0F, 0.0F, light);
        }
        for (int k = 0; k < texels; k++) {
            float y = k / (float) texels;
            float v = 1.0F - y - half;
            ResearchTableTop.vertex(pose, buffer, 1.0F, y, 0.0F, 0.0F, v, 0.0F, -1.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, 0.0F, y, 0.0F, 1.0F, v, 0.0F, -1.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, 0.0F, y, -t, 1.0F, v, 0.0F, -1.0F, 0.0F, light);
            ResearchTableTop.vertex(pose, buffer, 1.0F, y, -t, 0.0F, v, 0.0F, -1.0F, 0.0F, light);
        }
    }
}
