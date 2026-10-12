package com.leclowndu93150.thaumaturge.client.screen.research.detail.insert;

import com.leclowndu93150.thaumaturge.api.aspect.*;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.ArrowBob;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.AspectTileDrawer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class AspectInsertRenderer {
    public static final int ROWS_PER_PAGE = 5;

    private static final int PAGE_LEFT_OFFSET = 60;
    private static final int PAGE_TOP_OFFSET = 24;
    private static final int ROW_STRIDE = 40;
    private static final int ROW_HOVER_SIZE = 32;
    private static final int BACKDROP_OFFSET = -2;
    private static final float BACKDROP_SCALE = 2.0F;
    private static final float BACKDROP_ALPHA = 0.5F;
    private static final int TAG_OFFSET = 2;
    private static final float TAG_SCALE = 1.5F;
    private static final int NAME_CENTER_X = 16;
    private static final int NAME_Y_OFFSET = 29;
    private static final float NAME_SCALE = 0.5F;
    private static final int NAME_COLOR = 0xFF505050;
    private static final int LEFT_PART_X = 60;
    private static final int RIGHT_PART_X = 102;
    private static final int PART_Y_OFFSET = 4;
    private static final float PART_SCALE = 1.25F;
    private static final int LEFT_PART_NAME_X = 22 + 50;
    private static final int RIGHT_PART_NAME_X = 22 + 92;
    private static final int EQUALS_X = 9 + 32;
    private static final int PLUS_X = 10 + 79;
    private static final int OPERATOR_Y_OFFSET = 12;
    private static final int OPERATOR_COLOR = 0xFF999999;
    private static final int PRIMAL_X = 54;
    private static final int PRIMAL_COLOR = 0xFF777777;
    private static final int PAGER_LEFT_X = -20;
    private static final int PAGER_RIGHT_X = 144;
    private static final int PAGER_Y = 208;
    private static final int COMPONENT_COUNT = 2;
    private static final int ITEMS_PER_ROW = 8;
    private static final int ITEM_SIZE = 18;

    private AspectInsertRenderer() {}

    public static AspectList listedAspects(@Nullable Player player) {
        if (player == null) {
            return AspectList.EMPTY;
        }
        return player.registryAccess().lookup(IAspect.REGISTRY_KEY).map(lookup -> {
            AspectList revealed = AspectList.EMPTY;
            for (Holder.Reference<IAspect> aspect : lookup.listElements().toList()) {
                if (AspectKnowledgeAccess.of(aspect).isCompositionRevealed()) {
                    revealed = revealed.add(aspect, 1);
                }
            }
            return revealed;
        }).orElse(AspectList.EMPTY);
    }

    public static int pageCount(@Nullable Player player) {
        return pagesFor(listedAspects(player).size());
    }

    private static int pagesFor(int listedCount) {
        return listedCount == 0 ? 0 : Mth.ceil(listedCount / (float) ROWS_PER_PAGE);
    }

    public static void render(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, int mouseX, int mouseY) {
        BookBlit.paper(graphics, frame.paperLeft(), frame.paperTop());
        drawPage(graphics, frame, model.aspectsPageIndex(), frame.paperLeft() + PAGE_LEFT_OFFSET, frame.paperTop() + PAGE_TOP_OFFSET, mouseX, mouseY);
    }

    private static void drawPage(GuiGraphicsExtractor graphics, DetailFrame frame, int pageIndex, int originX, int originY, int mouseX, int mouseY) {
        AspectList listed = listedAspects(frame.player());
        if (listed.isEmpty()) {
            return;
        }
        List<AspectInstance> ordered = listed.sortedByTag();
        int firstIndex = Math.min(pageIndex * ROWS_PER_PAGE, ordered.size());
        int endIndex = Math.min(firstIndex + ROWS_PER_PAGE, ordered.size());
        List<AspectInstance> pageRows = ordered.subList(firstIndex, endIndex);
        Holder<IAspect> hoveredAspect = null;
        for (int row = 0; row < pageRows.size(); row++) {
            Holder<IAspect> aspect = pageRows.get(row).aspect();
            int rowY = originY + row * ROW_STRIDE;
            drawRow(graphics, frame, aspect, originX, rowY, mouseX, mouseY);
            if (hoveredAspect == null && AspectKnowledgeAccess.isKnown(aspect) && Rect.inside(originX, rowY, ROW_HOVER_SIZE, ROW_HOVER_SIZE, mouseX, mouseY)) {
                hoveredAspect = aspect;
            }
        }
        int totalPages = pagesFor(listed.size());
        float swell = ArrowBob.swell(frame.player());
        if (pageIndex > 0) {
            BookBlit.swellingSprite(graphics, originX + PAGER_LEFT_X, originY + PAGER_Y, BookSprites.ARROW_LEFT_U, BookSprites.ARROW_V, BookSprites.ARROW_WIDTH, BookSprites.ARROW_HEIGHT, swell);
        }
        if (pageIndex < totalPages - 1) {
            BookBlit.swellingSprite(graphics, originX + PAGER_RIGHT_X, originY + PAGER_Y, BookSprites.ARROW_RIGHT_U, BookSprites.ARROW_V, BookSprites.ARROW_WIDTH, BookSprites.ARROW_HEIGHT, swell);
        }

        if (hoveredAspect != null) {
            drawHoverItems(graphics, frame.font(), hoveredAspect, mouseX, mouseY);
        }
    }

    private static void drawRow(GuiGraphicsExtractor graphics, DetailFrame frame, Holder<IAspect> aspect, int rowX, int rowY, int mouseX, int mouseY) {
        Font font = frame.font();
        if (Rect.inside(rowX, rowY, ROW_HOVER_SIZE, ROW_HOVER_SIZE, mouseX, mouseY)) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(rowX + BACKDROP_OFFSET, rowY + BACKDROP_OFFSET);
            graphics.pose().scale(BACKDROP_SCALE, BACKDROP_SCALE);
            BookBlit.iconTile(graphics, AspectTileDrawer.BACKDROP_TILE, 0, 0, BookBlit.alphaTint(BACKDROP_ALPHA));
            graphics.pose().popMatrix();
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(rowX + TAG_OFFSET, rowY + TAG_OFFSET);
        graphics.pose().scale(TAG_SCALE, TAG_SCALE);
        if (AspectKnowledgeAccess.isKnown(aspect)) {
            AspectTileDrawer.known(graphics, 0, 0, aspect);
        } else {
            AspectTileDrawer.silhouette(graphics, aspect);
        }
        graphics.pose().popMatrix();
        drawScaledName(graphics, font, AspectComponents.name(aspect), rowX + NAME_CENTER_X, rowY + NAME_Y_OFFSET);
        List<Holder<IAspect>> parts = aspect.value().components();
        if (parts.size() >= COMPONENT_COUNT) {
            drawComposition(graphics, frame, parts.get(0), parts.get(1), rowX, rowY);
        } else {
            graphics.text(font, Component.translatable("tooltip.thaumaturge.aspect.primal"), rowX + PRIMAL_X, rowY + OPERATOR_Y_OFFSET, PRIMAL_COLOR, false);
        }
    }

    private static void drawComposition(GuiGraphicsExtractor graphics, DetailFrame frame, Holder<IAspect> left, Holder<IAspect> right, int rowX, int rowY) {
        Font font = frame.font();
        boolean leftKnown = AspectPools.isDiscovered(frame.player(), left);
        boolean rightKnown = AspectPools.isDiscovered(frame.player(), right);
        drawPart(graphics, left, leftKnown, rowX + LEFT_PART_X, rowY + PART_Y_OFFSET);
        drawPart(graphics, right, rightKnown, rowX + RIGHT_PART_X, rowY + PART_Y_OFFSET);
        if (leftKnown) {
            drawScaledName(graphics, font, AspectComponents.name(left), rowX + LEFT_PART_NAME_X, rowY + NAME_Y_OFFSET);
        }
        if (rightKnown) {
            drawScaledName(graphics, font, AspectComponents.name(right), rowX + RIGHT_PART_NAME_X, rowY + NAME_Y_OFFSET);
        }
        graphics.text(font, Component.literal("="), rowX + EQUALS_X, rowY + OPERATOR_Y_OFFSET, OPERATOR_COLOR, false);
        graphics.text(font, Component.literal("+"), rowX + PLUS_X, rowY + OPERATOR_Y_OFFSET, OPERATOR_COLOR, false);
    }

    private static void drawPart(GuiGraphicsExtractor graphics, Holder<IAspect> aspect, boolean revealed, int x, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(PART_SCALE, PART_SCALE);
        if (revealed) {
            AspectTileDrawer.known(graphics, 0, 0, aspect);
        } else {
            AspectTileDrawer.unrevealed(graphics);
        }
        graphics.pose().popMatrix();
    }

    private static void drawScaledName(GuiGraphicsExtractor graphics, Font font, Component name, int centerX, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, y);
        graphics.pose().scale(NAME_SCALE, NAME_SCALE);
        graphics.text(font, name, -font.width(name) / 2, 0, NAME_COLOR, false);
        graphics.pose().popMatrix();
    }

    private static void drawHoverItems(GuiGraphicsExtractor graphics, Font font, Holder<IAspect> aspect, int mouseX, int mouseY) {
        Map<Item, Integer> amountByItem = AspectAmountForItemHelper.aspectAmountByItem(aspect);
        if (amountByItem.isEmpty())
            return;

        int startX = mouseX + 12;
        int startY = mouseY - 4;
        int i = 0;
        for (Map.Entry<Item, Integer> entry : amountByItem.entrySet()) {
            int x = startX + (i % ITEMS_PER_ROW) * ITEM_SIZE;
            int y = startY + (i / ITEMS_PER_ROW) * ITEM_SIZE;

            ItemStack stack = new ItemStack(entry.getKey(), entry.getValue());
            graphics.fakeItem(stack, x, y);
            if (entry.getValue() > 1) {
                String amount = String.valueOf(entry.getValue());
                graphics.text(font, amount, x + 17 - font.width(amount), y + 9, -1, true);
            }
            i++;
        }
    }
}
