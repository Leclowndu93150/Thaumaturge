package com.leclowndu93150.thaumaturge.content.golem.logistics;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.ProvideBehavior;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class MenuGolemLogistics extends AbstractContainerMenu {
    public static final int COLUMNS = 9;
    public static final int ROWS = 9;
    public static final int SLOT_ORIGIN_X = 19;
    public static final int SLOT_ORIGIN_Y = 19;
    public static final int SLOT_STRIDE = 19;
    public static final int SEARCH_MAX_LENGTH = 10;
    public static final int BUTTON_PAGE_UP = 0;
    public static final int BUTTON_PAGE_DOWN = 1;
    public static final int BUTTON_REFRESH = 2;
    public static final int BUTTON_SET_PAGE = 16;

    private static final int SLOT_TOTAL = COLUMNS * ROWS;
    private static final int PROVISION_RANGE = 32;
    private static final int COLLECT_INTERVAL_TICKS = 20;
    private static final int MAX_STACKS_PER_REQUEST = 16;
    private static final char KEY_SEPARATOR = '\u0001';

    private final Player player;
    private final @Nullable LogisticsTarget target;
    private final DisplayContainer display = new DisplayContainer();
    private final DataSlot pageSlot = DataSlot.standalone();
    private final DataSlot lastPageSlot = DataSlot.standalone();
    private List<ItemStack> stock = List.of();
    private List<ItemStack> visible = List.of();
    private String search = "";
    private boolean collected;
    private long collectedAt;

    public MenuGolemLogistics(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, (LogisticsTarget) null);
    }

    public MenuGolemLogistics(int containerId, Inventory playerInventory, @Nullable LogisticsTarget target) {
        super(TTMenus.GOLEM_LOGISTICS.get(), containerId);
        this.player = playerInventory.player;
        this.target = target;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlot(new DisplaySlot(display, row * COLUMNS + column, SLOT_ORIGIN_X + column * SLOT_STRIDE, SLOT_ORIGIN_Y + row * SLOT_STRIDE));
            }
        }
        addDataSlot(pageSlot);
        addDataSlot(lastPageSlot);
        if (!player.level().isClientSide()) {
            refresh();
        }
    }

    public int start() {
        return pageSlot.get();
    }

    public int end() {
        return lastPageSlot.get();
    }

    public void setSearchText(String text) {
        search = text.length() > SEARCH_MAX_LENGTH ? text.substring(0, SEARCH_MAX_LENGTH) : text;
        pageSlot.set(0);
        refresh();
    }

    public void request(ItemStack template, int amount) {
        if (template.isEmpty() || amount <= 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        int available = 0;
        for (ItemStack entry : visible) {
            if (ItemStack.isSameItemSameComponents(entry, template)) {
                available = entry.getCount();
                break;
            }
        }
        if (available <= 0) {
            return;
        }
        int stackSize = template.getMaxStackSize();
        int remaining = Math.min(Math.min(amount, available), stackSize * MAX_STACKS_PER_REQUEST);
        for (int batch = 0; remaining > 0; batch++) {
            ItemStack part = template.copyWithCount(Math.min(stackSize, remaining));
            if (target != null) {
                GolemHelper.requestProvisioning(level, target.pos(), target.face(), part, batch);
            } else {
                GolemHelper.requestProvisioning(level, player, part, batch);
            }
            remaining -= part.getCount();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_PAGE_DOWN) {
            if (start() < end()) {
                showPage(start() + 1);
            }
            return true;
        }
        if (id == BUTTON_PAGE_UP) {
            if (start() > 0) {
                showPage(start() - 1);
            }
            return true;
        }
        if (id == BUTTON_REFRESH) {
            refresh();
            return true;
        }
        int row = id - BUTTON_SET_PAGE;
        if (row >= 0 && row <= end()) {
            showPage(row);
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    private void refresh() {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        if (!collected || now - collectedAt >= COLLECT_INTERVAL_TICKS) {
            stock = collect(level);
            collected = true;
            collectedAt = now;
        }
        visible = matching(stock, search);
        int lastRow = Math.max(0, Math.ceilDiv(visible.size(), COLUMNS) - ROWS);
        lastPageSlot.set(lastRow);
        showPage(Math.clamp(start(), 0, lastRow));
    }

    private static List<ItemStack> matching(List<ItemStack> stock, String search) {
        String wanted = search.toLowerCase(Locale.ROOT);
        List<ItemStack> kept = new ArrayList<>();
        for (ItemStack entry : stock) {
            if (wanted.isEmpty() || displayName(entry).toLowerCase(Locale.ROOT).contains(wanted)) {
                kept.add(entry.copy());
            }
        }
        kept.sort(Comparator.comparing(MenuGolemLogistics::displayName, String.CASE_INSENSITIVE_ORDER));
        return kept;
    }

    private static String displayName(ItemStack stack) {
        return stack.getHoverName().getString();
    }

    private void showPage(int page) {
        pageSlot.set(page);
        int first = page * COLUMNS;
        for (int slot = 0; slot < SLOT_TOTAL; slot++) {
            int index = first + slot;
            display.setItem(slot, index < visible.size() ? visible.get(index).copy() : ItemStack.EMPTY);
        }
    }

    private List<ItemStack> collect(ServerLevel level) {
        Map<String, ItemStack> merged = new TreeMap<>();
        for (SealEntity seal : SealHandler.within(level, player.blockPosition(), PROVISION_RANGE)) {
            if (!SealHandler.isSealOwner(seal, player.getUUID()) || !(seal.behavior() instanceof ProvideBehavior)) {
                continue;
            }
            ResourceHandler<ItemResource> inventory = InvHelper.getItemHandlerAt(level, seal.pos().pos(), seal.pos().face());
            if (inventory == null) {
                continue;
            }
            for (int slot = 0; slot < inventory.size(); slot++) {
                ItemResource resource = inventory.getResource(slot);
                int amount = inventory.getAmountAsInt(slot);
                if (resource.isEmpty() || amount <= 0) {
                    continue;
                }
                ItemStack offered = resource.toStack(amount);
                if (ProvideBehavior.supplies(seal, offered)) {
                    merged.merge(keyOf(offered), offered, MenuGolemLogistics::combine);
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    private static ItemStack combine(ItemStack existing, ItemStack added) {
        existing.grow(added.getCount());
        return existing;
    }

    private static String keyOf(ItemStack stack) {
        return stack.getHoverName().getString() + KEY_SEPARATOR + BuiltInRegistries.ITEM.getKey(stack.getItem()) + KEY_SEPARATOR + stack.getComponentsPatch();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.player;
    }

    private static final class DisplaySlot extends Slot {
        DisplaySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    private static final class DisplayContainer implements Container {
        private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_TOTAL, ItemStack.EMPTY);

        @Override
        public int getContainerSize() {
            return SLOT_TOTAL;
        }

        @Override
        public boolean isEmpty() {
            return items.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            items.set(slot, stack);
        }

        @Override
        public int getMaxStackSize() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            items.replaceAll(stack -> ItemStack.EMPTY);
        }
    }
}
