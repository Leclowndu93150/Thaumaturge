package com.leclowndu93150.thaumaturge.client.screen.golem;

import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCLabelButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCPlusMinusButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCScrollButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCSlider;
import com.leclowndu93150.thaumaturge.content.golem.logistics.MenuGolemLogistics;
import com.leclowndu93150.thaumaturge.network.ServerboundLogisticsRequestPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundLogisticsSearchPayload;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class GolemLogisticsScreen extends AbstractTCContainerScreen<MenuGolemLogistics> {
    private static final int IMAGE_SIZE = 215;
    private static final int ATLAS = 256;

    private static final int SELECTION_U = 222;
    private static final int SELECTION_V = 46;
    private static final int SELECTION_SIZE = 20;
    private static final int SELECTION_ORIGIN_X = 17;
    private static final int SELECTION_ORIGIN_Y = 17;

    private static final int SCROLL_UP_X = 195;
    private static final int SCROLL_UP_Y = 16;
    private static final int SCROLL_DOWN_X = 195;
    private static final int SCROLL_DOWN_Y = 180;
    private static final int SCROLLBAR_X = 196;
    private static final int SCROLLBAR_Y = 28;
    private static final int SCROLLBAR_W = 8;
    private static final int SCROLLBAR_H = 149;

    private static final int COUNT_MINUS_X = 13;
    private static final int COUNT_PLUS_X = 57;
    private static final int COUNT_BUTTON_Y = 195;
    private static final int COUNTBAR_X = 24;
    private static final int COUNTBAR_Y = 196;
    private static final int COUNTBAR_W = 32;
    private static final int COUNTBAR_H = 8;
    private static final int COUNT_LABEL_X = 83;
    private static final int COUNT_LABEL_Y = 196;
    private static final int COUNT_LABEL_COLOR = 0xFF333333;

    private static final int REQUEST_X = 116;
    private static final int REQUEST_Y = 200;
    private static final int REQUEST_W = 40;
    private static final int REQUEST_H = 13;
    private static final int REQUEST_U = 37;
    private static final int REQUEST_V = 82;

    private static final int SEARCH_X = 143;
    private static final int SEARCH_Y = 196;
    private static final int SEARCH_W = 55;
    private static final int SEARCH_HINT_X = 146;
    private static final int SEARCH_HINT_Y = 197;
    private static final int SEARCH_HINT_COLOR = 0xFF222222;

    private static final long REFRESH_INTERVAL_MS = 1000L;
    private static final int MIN_REQUEST = 1;
    private static final float CLACK_VOLUME = 0.66F;

    private @Nullable TCSlider scrollbar;
    private @Nullable TCSlider countbar;
    private @Nullable TCPlusMinusButton countDown;
    private @Nullable TCPlusMinusButton countUp;
    private @Nullable TCLabelButton requestButton;
    private @Nullable EditBox searchField;

    private int selectedSlot = -1;
    private ItemStack selectedStack = ItemStack.EMPTY;
    private int requestCount = MIN_REQUEST;
    private int lastScrollPage;
    private long nextRefresh;

    public GolemLogisticsScreen(MenuGolemLogistics menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TCScreenTextures.GUI_LOGISTICS, IMAGE_SIZE, IMAGE_SIZE);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(TCScrollButton.of(
                leftPos + SCROLL_UP_X,
                topPos + SCROLL_UP_Y,
                TCScrollButton.Direction.UP,
                Component.translatable("gui.thaumaturge.logistics.scroll_up"),
                () -> clickButton(MenuGolemLogistics.BUTTON_PAGE_UP)));
        addRenderableWidget(TCScrollButton.of(
                leftPos + SCROLL_DOWN_X,
                topPos + SCROLL_DOWN_Y,
                TCScrollButton.Direction.DOWN,
                Component.translatable("gui.thaumaturge.logistics.scroll_down"),
                () -> clickButton(MenuGolemLogistics.BUTTON_PAGE_DOWN)));
        countDown = TCPlusMinusButton.minus(
                leftPos + COUNT_MINUS_X,
                topPos + COUNT_BUTTON_Y,
                Component.translatable("gui.thaumaturge.logistics.count_down"),
                () -> adjustCount(-1));
        countUp = TCPlusMinusButton.plus(
                leftPos + COUNT_PLUS_X,
                topPos + COUNT_BUTTON_Y,
                Component.translatable("gui.thaumaturge.logistics.count_up"),
                () -> adjustCount(1));
        addRenderableWidget(countDown);
        addRenderableWidget(countUp);
        scrollbar = new TCSlider(
                leftPos + SCROLLBAR_X,
                topPos + SCROLLBAR_Y,
                SCROLLBAR_W,
                SCROLLBAR_H,
                true,
                0.0F,
                menu.end(),
                menu.start(),
                this::onScroll);
        countbar = new TCSlider(
                leftPos + COUNTBAR_X,
                topPos + COUNTBAR_Y,
                COUNTBAR_W,
                COUNTBAR_H,
                false,
                MIN_REQUEST,
                selectedCount(),
                requestCount,
                this::onCountChanged);
        addRenderableWidget(scrollbar);
        addRenderableWidget(countbar);
        requestButton = TCLabelButton.centered(
                leftPos + REQUEST_X,
                topPos + REQUEST_Y,
                REQUEST_W,
                REQUEST_H,
                TCScreenTextures.GUI_BASE,
                REQUEST_U,
                REQUEST_V,
                REQUEST_W,
                REQUEST_H,
                ATLAS,
                ATLAS,
                Component.translatable("gui.thaumaturge.logistics.request"),
                this::sendRequest);
        addRenderableWidget(requestButton);
        searchField =
                new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_W, font.lineHeight, Component.empty());
        searchField.setMaxLength(MenuGolemLogistics.SEARCH_MAX_LENGTH);
        searchField.setBordered(true);
        searchField.setTextColor(-1);
        searchField.setResponder(this::onSearchChanged);
        addRenderableWidget(searchField);
        syncWidgets();
    }

    private void clickButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void onScroll(float value) {
        int page = Math.round(value);
        if (page != lastScrollPage) {
            lastScrollPage = page;
            clickButton(MenuGolemLogistics.BUTTON_SET_PAGE + page);
        }
    }

    private void onCountChanged(float value) {
        requestCount = Math.max(MIN_REQUEST, Math.round(value));
    }

    private void adjustCount(int delta) {
        requestCount = Math.clamp(requestCount + delta, MIN_REQUEST, selectedCount());
        if (countbar != null) {
            countbar.setValue(requestCount);
        }
    }

    private void onSearchChanged(String text) {
        PacketDistributor.sendToServer(new ServerboundLogisticsSearchPayload(text));
    }

    private void sendRequest() {
        ItemStack stack = selectedStack();
        if (!stack.isEmpty()) {
            PacketDistributor.sendToServer(
                    new ServerboundLogisticsRequestPayload(stack.copyWithCount(1), requestCount));
        }
    }

    private ItemStack selectedStack() {
        return selectedSlot < 0 || selectedSlot >= menu.slots.size()
                ? ItemStack.EMPTY
                : menu.getSlot(selectedSlot).getItem();
    }

    private int selectedCount() {
        ItemStack stack = selectedStack();
        return stack.isEmpty() ? MIN_REQUEST : stack.getCount();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        long now = System.currentTimeMillis();
        if (now >= nextRefresh) {
            nextRefresh = now + REFRESH_INTERVAL_MS;
            clickButton(MenuGolemLogistics.BUTTON_REFRESH);
        }
        syncSelection();
        syncWidgets();
    }

    private void syncSelection() {
        if (selectedSlot < 0) {
            return;
        }
        ItemStack current = selectedStack();
        if (current.isEmpty()) {
            selectedSlot = -1;
            selectedStack = ItemStack.EMPTY;
            return;
        }
        if (ItemStack.isSameItemSameComponents(current, selectedStack)) {
            return;
        }
        selectedSlot = -1;
        for (Slot slot : menu.slots) {
            if (ItemStack.isSameItemSameComponents(selectedStack, slot.getItem())) {
                selectedSlot = slot.index;
                return;
            }
        }
        selectedStack = ItemStack.EMPTY;
    }

    private void syncWidgets() {
        boolean hasSelection = selectedSlot >= 0;
        if (countbar != null) {
            countbar.visible = hasSelection;
            if (hasSelection && countbar.max() != selectedCount()) {
                countbar.setMax(selectedCount());
                requestCount = MIN_REQUEST;
                countbar.setValue(requestCount);
            }
        }
        if (countDown != null) {
            countDown.visible = hasSelection;
        }
        if (countUp != null) {
            countUp.visible = hasSelection;
        }
        if (requestButton != null) {
            requestButton.visible = hasSelection;
        }
        if (scrollbar != null && scrollbar.max() != menu.end()) {
            scrollbar.setMax(menu.end());
            lastScrollPage = menu.start();
            scrollbar.setValue(menu.start());
        }
    }

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (selectedSlot >= 0) {
            int x = leftPos
                    + SELECTION_ORIGIN_X
                    + selectedSlot % MenuGolemLogistics.COLUMNS * MenuGolemLogistics.SLOT_STRIDE;
            int y = topPos
                    + SELECTION_ORIGIN_Y
                    + selectedSlot / MenuGolemLogistics.COLUMNS * MenuGolemLogistics.SLOT_STRIDE;
            graphics.blit(background(), x, y, SELECTION_U, SELECTION_V, SELECTION_SIZE, SELECTION_SIZE, ATLAS, ATLAS);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (selectedSlot >= 0) {
            String text = String.valueOf(requestCount);
            graphics.drawString(
                    font, text, COUNT_LABEL_X - font.width(text) / 2, COUNT_LABEL_Y, COUNT_LABEL_COLOR, false);
        }
        if (searchField != null
                && !searchField.isFocused()
                && searchField.getValue().isEmpty()) {
            graphics.drawString(
                    font,
                    Component.translatable("gui.thaumaturge.logistics.search"),
                    SEARCH_HINT_X,
                    SEARCH_HINT_Y,
                    SEARCH_HINT_COLOR,
                    false);
        }
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType clickType) {
        if (slot != null && slot.hasItem()) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(TCSounds.CLACK.get(), CLACK_VOLUME, 1.0F);
            }
            selectedSlot = slotId;
            selectedStack = slot.getItem().copy();
            requestCount = MIN_REQUEST;
            if (countbar != null) {
                countbar.setMax(selectedCount());
                countbar.setValue(requestCount);
            }
            return;
        }
        super.slotClicked(slot, slotId, button, clickType);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0) {
            clickButton(scrollY < 0.0 ? MenuGolemLogistics.BUTTON_PAGE_DOWN : MenuGolemLogistics.BUTTON_PAGE_UP);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchField != null && !searchField.isMouseOver(mouseX, mouseY)) {
            searchField.setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
            }
            return true;
        }
        if (searchField != null
                && searchField.isFocused()
                && (searchField.keyPressed(keyCode, scanCode, modifiers) || searchField.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
