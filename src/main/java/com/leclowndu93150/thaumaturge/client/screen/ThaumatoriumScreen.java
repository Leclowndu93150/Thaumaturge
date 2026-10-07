package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.MenuThaumatorium;
import com.leclowndu93150.thaumaturge.network.ClientboundThaumatoriumRecipesPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundThaumatoriumTogglePayload;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ThaumatoriumScreen extends AbstractTTContainerScreen<MenuThaumatorium> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/gui/gui_thaumatorium.png");
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
    private ItemStack hoverTooltip = ItemStack.EMPTY;

    public ThaumatoriumScreen(MenuThaumatorium menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 175, 216);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hoverTooltip = ItemStack.EMPTY;
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!hoverTooltip.isEmpty()) {
            graphics.renderTooltip(font, hoverTooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
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
                graphics.blit(TEXTURE, k + ARROW_X, l + ARROW_UP_Y, 176, 56, ARROW_W, ARROW_H, 256, 256);
            }
            if (index < recipes.size() / (float) COLS - ROWS) {
                graphics.blit(TEXTURE, k + ARROW_X, l + ARROW_DOWN_Y, 176, 93, ARROW_W, ARROW_H, 256, 256);
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
                graphics.blit(TEXTURE, x, y, QUEUED_U, QUEUED_V, CELL, CELL, 256, 256);
            }
            graphics.renderItem(entry.output(), x, y);
            if (mouseX >= x && mouseY >= y && mouseX < x + CELL && mouseY < y + CELL) {
                hoverTooltip = entry.output();
            }
        }
        BlockEntityThaumatorium machine = menu.blockEntity;
        if (machine != null) {
            if (machine.maxRecipes() > 1) {
                String text = machine.queue().size() + "/" + machine.maxRecipes();
                graphics.pose().pushPose();
                graphics.pose().translate(k + COUNT_X, l + COUNT_Y, 0.0F);
                graphics.pose().scale(0.5F, 0.5F, 1.0F);
                graphics.drawString(font, text, -font.width(text) / 2, 0, 0xFFFFFFFF, false);
                graphics.pose().popPose();
            }
            drawAspectBars(graphics, machine, k, l);
        }
    }

    private void drawAspectBars(GuiGraphics graphics, BlockEntityThaumatorium machine, int k, int l) {
        List<ResourceLocation> queue = machine.queue();
        if (queue.isEmpty()) {
            return;
        }
        ResourceLocation shownId = queue.get((int) (System.currentTimeMillis() / 1000L % queue.size()));
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
            graphics.blit(TEXTURE, x, y, BAR_U, BAR_BACK_V, BAR_WIDTH, BAR_HEIGHT, 256, 256);
            int fill = (int) (machine.essentia().amountOf(entry.aspect()) / (float) entry.amount() * BAR_WIDTH);
            if (fill > 0) {
                int color = ARGB32.opaque(entry.aspect().value().color());
                GuiBlend.blitTinted(
                        graphics,
                        TEXTURE,
                        x,
                        y,
                        BAR_U,
                        BAR_FILL_V,
                        Math.min(fill, BAR_WIDTH),
                        BAR_HEIGHT,
                        256,
                        256,
                        color);
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
            AspectTagRenderer.render(
                    graphics,
                    font,
                    k + TAG_X + BAR_SPACING_X * px,
                    l + TAG_Y + BAR_SPACING_Y * py,
                    entry.aspect(),
                    entry.amount());
            count++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        List<ClientboundThaumatoriumRecipesPayload.Entry> recipes = menu.clientRecipes;
        int cell = 0;
        for (int i = index * COLS; i < recipes.size() && cell < VISIBLE; i++, cell++) {
            int px = cell % COLS;
            int py = cell / COLS;
            int x = leftPos + GRID_X + px * CELL;
            int y = topPos + GRID_Y + py * CELL;
            if (mx >= x && my >= y && mx < x + CELL && my < y + CELL) {
                if (menu.blockEntity != null) {
                    PacketDistributor.sendToServer(new ServerboundThaumatoriumTogglePayload(
                            menu.blockEntity.getBlockPos(), recipes.get(i).id()));
                }
                return true;
            }
        }
        if (recipes.size() > VISIBLE) {
            if (index > 0
                    && mx >= leftPos + ARROW_X
                    && my >= topPos + ARROW_UP_Y
                    && mx < leftPos + ARROW_X + ARROW_W
                    && my < topPos + ARROW_UP_Y + ARROW_H) {
                index--;
                return true;
            }
            if (index < recipes.size() / (float) COLS - ROWS
                    && mx >= leftPos + ARROW_X
                    && my >= topPos + ARROW_DOWN_Y
                    && mx < leftPos + ARROW_X + ARROW_W
                    && my < topPos + ARROW_DOWN_Y + ARROW_H) {
                index++;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
