package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.MenuThaumatorium;
import com.leclowndu93150.thaumaturge.network.ClientboundThaumatoriumRecipesPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundThaumatoriumTogglePayload;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class ThaumatoriumScreen extends AbstractTCContainerScreen<MenuThaumatorium> {
    private static final Identifier TEXTURE = TCIds.rl("textures/gui/gui_thaumatorium.png");
    private static final int GRID_X = 48;
    private static final int GRID_Y = 56;
    private static final int CELL = 16;
    private static final int COLS = 2;
    private static final int ROWS = 3;
    private static final int VISIBLE = COLS * ROWS;
    private static final int ARROW_X = 82;
    private static final int ARROW_UP_Y = 56;
    private static final int ARROW_DOWN_Y = 93;
    private static final int ARROW_W = 8;
    private static final int ARROW_H = 11;
    private static final int QUEUED_U = 176;
    private static final int QUEUED_V = 8;
    private static final int BAR_X = 98;
    private static final int BAR_Y = 40;
    private static final int TAG_X = 96;
    private static final int TAG_Y = 24;
    private static final int BAR_SPACING_X = 16;
    private static final int BAR_SPACING_Y = 20;
    private static final int BAR_U = 176;
    private static final int BAR_BACK_V = 4;
    private static final int BAR_FILL_V = 0;
    private static final int BAR_WIDTH = 12;
    private static final int BAR_HEIGHT = 3;
    private static final int COUNT_X = 64;
    private static final int COUNT_Y = 48;
    private static final int ASPECTS_PER_ROW = 2;
    private static final int MAX_ASPECTS = 8;

    private int index;

    public ThaumatoriumScreen(MenuThaumatorium menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 175, 216);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        List<ClientboundThaumatoriumRecipesPayload.Entry> recipes = menu.clientRecipes;
        int k = leftPos;
        int l = topPos;
        if (index > recipes.size() / COLS) {
            index = recipes.size() / COLS;
        }
        if (index < 0 || recipes.size() <= VISIBLE) {
            index = 0;
        }
        if (recipes.size() > VISIBLE) {
            if (index > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k + ARROW_X, l + ARROW_UP_Y, 176, 56, ARROW_W, ARROW_H, 256, 256);
            }
            if (index < recipes.size() / (float) COLS - ROWS) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k + ARROW_X, l + ARROW_DOWN_Y, 176, 93, ARROW_W, ARROW_H, 256, 256);
            }
        }
        int cell = 0;
        for (int i = index * COLS; i < recipes.size() && cell < VISIBLE; i++, cell++) {
            int px = cell % COLS;
            int py = cell / COLS;
            int x = k + GRID_X + px * CELL;
            int y = l + GRID_Y + py * CELL;
            ClientboundThaumatoriumRecipesPayload.Entry entry = recipes.get(i);
            if (entry.queued()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, QUEUED_U, QUEUED_V, CELL, CELL, 256, 256);
            }
            graphics.item(entry.output(), x, y);
            if (mouseX >= x && mouseY >= y && mouseX < x + CELL && mouseY < y + CELL) {
                graphics.setTooltipForNextFrame(font, entry.output(), mouseX, mouseY);
            }
        }
        BlockEntityThaumatorium machine = menu.blockEntity;
        if (machine != null) {
            if (machine.maxRecipes() > 1) {
                String text = machine.queue().size() + "/" + machine.maxRecipes();
                graphics.pose().pushMatrix();
                graphics.pose().translate(k + COUNT_X, l + COUNT_Y);
                graphics.pose().scale(0.5F, 0.5F);
                graphics.text(font, text, -font.width(text) / 2, 0, 0xFFFFFFFF, false);
                graphics.pose().popMatrix();
            }
            drawAspectBars(graphics, machine, k, l);
        }
    }

    private void drawAspectBars(GuiGraphicsExtractor graphics, BlockEntityThaumatorium machine, int k, int l) {
        List<Identifier> queue = machine.queue();
        if (queue.isEmpty()) {
            return;
        }
        Identifier shownId = queue.get((int) (System.currentTimeMillis() / 1000L % queue.size()));
        ClientboundThaumatoriumRecipesPayload.Entry shown = null;
        for (ClientboundThaumatoriumRecipesPayload.Entry entry : menu.clientRecipes) {
            if (entry.id().equals(shownId)) {
                shown = entry;
                break;
            }
        }
        if (shown == null) {
            return;
        }
        int count = 0;
        for (AspectInstance entry : shown.aspects().sortedByTag()) {
            if (count >= MAX_ASPECTS) {
                break;
            }
            int px = count % ASPECTS_PER_ROW;
            int py = count / ASPECTS_PER_ROW;
            int x = k + BAR_X + BAR_SPACING_X * px;
            int y = l + BAR_Y + BAR_SPACING_Y * py;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, BAR_U, BAR_BACK_V, BAR_WIDTH, BAR_HEIGHT, 256, 256);
            int fill = (int) (machine.essentia().amountOf(entry.aspect()) / (float) entry.amount() * BAR_WIDTH);
            if (fill > 0) {
                int color = ARGB.opaque(entry.aspect().value().color());
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, BAR_U, BAR_FILL_V, Math.min(fill, BAR_WIDTH), BAR_HEIGHT, 256, 256, color);
            }
            count++;
        }
        count = 0;
        for (AspectInstance entry : shown.aspects().sortedByTag()) {
            if (count >= MAX_ASPECTS) {
                break;
            }
            int px = count % ASPECTS_PER_ROW;
            int py = count / ASPECTS_PER_ROW;
            AspectTagRenderer.render(graphics, font, k + TAG_X + BAR_SPACING_X * px, l + TAG_Y + BAR_SPACING_Y * py, entry.aspect(), entry.amount());
            count++;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        int mx = (int) event.x();
        int my = (int) event.y();
        List<ClientboundThaumatoriumRecipesPayload.Entry> recipes = menu.clientRecipes;
        int cell = 0;
        for (int i = index * COLS; i < recipes.size() && cell < VISIBLE; i++, cell++) {
            int px = cell % COLS;
            int py = cell / COLS;
            int x = leftPos + GRID_X + px * CELL;
            int y = topPos + GRID_Y + py * CELL;
            if (mx >= x && my >= y && mx < x + CELL && my < y + CELL) {
                if (menu.blockEntity != null) {
                    ClientPacketDistributor.sendToServer(new ServerboundThaumatoriumTogglePayload(menu.blockEntity.getBlockPos(), recipes.get(i).id()));
                }
                return true;
            }
        }
        if (recipes.size() > VISIBLE) {
            if (index > 0 && mx >= leftPos + ARROW_X && my >= topPos + ARROW_UP_Y && mx < leftPos + ARROW_X + ARROW_W && my < topPos + ARROW_UP_Y + ARROW_H) {
                index--;
                return true;
            }
            if (index < recipes.size() / (float) COLS - ROWS && mx >= leftPos + ARROW_X && my >= topPos + ARROW_DOWN_Y && mx < leftPos + ARROW_X + ARROW_W && my < topPos + ARROW_DOWN_Y + ARROW_H) {
                index++;
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }
}
