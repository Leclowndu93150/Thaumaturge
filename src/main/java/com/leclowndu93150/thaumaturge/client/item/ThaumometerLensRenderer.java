package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.client.render.TCFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanNode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class ThaumometerLensRenderer {
    private static final float NAME_Y = -0.25F;
    private static final float NAME_SCALE = 0.005F;
    private static final int NAME_SHRINK_WIDTH = 90;
    private static final float NAME_SHRINK_PER_PX = 0.000025F;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final float NODE_LINE_SCALE = 0.004F;
    private static final int NODE_LINE_TEXT_Y = -40;
    private static final int NODE_LINE_COLOR = 0xFFEEAE16;
    private static final float TAG_SPACE_SCALE = 0.0075F;
    private static final int TAG_SIZE = 16;
    private static final int TAG_BASE_Y = -8;
    private static final float TAG_ALPHA = 1.0F;
    private static final float TAG_AMOUNT_TEXT_SCALE = 0.5F;
    private static final int TAG_TEXT_COLOR = 0xFFFFFFFF;
    private static final int TAGS_PER_ROW = 5;

    private ThaumometerLensRenderer() {}

    public static void submitReadout(Minecraft mc, AbstractClientPlayer player, PoseStack poseStack, SubmitNodeCollector collector) {
        if (mc.level == null) {
            return;
        }
        Object target = ThaumometerItem.resolveTarget(mc.level, player);
        if (target == null) {
            return;
        }
        Component name = null;
        Component nodeLine = null;
        AspectList aspects = null;
        if (target instanceof BlockPos pos) {
            if (mc.level.getBlockEntity(pos) instanceof BlockEntityNode node) {
                name = Component.translatable(node.isEnergized() ? "tc.node.name.energized" : "tc.node.name");
                if (KnowledgeAccess.of(player).isResearchKnown(ScanNode.researchKey(mc.level, pos))) {
                    Component type = Component.translatable("nodetype.thaumaturge." + node.getNodeType().getSerializedName());
                    nodeLine = node.getNodeModifier() == null
                            ? type
                            : Component.translatable("tc.node.typemod", type, Component.translatable("nodemod.thaumaturge." + node.getNodeModifier().getSerializedName()));
                    aspects = node.getAspects();
                }
            } else {
                BlockState state = mc.level.getBlockState(pos);
                ItemStack pick = new ItemStack(state.getBlock().asItem());
                if (pick.isEmpty()) {
                    return;
                }
                name = pick.getHoverName();
                if (KnowledgeAccess.of(player).isResearchKnown(ScanKeys.item(pick.getItem()))) {
                    aspects = AspectIndexAccess.index().of(pick);
                }
            }
        } else if (target instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            name = stack.getHoverName();
            if (KnowledgeAccess.of(player).isResearchKnown(ScanKeys.item(stack.getItem()))) {
                aspects = AspectIndexAccess.index().of(stack);
            }
        } else if (target instanceof Entity entity) {
            name = entity.getName();
            if (KnowledgeAccess.of(player).isResearchKnown(ScanKeys.entity(entity.getType()))) {
                aspects = EntityAspects.of(entity);
            }
        }
        if (name == null) {
            name = Component.literal("?");
        }

        Font font = mc.font;
        if (nodeLine != null) {
            poseStack.pushPose();
            poseStack.scale(NODE_LINE_SCALE, NODE_LINE_SCALE, NODE_LINE_SCALE);
            collector.submitText(poseStack, -font.width(nodeLine) / 2.0F, NODE_LINE_TEXT_Y, nodeLine.getVisualOrderText(), false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, NODE_LINE_COLOR,
                    0, 0);
            poseStack.popPose();
        }
        if (aspects != null && !aspects.isEmpty()) {
            submitAspectGrid(poseStack, collector, font, player, aspects);
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, NAME_Y, 0.0F);
        float scale = nameScale(font, name);
        poseStack.scale(scale, scale, scale);
        collector.submitText(poseStack, -font.width(name) / 2.0F, 0.0F, name.getVisualOrderText(), false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, NAME_COLOR, 0, 0);
        poseStack.popPose();
    }

    private static float nameScale(Font font, Component name) {
        int width = font.width(name);
        return width > NAME_SHRINK_WIDTH ? NAME_SCALE - NAME_SHRINK_PER_PX * (width - NAME_SHRINK_WIDTH) : NAME_SCALE;
    }

    private static void submitAspectGrid(PoseStack poseStack, SubmitNodeCollector collector, Font font, AbstractClientPlayer player, AspectList aspects) {
        poseStack.pushPose();
        poseStack.scale(TAG_SPACE_SCALE, TAG_SPACE_SCALE, TAG_SPACE_SCALE);
        int column = 0;
        int row = 0;
        int remaining = aspects.size();
        int baseX = Math.min(TAGS_PER_ROW, remaining) * TAG_SIZE / 2;
        for (AspectInstance entry : aspects.entries()) {
            int x = -baseX + column * TAG_SIZE;
            int y = TAG_BASE_Y + row * TAG_SIZE;
            boolean known = AspectPools.isDiscovered(player, entry.aspect());
            poseStack.pushPose();
            poseStack.translate(x + TAG_SIZE / 2.0F, y + TAG_SIZE / 2.0F, 0.0F);
            poseStack.pushPose();
            poseStack.scale(TAG_SIZE, -TAG_SIZE, TAG_SIZE);
            PoseStack.Pose tagPose = poseStack.last().copy();
            collector.submitCustomGeometry(poseStack, TCFlatRenderTypes.entityTranslucentFlat(known ? entry.aspect().value().texture() : AspectTagWorldRenderer.UNKNOWN_TEXTURE),
                    (pose, buffer) -> AspectTagWorldRenderer.renderQuad(tagPose, buffer, entry.aspect(), TAG_ALPHA, !known, LightCoordsUtil.FULL_BRIGHT));
            poseStack.popPose();
            if (known) {
                String amount = Integer.toString(entry.amount());
                poseStack.pushPose();
                poseStack.translate(TAG_SIZE / 2.0F, TAG_SIZE / 2.0F, -0.01F);
                poseStack.scale(TAG_AMOUNT_TEXT_SCALE, TAG_AMOUNT_TEXT_SCALE, TAG_AMOUNT_TEXT_SCALE);
                collector.submitText(poseStack, -font.width(amount) - 1, -font.lineHeight, Component.literal(amount).getVisualOrderText(), true, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT,
                        TAG_TEXT_COLOR, 0, 0);
                poseStack.popPose();
            }
            poseStack.popPose();
            column++;
            if (column >= TAGS_PER_ROW - row) {
                column = 0;
                row++;
                remaining -= TAGS_PER_ROW - row;
                baseX = Math.min(TAGS_PER_ROW - row, Math.max(remaining, 1)) * TAG_SIZE / 2;
            }
        }
        poseStack.popPose();
    }
}
