package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ResearchTableModel;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class ResearchTablePaperPortTest {
    private static final float SOURCE_BASE_HEIGHT = 1.008F;
    private static final float SOURCE_HEIGHT_STEP = 0.013F;
    private static final float[] SOURCE_TILTS = {-6.0F, 8.0F, 14.0F, -3.0F, 11.0F};

    @Test
    void eachSheetUsesSourceHeightAndTiltInItsSubmittedPose() {
        for (int sheet = 0; sheet < SOURCE_TILTS.length; sheet++) {
            PoseStack poseStack = new PoseStack();
            ResearchTableTop.applySheetPose(poseStack, sheet);

            Vector3f origin = poseStack.last().pose().transformPosition(0.0F, 0.0F, 0.0F, new Vector3f());
            Vector3f xAxis = poseStack.last().pose().transformPosition(1.0F, 0.0F, 0.0F, new Vector3f());
            double radians = Math.toRadians(SOURCE_TILTS[sheet]);
            float sourceHeight = SOURCE_BASE_HEIGHT + sheet * SOURCE_HEIGHT_STEP;

            assertEquals(0.12F, origin.x, 1.0E-6F, "sheet " + sheet + " x position");
            assertEquals(1.0F - sourceHeight, origin.y, 1.0E-6F, "sheet " + sheet + " height");
            assertEquals(0.32F, origin.z, 1.0E-6F, "sheet " + sheet + " z position");
            assertEquals(0.48D * Math.cos(radians), xAxis.x - origin.x, 1.0E-6D, "sheet " + sheet + " tilt x");
            assertEquals(-0.48D * Math.sin(radians), xAxis.z - origin.z, 1.0E-6D, "sheet " + sheet + " tilt z");
        }
    }

    @Test
    void quillPoseUsesTheSourceAnchorAndYaw() {
        PoseStack poseStack = new PoseStack();
        ResearchTableRenderer.applyQuillPose(poseStack);

        Vector3f origin = poseStack.last().pose().transformPosition(0.0F, 0.0F, 0.0F, new Vector3f());
        Vector3f xAxis = poseStack.last().pose().transformPosition(1.0F, 0.0F, 0.0F, new Vector3f());
        double radians = Math.toRadians(-105.0D);

        assertEquals(-0.226F, origin.x, 1.0E-6F);
        assertEquals(-0.095F, origin.y, 1.0E-6F);
        assertEquals(0.076F, origin.z, 1.0E-6F);
        assertEquals(0.5D * Math.cos(radians), xAxis.x - origin.x, 1.0E-6D);
        assertEquals(-0.5D * Math.sin(radians), xAxis.z - origin.z, 1.0E-6D);
    }

    @Test
    void renderSubmitsFiveFourVertexSheetsWhenToolsAndNoteAreAbsent() {
        BlockEntityRendererProvider.Context context = mock(BlockEntityRendererProvider.Context.class);
        when(context.bakeLayer(TTModelLayers.RESEARCH_TABLE))
                .thenReturn(ResearchTableModel.createLayer().bakeRoot());
        ResearchTableRenderer renderer = new ResearchTableRenderer(context);

        BlockEntityResearchTable table = mock(BlockEntityResearchTable.class);
        BlockState state = mock(BlockState.class);
        when(table.getBlockState()).thenReturn(state);
        when(table.items()).thenReturn(new ItemStackHandler(2));

        MultiBufferSource buffers = mock(MultiBufferSource.class);
        VertexConsumer buffer = mock(VertexConsumer.class, RETURNS_SELF);
        when(buffers.getBuffer(any(RenderType.class))).thenReturn(buffer);

        renderer.render(table, 0.0F, new PoseStack(), buffers, 15728880, 0);

        verify(buffer, times(20)).addVertex(any(PoseStack.Pose.class), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    void facingSideInflationIsAppliedBeforeJoiningTheTableBounds() {
        BlockPos pos = new BlockPos(4, 7, -2);
        for (Direction facing : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            AABB expected = new AABB(pos).minmax(new AABB(pos.relative(facing)).inflate(0.5D));
            assertEquals(expected, ResearchTableRenderer.renderBounds(pos, facing), facing.toString());
        }

        AABB northBounds = ResearchTableRenderer.renderBounds(pos, Direction.NORTH);
        assertEquals(pos.getZ() + 1.0D, northBounds.maxZ, 0.0D);
    }
}
