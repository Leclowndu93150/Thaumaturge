package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TCTooltips;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ItemRequirementWidget {
    public static final int ICON_SIZE = 16;

    private static final int DEFAULT_STRIDE = 18;
    private static final int STRIDE_BUDGET = 110;
    private static final int STRIDE_BREAKPOINT = 6;

    private static final int SECTION_HEADER_OFFSET_X = -12;
    private static final int SECTION_HEADER_OFFSET_Y = -1;
    private static final int SECTION_HEADER_WIDTH = 56;
    private static final int SECTION_HEADER_HEIGHT = 16;
    private static final int SECTION_HEADER_U = 200;
    private static final int SECTION_HEADER_OBTAIN_V = 216;
    private static final int SECTION_HEADER_CRAFT_V = 200;
    private static final int SECTION_HEADER_TINT = 0x40FFFFFF;

    private static final int SECTION_POPUP_OFFSET_X = -15;
    private static final int SECTION_POPUP_WIDTH = 16;
    private static final int SECTION_POPUP_HEIGHT = 16;

    private static final int SLOT_OFFSET_X = -15;
    private static final int SLOT_BASE_SHIFT = 24;

    private static final int CHECKMARK_OFFSET_X = 8;
    private static final int CHECKMARK_U = 159;
    private static final int CHECKMARK_V = 207;
    private static final int CHECKMARK_SIZE = 10;

    private static final long CYCLE_SECONDS = 1000L;
    private static final int DAMAGE_SENTINEL = 32767;
    private static final int DAMAGE_CYCLE_DIVISOR = 5000;

    private ItemRequirementWidget() {}

    public static int stride(int requirementCount) {
        if (requirementCount > STRIDE_BREAKPOINT) {
            return STRIDE_BUDGET / requirementCount;
        }
        return DEFAULT_STRIDE;
    }

    public static int baseShift() {
        return SLOT_BASE_SHIFT;
    }

    public static int slotOffsetX() {
        return SLOT_OFFSET_X;
    }

    public static void renderObtainHeader(GuiGraphicsExtractor graphics, int x, int y) {
        renderSectionHeader(graphics, x, y, SECTION_HEADER_OBTAIN_V);
    }

    public static void renderCraftHeader(GuiGraphicsExtractor graphics, int x, int y) {
        renderSectionHeader(graphics, x, y, SECTION_HEADER_CRAFT_V);
    }

    private static void renderSectionHeader(GuiGraphicsExtractor graphics, int x, int y, int v) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + SECTION_HEADER_OFFSET_X, y + SECTION_HEADER_OFFSET_Y, (float) SECTION_HEADER_U, (float) v, SECTION_HEADER_WIDTH,
                SECTION_HEADER_HEIGHT, SECTION_HEADER_WIDTH, SECTION_HEADER_HEIGHT, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, SECTION_HEADER_TINT);
    }

    public static void renderObtainHeaderTooltip(GuiGraphicsExtractor graphics, Font font, int x, int y, int mouseX, int mouseY) {
        renderSectionTooltip(graphics, font, x, y, mouseX, mouseY, "obtain");
    }

    public static void renderCraftHeaderTooltip(GuiGraphicsExtractor graphics, Font font, int x, int y, int mouseX, int mouseY) {
        renderSectionTooltip(graphics, font, x, y, mouseX, mouseY, "craft");
    }

    private static void renderSectionTooltip(GuiGraphicsExtractor graphics, Font font, int x, int y, int mouseX, int mouseY, String which) {
        int popupX = x + SECTION_POPUP_OFFSET_X;
        if (mouseX >= popupX && mouseY >= y && mouseX < popupX + SECTION_POPUP_WIDTH && mouseY < y + SECTION_POPUP_HEIGHT) {
            graphics.setTooltipForNextFrame(font, TCTooltips.need(which), mouseX, mouseY);
        }
    }

    public static void renderSlot(GuiGraphicsExtractor graphics, Font font, int x, int y, ResearchRequirement requirement, int slotIndex, boolean hasItem, int mouseX, int mouseY) {
        ItemStack stack = cycleItemStack(requirement, slotIndex);
        if (!stack.isEmpty()) {
            graphics.item(stack, x, y);
        }
        if (hasItem) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + CHECKMARK_OFFSET_X, y, (float) CHECKMARK_U, (float) CHECKMARK_V, CHECKMARK_SIZE, CHECKMARK_SIZE,
                    CHECKMARK_SIZE, CHECKMARK_SIZE, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        }
        if (mouseX >= x && mouseY >= y && mouseX < x + ICON_SIZE && mouseY < y + ICON_SIZE && !stack.isEmpty()) {
            graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
        }
    }

    public static ItemStack cycleItemStack(ResearchRequirement requirement, int slotIndex) {
        int size = requirement.items().size();
        if (size == 0) {
            return ItemStack.EMPTY;
        }
        long wall = System.currentTimeMillis() / CYCLE_SECONDS;
        int index = (int) Math.floorMod((long) slotIndex + wall, (long) size);
        Holder<Item> holder = requirement.items().get(index);
        ItemStack stack = new ItemStack(holder);
        return cycleDamageable(stack);
    }

    private static ItemStack cycleDamageable(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return stack;
        }
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0 || stack.getDamageValue() != DAMAGE_SENTINEL) {
            return stack;
        }
        int divisor = Math.max(1, DAMAGE_CYCLE_DIVISOR / maxDamage);
        int damage = (int) Math.floorMod(System.currentTimeMillis() / (long) divisor, (long) maxDamage);
        ItemStack copy = stack.copy();
        copy.setDamageValue(damage);
        return copy;
    }
}
