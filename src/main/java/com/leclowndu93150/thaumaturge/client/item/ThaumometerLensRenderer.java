package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedBlock;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedEntity;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanNode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class ThaumometerLensRenderer {
    private static final float NAME_Y = 0.2F;
    private static final float NAME_SCALE = 0.0075F;
    private static final float NAME_MAX_WIDTH = 0.62F;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final float NODE_LINE_SCALE = 0.005F;
    private static final float NODE_LINE_Y = -0.29F;
    private static final float NODE_LINE_MAX_WIDTH = 0.5F;
    private static final int NODE_LINE_COLOR = 0xFFE6B84A;
    private static final float TAG_SPACE_SCALE = 0.0072F;
    private static final float TAG_MAX_HEIGHT = 0.37F;
    private static final float TAG_GRID_CENTRE_Y = -0.07F;
    private static final int TAG_SIZE = 16;
    private static final float TAG_GAP = 2.0F;
    private static final float TAG_ROW_GAP = 1.0F;
    private static final float TAG_PITCH = TAG_SIZE + TAG_GAP;
    private static final float TAG_ROW_PITCH = TAG_SIZE + TAG_ROW_GAP;
    private static final float TAG_ALPHA = 1.0F;
    private static final float TAG_AMOUNT_TEXT_SCALE = 0.5F;
    private static final int TAG_TEXT_COLOR = 0xFFFFFFFF;
    private static final int TAGS_FIRST_ROW = 5;
    private static final int TAG_MAX_ROWS = 5;

    private ThaumometerLensRenderer() {}

    public static void submitReadout(
            Minecraft mc, AbstractClientPlayer player, PoseStack poseStack, MultiBufferSource buffers) {
        if (mc.level == null) {
            return;
        }
        ScanTarget target = ThaumometerItem.resolveTarget(mc.level, player);
        Component name = null;
        Component nodeLine = null;
        AspectList aspects = null;
        if (target instanceof ScannedBlock scannedBlock) {
            BlockPos pos = scannedBlock.pos();
            if (mc.level.getBlockEntity(pos) instanceof BlockEntityNode node) {
                name = Component.translatable(
                        node.isEnergized()
                                ? "gui.thaumaturge.thaumometer.node_energized"
                                : "gui.thaumaturge.thaumometer.node");
                if (PlayerResearchAccess.of(player).knows(ScanNode.researchKey(mc.level, pos))) {
                    Component type = Component.translatable(
                            "nodetype.thaumaturge." + node.getNodeType().getSerializedName());
                    nodeLine = node.getNodeModifier() == null
                            ? type
                            : Component.translatable(
                                    "tooltip.thaumaturge.node.type_modifier",
                                    type,
                                    Component.translatable("nodemod.thaumaturge."
                                            + node.getNodeModifier().getSerializedName()));
                    aspects = node.getAspects();
                }
            } else {
                BlockState state = mc.level.getBlockState(pos);
                ItemStack pick = ScanningManager.stackOf(player, ScanTarget.block(pos));
                name = state.getFluidState().isEmpty() && !pick.isEmpty()
                        ? pick.getHoverName()
                        : state.getBlock().getName();
                IAspectContainer container = mc.level.getCapability(AspectCapabilities.CONTAINER, pos, null);
                if (container != null && !container.getAspects().isEmpty()) {
                    aspects = container.getAspects();
                } else if (!pick.isEmpty() && PlayerResearchAccess.of(player).knows(ScanKeys.item(pick.getItem()))) {
                    aspects = AspectIndexAccess.index().of(pick);
                }
            }
        } else if (target instanceof ScannedEntity scannedEntity
                && scannedEntity.entity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            name = stack.getHoverName();
            if (!stack.isEmpty() && PlayerResearchAccess.of(player).knows(ScanKeys.item(stack.getItem()))) {
                aspects = AspectIndexAccess.index().of(stack);
            }
        } else if (target instanceof ScannedEntity scannedEntity) {
            Entity entity = scannedEntity.entity();
            name = entity.getName();
            if (PlayerResearchAccess.of(player).knows(ScanKeys.entity(entity.getType()))) {
                aspects = EntityAspects.of(entity);
            }
        }
        if (name == null) {
            name = Component.literal("?");
        }

        Font font = mc.font;
        if (nodeLine != null) {
            poseStack.pushPose();
            int width = font.width(nodeLine);
            poseStack.translate(0.0F, NODE_LINE_Y, 0.0F);
            float scale = fit(width, NODE_LINE_SCALE, NODE_LINE_MAX_WIDTH);
            poseStack.scale(scale, scale, scale);
            drawText(font, nodeLine, -width / 2.0F, -font.lineHeight / 2.0F, true, NODE_LINE_COLOR, poseStack, buffers);
            poseStack.popPose();
        }
        if (aspects != null && !aspects.isEmpty()) {
            submitAspectGrid(poseStack, buffers, font, player, aspects);
        }
        poseStack.pushPose();
        float scale = nameScale(font, name);
        poseStack.translate(0.0F, NAME_Y, 0.0F);
        poseStack.scale(scale, scale, scale);
        drawText(font, name, -font.width(name) / 2.0F, -font.lineHeight / 2.0F, true, NAME_COLOR, poseStack, buffers);
        poseStack.popPose();
    }

    private static float nameScale(Font font, Component name) {
        return fit(font.width(name), NAME_SCALE, NAME_MAX_WIDTH);
    }

    static float fit(int width, float baseScale, float maximumWidth) {
        return width <= 0 ? baseScale : Math.min(baseScale, maximumWidth / width);
    }

    static int rowsFor(int entryCount) {
        int rows = 0;
        int capacity = 0;
        while (rows < TAG_MAX_ROWS && capacity < entryCount) {
            capacity += TAGS_FIRST_ROW - rows;
            rows++;
        }
        return rows;
    }

    private static void drawText(
            Font font,
            Component text,
            float x,
            float y,
            boolean shadow,
            int color,
            PoseStack poseStack,
            MultiBufferSource buffers) {
        font.drawInBatch(
                text.getVisualOrderText(),
                x,
                y,
                color,
                shadow,
                poseStack.last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                0,
                LightTexture.FULL_BRIGHT);
    }

    private static void submitAspectGrid(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Font font,
            AbstractClientPlayer player,
            AspectList aspects) {
        var entries = aspects.entries();
        int rows = rowsFor(entries.size());
        if (rows == 0) {
            return;
        }
        float height = rows * TAG_ROW_PITCH - TAG_ROW_GAP;
        float scale = Math.min(TAG_SPACE_SCALE, TAG_MAX_HEIGHT / height);
        poseStack.pushPose();
        poseStack.translate(0.0F, TAG_GRID_CENTRE_Y, 0.0F);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, -height / 2.0F, 0.0F);
        int start = 0;
        for (int row = 0; row < rows && start < entries.size(); row++) {
            int end = Math.min(entries.size(), start + TAGS_FIRST_ROW - row);
            submitAspectRow(poseStack, buffers, font, player, entries.subList(start, end), row);
            start = end;
        }
        poseStack.popPose();
    }

    private static void submitAspectRow(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Font font,
            AbstractClientPlayer player,
            List<AspectInstance> entries,
            int row) {
        float x = -(entries.size() * TAG_PITCH - TAG_GAP) / 2.0F;
        float y = row * TAG_ROW_PITCH;
        for (AspectInstance entry : entries) {
            boolean known = AspectPools.isDiscovered(player, entry.aspect());
            poseStack.pushPose();
            poseStack.translate(x + TAG_SIZE / 2.0F, y + TAG_SIZE / 2.0F, 0.0F);
            poseStack.pushPose();
            poseStack.scale(TAG_SIZE, -TAG_SIZE, TAG_SIZE);
            PoseStack.Pose tagPose = poseStack.last().copy();
            VertexConsumer tagBuffer = buffers.getBuffer(TTFlatRenderTypes.entityTranslucentFlat(
                    known ? entry.aspect().value().texture() : AspectTagWorldRenderer.UNKNOWN_TEXTURE));
            AspectTagWorldRenderer.renderQuad(
                    tagPose, tagBuffer, entry.aspect(), TAG_ALPHA, !known, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
            if (known) {
                String amount = Integer.toString(entry.amount());
                poseStack.pushPose();
                poseStack.translate(TAG_SIZE / 2.0F, TAG_SIZE / 2.0F, -0.01F);
                poseStack.scale(TAG_AMOUNT_TEXT_SCALE, TAG_AMOUNT_TEXT_SCALE, TAG_AMOUNT_TEXT_SCALE);
                font.drawInBatch(
                        Component.literal(amount).getVisualOrderText(),
                        -font.width(amount) - 1,
                        -font.lineHeight,
                        TAG_TEXT_COLOR,
                        true,
                        poseStack.last().pose(),
                        buffers,
                        Font.DisplayMode.NORMAL,
                        0,
                        LightTexture.FULL_BRIGHT);
                poseStack.popPose();
            }
            poseStack.popPose();
            x += TAG_PITCH;
        }
    }
}
