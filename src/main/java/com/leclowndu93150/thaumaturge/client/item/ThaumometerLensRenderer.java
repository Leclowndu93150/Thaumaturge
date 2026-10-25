package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedBlock;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedEntity;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedSky;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanNode;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class ThaumometerLensRenderer {
    private static final String NODE_KEY = "gui.thaumaturge.thaumometer.node";
    private static final String ENERGIZED_NODE_KEY = "gui.thaumaturge.thaumometer.node_energized";
    private static final String NODE_TYPE_KEY_PREFIX = "nodetype.thaumaturge.";
    private static final String NODE_MODIFIER_KEY_PREFIX = "nodemod.thaumaturge.";
    private static final String TYPE_MODIFIER_KEY = "tooltip.thaumaturge.node.type_modifier";
    private static final String FALLBACK_NAME = "?";
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int NODE_LINE_COLOR = 0xFFEEAE16;
    private static final int AMOUNT_COLOR = 0xFFFFFFFF;
    private static final int NO_BACKGROUND = 0;
    private static final float NODE_LINE_SCALE = 0.004F;
    private static final float NODE_LINE_Y = -40.0F;
    private static final float NAME_Y = -0.25F;
    private static final float NAME_FRONT_Z = 0.0005F;
    private static final float NAME_BASE_SCALE = 0.005F;
    private static final float NAME_MIN_SCALE = 0.001F;
    private static final float NAME_SCALE_STEP = 0.000025F;
    private static final int NAME_FULL_SCALE_WIDTH = 90;
    private static final float GRID_SCALE = 0.0075F;
    private static final int TAG_SIZE = 16;
    private static final float TAG_HALF = 8.0F;
    private static final float TAG_FIRST_ROW_Y = -8.0F;
    private static final int GRID_ROWS = 5;
    private static final float AMOUNT_FRONT_Z = 0.01F;
    private static final float AMOUNT_SCALE = 0.5F;
    private static final float AMOUNT_GAP = 1.0F;
    private static final float TAG_ALPHA = 1.0F;

    private ThaumometerLensRenderer() {}

    public static void submitReadout(
            Minecraft minecraft, AbstractClientPlayer player, PoseStack poseStack, MultiBufferSource buffers) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        ScanTarget target = ThaumometerItem.resolveTarget(level, player);
        if (target instanceof ScannedSky) {
            return;
        }
        Readout readout = readoutOf(level, player, target);
        Font font = minecraft.font;
        Component nodeLine = readout.nodeLine();
        if (nodeLine != null) {
            submitNodeLine(font, poseStack, buffers, nodeLine);
        }
        if (!readout.aspects().isEmpty()) {
            submitGrid(font, player, poseStack, buffers, readout.aspects().entries());
        }
        submitName(font, poseStack, buffers, readout.name());
    }

    private static Readout readoutOf(ClientLevel level, AbstractClientPlayer player, ScanTarget target) {
        if (target instanceof ScannedBlock(BlockPos pos)) {
            return blockReadout(level, player, target, pos);
        }
        if (target instanceof ScannedEntity(Entity entity)) {
            return entityReadout(player, entity);
        }
        return Readout.plain(Component.literal(FALLBACK_NAME), AspectList.EMPTY);
    }

    private static AspectList knownOrEmpty(
            AbstractClientPlayer player, ResourceLocation researchKey, Supplier<AspectList> aspects) {
        return PlayerResearchAccess.of(player).knows(researchKey) ? aspects.get() : AspectList.EMPTY;
    }

    private static Readout blockReadout(
            ClientLevel level, AbstractClientPlayer player, ScanTarget target, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockEntityNode node) {
            return nodeReadout(level, player, pos, node);
        }
        BlockState state = level.getBlockState(pos);
        ItemStack pick = ScanningManager.stackOf(player, target);
        Component name = pick.isEmpty() || !state.getFluidState().isEmpty()
                ? state.getBlock().getName()
                : pick.getHoverName();
        IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, pos, null);
        if (container != null && !container.getAspects().isEmpty()) {
            return Readout.plain(name, container.getAspects());
        }
        if (pick.isEmpty()) {
            return Readout.plain(name, AspectList.EMPTY);
        }
        return Readout.plain(
                name,
                knownOrEmpty(
                        player,
                        ScanKeys.item(pick.getItem()),
                        () -> AspectIndexAccess.index().of(pick)));
    }

    private static Readout nodeReadout(
            ClientLevel level, AbstractClientPlayer player, BlockPos pos, BlockEntityNode node) {
        Component name = Component.translatable(node.isEnergized() ? ENERGIZED_NODE_KEY : NODE_KEY);
        if (PlayerResearchAccess.of(player).knows(ScanNode.researchKey(level, pos))) {
            return new Readout(name, nodeLine(node), node.getAspects());
        }
        return Readout.plain(name, AspectList.EMPTY);
    }

    private static Readout entityReadout(AbstractClientPlayer player, Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            return Readout.plain(
                    stack.getHoverName(),
                    knownOrEmpty(
                            player,
                            ScanKeys.item(stack.getItem()),
                            () -> AspectIndexAccess.index().of(stack)));
        }
        return Readout.plain(
                entity.getDisplayName(),
                knownOrEmpty(player, ScanKeys.entity(entity.getType()), () -> EntityAspects.of(entity)));
    }

    private static Component nodeLine(BlockEntityNode node) {
        Component type =
                Component.translatable(NODE_TYPE_KEY_PREFIX + node.getNodeType().getSerializedName());
        NodeModifier modifier = node.getNodeModifier();
        if (modifier == null) {
            return type;
        }
        Component modifierName = Component.translatable(NODE_MODIFIER_KEY_PREFIX + modifier.getSerializedName());
        return Component.translatable(TYPE_MODIFIER_KEY, type, modifierName);
    }

    private static void drawText(
            MultiBufferSource buffers,
            Font font,
            PoseStack poseStack,
            Component text,
            float x,
            float y,
            boolean shadow,
            int color) {
        font.drawInBatch(
                text.getVisualOrderText(),
                x,
                y,
                color,
                shadow,
                poseStack.last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                NO_BACKGROUND,
                LightTexture.FULL_BRIGHT);
    }

    private static void submitNodeLine(Font font, PoseStack poseStack, MultiBufferSource buffers, Component line) {
        poseStack.pushPose();
        poseStack.scale(NODE_LINE_SCALE, NODE_LINE_SCALE, NODE_LINE_SCALE);
        drawText(buffers, font, poseStack, line, -font.width(line) / 2.0F, NODE_LINE_Y, false, NODE_LINE_COLOR);
        poseStack.popPose();
    }

    private static float nameScale(int width) {
        if (width <= NAME_FULL_SCALE_WIDTH) {
            return NAME_BASE_SCALE;
        }
        return Math.max(NAME_MIN_SCALE, NAME_BASE_SCALE - NAME_SCALE_STEP * (width - NAME_FULL_SCALE_WIDTH));
    }

    private static void submitName(Font font, PoseStack poseStack, MultiBufferSource buffers, Component name) {
        int width = font.width(name);
        drawPlaced(
                buffers,
                font,
                poseStack,
                name,
                new Vector3f(0.0F, NAME_Y, NAME_FRONT_Z),
                nameScale(width),
                -width / 2.0F,
                0.0F,
                false,
                NAME_COLOR);
    }

    private static void submitGrid(
            Font font,
            AbstractClientPlayer player,
            PoseStack poseStack,
            MultiBufferSource buffers,
            List<AspectInstance> entries) {
        poseStack.pushPose();
        poseStack.scale(GRID_SCALE, GRID_SCALE, GRID_SCALE);
        int row = 0;
        int placed = 0;
        while (row < GRID_ROWS && placed < entries.size()) {
            int count = Math.min(GRID_ROWS - row, entries.size() - placed);
            submitRow(font, player, poseStack, buffers, entries.subList(placed, placed + count), row);
            placed += count;
            row++;
        }
        poseStack.popPose();
    }

    private static void submitRow(
            Font font,
            AbstractClientPlayer player,
            PoseStack poseStack,
            MultiBufferSource buffers,
            List<AspectInstance> rowEntries,
            int row) {
        float left = -(rowEntries.size() * TAG_SIZE / 2.0F);
        float top = TAG_FIRST_ROW_Y + TAG_SIZE * row;
        for (int column = 0; column < rowEntries.size(); column++) {
            submitTag(font, player, poseStack, buffers, rowEntries.get(column), left + TAG_SIZE * column, top);
        }
    }

    private static void submitTag(
            Font font,
            AbstractClientPlayer player,
            PoseStack poseStack,
            MultiBufferSource buffers,
            AspectInstance entry,
            float x,
            float y) {
        Holder<IAspect> aspect = entry.aspect();
        boolean discovered = AspectPools.isDiscovered(player, aspect);
        ResourceLocation texture = discovered ? aspect.value().texture() : AspectTagWorldRenderer.UNKNOWN_TEXTURE;
        poseStack.pushPose();
        poseStack.translate(x + TAG_HALF, y + TAG_HALF, 0.0F);
        poseStack.pushPose();
        poseStack.scale(TAG_SIZE, -TAG_SIZE, TAG_SIZE);
        AspectTagWorldRenderer.renderQuad(
                poseStack.last(),
                buffers.getBuffer(TTFlatRenderTypes.entityTranslucentFlat(texture)),
                aspect,
                TAG_ALPHA,
                !discovered,
                LightTexture.FULL_BRIGHT);
        poseStack.popPose();
        if (discovered) {
            submitAmount(font, poseStack, buffers, entry.amount());
        }
        poseStack.popPose();
    }

    private static void submitAmount(Font font, PoseStack poseStack, MultiBufferSource buffers, int amount) {
        Component text = Component.literal(Integer.toString(amount));
        drawPlaced(
                buffers,
                font,
                poseStack,
                text,
                new Vector3f(TAG_HALF, TAG_HALF, AMOUNT_FRONT_Z),
                AMOUNT_SCALE,
                -font.width(text) - AMOUNT_GAP,
                -font.lineHeight,
                true,
                AMOUNT_COLOR);
    }

    private static void drawPlaced(
            MultiBufferSource buffers,
            Font font,
            PoseStack poseStack,
            Component text,
            Vector3f offset,
            float scale,
            float x,
            float y,
            boolean shadow,
            int color) {
        poseStack.pushPose();
        poseStack.translate(offset.x(), offset.y(), offset.z());
        poseStack.scale(scale, scale, scale);
        drawText(buffers, font, poseStack, text, x, y, shadow, color);
        poseStack.popPose();
    }

    private record Readout(Component name, @Nullable Component nodeLine, AspectList aspects) {
        static Readout plain(Component name, AspectList aspects) {
            return new Readout(name, null, aspects);
        }
    }
}
