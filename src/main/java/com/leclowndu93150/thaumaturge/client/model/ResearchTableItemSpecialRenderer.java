package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.render.blockentity.ResearchTableTop;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.ResearchTablePart;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class ResearchTableItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Direction ITEM_FACING = Direction.NORTH;
    private static final float ITEM_X = 0.25F;
    private static final float ITEM_Y = 0.25F;
    private static final float ITEM_Z = 0.5F;
    private static final float ITEM_SCALE = 0.5F;
    private static final float EXTENSION_OFFSET_Z = -1.0F;

    public ResearchTableItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        poseStack.pushPose();
        poseStack.translate(ITEM_X, ITEM_Y, ITEM_Z);
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        dispatcher.renderSingleBlock(part(ResearchTablePart.MAIN, Direction.NORTH), poseStack, buffers, light, overlay);
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, EXTENSION_OFFSET_Z);
        dispatcher.renderSingleBlock(part(ResearchTablePart.EXT, Direction.SOUTH), poseStack, buffers, light, overlay);
        poseStack.popPose();
        poseStack.pushPose();
        ResearchTableTop.pose(poseStack, ITEM_FACING);
        ResearchTableTop.turnSheetsAround(poseStack);
        ResearchTableTop.renderSheets(poseStack, buffers, light);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static BlockState part(ResearchTablePart part, Direction facing) {
        return TTBlocks.RESEARCH_TABLE
                .get()
                .defaultBlockState()
                .setValue(BlockResearchTable.PART, part)
                .setValue(BlockResearchTable.FACING, facing);
    }
}
