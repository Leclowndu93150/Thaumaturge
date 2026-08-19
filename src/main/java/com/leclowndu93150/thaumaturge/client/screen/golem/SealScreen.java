package com.leclowndu93150.thaumaturge.client.screen.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealGui;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCButtonIcon;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCHoverButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCImageButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCPlusMinusButton;
import com.leclowndu93150.thaumaturge.content.golem.seals.MenuSealBase;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;

public final class SealScreen extends AbstractTCContainerScreen<MenuSealBase> {
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 232;
    private static final int CIRCLE_U = 96;
    private static final int CIRCLE_V = 0;
    private static final int CIRCLE_SIZE = 160;
    private static final int PANEL_V = 167;
    private static final int PANEL_Y = 143;
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 89;
    private static final int CATEGORY_ICON_V = 120;
    private static final int LOCK_U_LOCKED = 32;
    private static final int LOCK_U_UNLOCKED = 48;
    private static final int REDSTONE_U_ON = 64;
    private static final int REDSTONE_U_OFF = 80;
    private static final int TOGGLE_V = 136;
    private static final int BLACKLIST_U = 0;
    private static final int WHITELIST_U = 16;
    private static final int PROP_BG_U = 2;
    private static final int PROP_BG_V = 18;
    private static final int PROP_CHECK_U = 18;
    private static final int COLOR_DIAL_U = 2;
    private static final int COLOR_DIAL_V = 18;
    private static final int COLOR_SWATCH_U = 74;
    private static final int COLOR_SWATCH_V = 31;
    private static final int FILTER_FRAME_U = 0;
    private static final int FILTER_FRAME_V = 56;
    private static final int FILTER_FRAME_SIZE = 32;
    private static final int WHITE = 0xFFFFFF;
    private static final int LABEL_BLUE = 0xBBAAFF;
    private static final int LABEL_GREY = 0xDDDDDD;
    private static final int ATLAS = 256;

    private final int middleX;
    private final int middleY;

    public SealScreen(MenuSealBase menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TCScreenTextures.GUI_BASE, IMAGE_WIDTH, IMAGE_HEIGHT);
        this.middleX = IMAGE_WIDTH / 2;
        this.middleY = (IMAGE_HEIGHT - 72) / 2 - 8;
    }

    @Override
    protected void init() {
        super.init();
        rebuildCategoryWidgets();
    }

    private void rebuildCategoryWidgets() {
        clearWidgets();
        ISealEntity seal = menu.seal();
        if (seal == null) {
            return;
        }
        int[] categories = menu.categories();
        float slice = Mth.clamp(60.0F / categories.length, 12.0F, 24.0F);
        float start = -180.0F + (categories.length - 1) * slice / 2.0F;
        int c = 0;
        for (int cat : categories) {
            if (categories.length > 1) {
                int xx = (int) (Mth.cos((start - c * slice) / 180.0F * (float) Math.PI) * 86.0F);
                int yy = (int) (Mth.sin((start - c * slice) / 180.0F * (float) Math.PI) * 86.0F);
                int index = c;
                CategoryButton button = new CategoryButton(leftPos + middleX + xx - 8, topPos + middleY + yy - 8, cat, menu.category() == cat, Component.translatable("button.category." + cat),
                        () -> selectCategory(index));
                addRenderableWidget(button);
            }
            c++;
        }
        int xx = (int) (Mth.cos((start - c * slice) / 180.0F * (float) Math.PI) * 86.0F);
        int yy = (int) (Mth.sin((start - c * slice) / 180.0F * (float) Math.PI) * 86.0F);
        addRenderableWidget(new StateButton(leftPos + middleX + xx - 8, topPos + middleY + yy - 8, () -> seal.isRedstoneSensitive() ? REDSTONE_U_ON : REDSTONE_U_OFF,
                () -> Component.translatable(seal.isRedstoneSensitive() ? "golem.prop.redon" : "golem.prop.redoff"), () -> {
                    seal.setRedstoneSensitive(!seal.isRedstoneSensitive());
                    sendButton(seal.isRedstoneSensitive() ? MenuSealBase.BUTTON_REDSTONE_ON : MenuSealBase.BUTTON_REDSTONE_OFF);
                }));
        switch (menu.category()) {
            case ISealGui.CAT_PRIORITY -> {
                addRenderableWidget(TCPlusMinusButton.minus(leftPos + middleX - 5 - 14, topPos + middleY - 17, Component.empty(), () -> sendButton(MenuSealBase.BUTTON_PRIORITY_DOWN)));
                addRenderableWidget(TCPlusMinusButton.plus(leftPos + middleX - 5 + 14, topPos + middleY - 17, Component.empty(), () -> sendButton(MenuSealBase.BUTTON_PRIORITY_UP)));
                addRenderableWidget(TCPlusMinusButton.minus(leftPos + middleX + 18 - 12, topPos + middleY + 4, Component.empty(), () -> sendButton(MenuSealBase.BUTTON_COLOR_DOWN)));
                addRenderableWidget(TCPlusMinusButton.plus(leftPos + middleX + 18 + 11, topPos + middleY + 4, Component.empty(), () -> sendButton(MenuSealBase.BUTTON_COLOR_UP)));
                if (minecraft != null && minecraft.player != null && minecraft.player.getUUID().equals(seal.getOwner())) {
                    addRenderableWidget(new StateButton(leftPos + middleX - 32, topPos + middleY, () -> seal.isLocked() ? LOCK_U_LOCKED : LOCK_U_UNLOCKED,
                            () -> Component.translatable(seal.isLocked() ? "golem.prop.lock" : "golem.prop.unlock"), () -> {
                                seal.setLocked(!seal.isLocked());
                                sendButton(seal.isLocked() ? MenuSealBase.BUTTON_LOCK : MenuSealBase.BUTTON_UNLOCK);
                            }));
                }
            }
            case ISealGui.CAT_FILTER -> {
                if (seal.getSeal() instanceof ISealConfigFilter filter) {
                    int size = filter.getFilterSize();
                    int offsetY = 16 + (size - 1) / 3 * 12;
                    addRenderableWidget(new StateButton(leftPos + middleX - 8, topPos + middleY + (size - 1) / 3 * 24 - offsetY + 27, () -> filter.isBlacklist() ? BLACKLIST_U : WHITELIST_U,
                            () -> Component.translatable(filter.isBlacklist() ? "golem.prop.blacklist" : "golem.prop.whitelist"), () -> {
                                filter.setBlacklist(!filter.isBlacklist());
                                sendButton(filter.isBlacklist() ? MenuSealBase.BUTTON_BLACKLIST_ON : MenuSealBase.BUTTON_BLACKLIST_OFF);
                            }));
                }
            }
            case ISealGui.CAT_AREA -> {
                for (int axis = 0; axis < 3; axis++) {
                    int y = topPos + middleY - 25 + axis * 25;
                    int down = MenuSealBase.BUTTON_AREA_BASE + axis * 2;
                    int up = down + 1;
                    addRenderableWidget(TCPlusMinusButton.minus(leftPos + middleX - 5 - 14, y, Component.empty(), () -> sendButton(down)));
                    addRenderableWidget(TCPlusMinusButton.plus(leftPos + middleX - 5 + 14, y, Component.empty(), () -> sendButton(up)));
                }
            }
            case ISealGui.CAT_TOGGLES -> {
                if (seal.getSeal() instanceof ISealConfigToggles toggles) {
                    ISealConfigToggles.SealToggle[] props = toggles.getToggles();
                    int spacing = props.length < 4 ? 8 : props.length < 6 ? 7 : props.length < 9 ? 6 : 5;
                    int height = (props.length - 1) * spacing;
                    int width = 12;
                    for (ISealConfigToggles.SealToggle prop : props) {
                        int textWidth = 12 + Math.min(100, font.width(Component.translatable(prop.getName())));
                        width = Math.max(width, textWidth / 2);
                    }
                    for (int p = 0; p < props.length; p++) {
                        ISealConfigToggles.SealToggle prop = props[p];
                        int index = p;
                        addRenderableWidget(new PropButton(leftPos + middleX - width, topPos + middleY - 5 - height + p * spacing * 2, prop, () -> {
                            prop.setValue(!prop.getValue());
                            sendButton((prop.getValue() ? MenuSealBase.BUTTON_TOGGLE_ON_BASE : MenuSealBase.BUTTON_TOGGLE_OFF_BASE) + index);
                        }));
                    }
                }
            }
            case ISealGui.CAT_TAGS -> {
                addTagButtons(seal.getSeal().getRequiredTags(), -8);
                addTagButtons(seal.getSeal().getForbiddenTags(), 24);
            }
            default -> {
            }
        }
    }

    private void addTagButtons(GolemTrait[] tags, int yOffset) {
        if (tags == null || tags.length == 0) {
            return;
        }
        for (int p = 0; p < tags.length; p++) {
            GolemTrait tag = tags[p];
            TCHoverButton button = new TCHoverButton(leftPos + middleX + p * 18 - (tags.length - 1) * 9, topPos + middleY + yOffset, 16, 16, new TCButtonIcon.TextureIcon(tag.icon()),
                    Component.translatable(GolemTrait.nameKey(TCGolemTraits.registry().getKey(tag))), () -> {
                    });
            button.setDescription(Component.translatable(GolemTrait.descriptionKey(TCGolemTraits.registry().getKey(tag))));
            addRenderableWidget(button);
        }
    }

    private void selectCategory(int index) {
        sendButton(index);
        rebuildCategoryWidgets();
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            menu.clickMenuButton(minecraft.player, id);
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
        rebuildCategoryWidgets();
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        rebuildCategoryWidgets();
    }

    @Override
    protected void extractBackgroundTexture(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, leftPos + middleX - 80, topPos + middleY - 80, CIRCLE_U, CIRCLE_V, CIRCLE_SIZE, CIRCLE_SIZE, ATLAS, ATLAS);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, leftPos, topPos + PANEL_Y, 0, PANEL_V, PANEL_WIDTH, PANEL_HEIGHT, ATLAS, ATLAS);
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ISealEntity seal = menu.seal();
        if (seal == null) {
            return;
        }
        drawCentered(graphics, Component.translatable("button.category." + menu.category()).getString(), leftPos + middleX, topPos + middleY - 64, WHITE);
        switch (menu.category()) {
            case ISealGui.CAT_PRIORITY -> {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, leftPos + middleX + 17, topPos + middleY + 3, COLOR_DIAL_U, COLOR_DIAL_V, 12, 12, ATLAS, ATLAS);
                if (menu.color() >= 1 && menu.color() <= 16) {
                    int dye = DyeColor.byId(menu.color() - 1).getTextureDiffuseColor();
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, leftPos + middleX + 20, topPos + middleY + 6, COLOR_SWATCH_U, COLOR_SWATCH_V, 6, 6, ATLAS, ATLAS,
                            ARGB.opaque(dye));
                }
                int mx = mouseX - leftPos;
                int my = mouseY - topPos;
                if (mx >= middleX + 5 && mx <= middleX + 41 && my >= middleY + 3 && my <= middleY + 15) {
                    String label = menu.color() >= 1 && menu.color() <= 16
                            ? Component.translatable("golem.prop.color", Component.translatable("color.minecraft." + DyeColor.byId(menu.color() - 1).getName())).getString()
                            : Component.translatable("golem.prop.colorall").getString();
                    drawCentered(graphics, label, leftPos + middleX + 23, topPos + middleY + 17, WHITE);
                }
                drawCentered(graphics, Component.translatable("golem.prop.priority").getString(), leftPos + middleX, topPos + middleY - 28, LABEL_BLUE);
                drawCentered(graphics, String.valueOf(menu.priority()), leftPos + middleX, topPos + middleY - 16, WHITE);
                if (minecraft != null && minecraft.player != null && minecraft.player.getUUID().equals(seal.getOwner())) {
                    drawCentered(graphics, Component.translatable("golem.prop.owner").getString(), leftPos + middleX, topPos + middleY + 32, LABEL_BLUE);
                }
            }
            case ISealGui.CAT_FILTER -> {
                if (seal.getSeal() instanceof ISealConfigFilter filter) {
                    int size = filter.getFilterSize();
                    int offsetX = 16 + (size - 1) % 3 * 12;
                    int offsetY = 16 + (size - 1) / 3 * 12;
                    for (int a = 0; a < size; a++) {
                        int x = a % 3;
                        int y = a / 3;
                        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, leftPos + middleX + x * 24 - offsetX, topPos + middleY + y * 24 - offsetY, FILTER_FRAME_U,
                                FILTER_FRAME_V, FILTER_FRAME_SIZE, FILTER_FRAME_SIZE, ATLAS, ATLAS);
                    }
                    if (!filter.isBlacklist()) {
                        for (int i = 0; i < menu.filterSlotCount(); i++) {
                            Slot slot = menu.slots.get(i);
                            if (slot.isActive() && !slot.getItem().isEmpty()) {
                                int limit = filter.getFilterSlotSize(i);
                                String text = limit == 0 ? "*" : String.valueOf(limit);
                                graphics.text(font, text, leftPos + slot.x + 17 - font.width(text), topPos + slot.y + 9, ARGB.opaque(limit == 0 ? 0xFFAA00 : WHITE), true);
                            }
                        }
                    }
                }
            }
            case ISealGui.CAT_AREA -> {
                drawCentered(graphics, Component.translatable("button.caption.y").getString(), leftPos + middleX, topPos + middleY - 33, LABEL_GREY);
                drawCentered(graphics, Component.translatable("button.caption.x").getString(), leftPos + middleX, topPos + middleY - 9, LABEL_GREY);
                drawCentered(graphics, Component.translatable("button.caption.z").getString(), leftPos + middleX, topPos + middleY + 15, LABEL_GREY);
                drawCentered(graphics, String.valueOf(menu.area().getY()), leftPos + middleX, topPos + middleY - 24, WHITE);
                drawCentered(graphics, String.valueOf(menu.area().getX()), leftPos + middleX, topPos + middleY, WHITE);
                drawCentered(graphics, String.valueOf(menu.area().getZ()), leftPos + middleX, topPos + middleY + 24, WHITE);
            }
            case ISealGui.CAT_TAGS -> {
                drawCentered(graphics, Component.translatable("button.caption.required").getString(), leftPos + middleX, topPos + middleY - 26, LABEL_GREY);
                drawCentered(graphics, Component.translatable("button.caption.forbidden").getString(), leftPos + middleX, topPos + middleY + 6, LABEL_GREY);
            }
            default -> {
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        graphics.text(font, text, x - font.width(text) / 2, y, ARGB.opaque(color), true);
    }

    interface UvSupplier {
        int get();
    }

    interface MessageSupplier {
        Component get();
    }

    static final class CategoryButton extends TCImageButton {
        private final boolean active;

        CategoryButton(int x, int y, int categoryIcon, boolean active, Component message, Runnable onPress) {
            super(x, y, 16, 16, TCScreenTextures.GUI_BASE, categoryIcon * 16, CATEGORY_ICON_V, 16, 16, ATLAS, ATLAS, message, onPress);
            this.active = active;
            if (active) {
                setTintColor(0xFFFFFFFF);
            }
            setDescription(Component.translatable("button.category." + categoryIcon + ".desc"));
        }
    }

    static final class StateButton extends TCButton {
        private final UvSupplier uv;
        private final MessageSupplier messageSupplier;

        StateButton(int x, int y, UvSupplier uv, MessageSupplier messageSupplier, Runnable onPress) {
            super(x, y, 16, 16, messageSupplier.get(), onPress);
            this.uv = uv;
            this.messageSupplier = messageSupplier;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            setMessage(messageSupplier.get());
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, getX(), getY(), uv.get(), TOGGLE_V, 16, 16, ATLAS, ATLAS, activeTintColor(tintColor(), isHovered(), active));
        }
    }

    final class PropButton extends TCButton {
        private final ISealConfigToggles.SealToggle prop;

        PropButton(int x, int y, ISealConfigToggles.SealToggle prop, Runnable onPress) {
            super(x, y, 8, 8, Component.translatable(prop.getName()), onPress);
            this.prop = prop;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, getX() - 2, getY() - 2, PROP_BG_U, PROP_BG_V, 12, 12, ATLAS, ATLAS);
            if (prop.getValue()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, getX() - 2, getY() - 2, PROP_CHECK_U, PROP_BG_V, 12, 12, ATLAS, ATLAS);
            }
            graphics.text(font, Component.translatable(prop.getName()).getString(), getX() + 12, getY(), 0xFFFFFFFF, true);
        }
    }
}
