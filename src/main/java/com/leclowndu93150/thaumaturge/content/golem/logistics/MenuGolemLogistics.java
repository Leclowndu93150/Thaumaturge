package com.leclowndu93150.thaumaturge.content.golem.logistics;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.ProvideBehavior;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

public final class MenuGolemLogistics extends AbstractContainerMenu {
    public static final int BUTTON_PAGE_DOWN = 0;
    public static final int BUTTON_PAGE_UP = 1;
    public static final int BUTTON_REFRESH = 22;
    public static final int BUTTON_SET_PAGE = 100;
    public static final int COLUMNS = 9;
    public static final int ROWS = 9;
    public static final int SLOT_ORIGIN_X = 19;
    public static final int SLOT_ORIGIN_Y = 19;
    public static final int SLOT_STRIDE = 19;
    public static final int SEARCH_MAX_LENGTH = 10;

    private static final int SIZE = COLUMNS * ROWS;
    private static final int RANGE = 32;
    private static final int COLLECT_INTERVAL_TICKS = 20;
    private static final char KEY_SEPARATOR = '|';
    private static final int MAX_REQUEST_STACKS = 16;

    private final SimpleContainer display = new SimpleContainer(SIZE);
    private final List<ItemStack> items = new ArrayList<>();
    private final List<ItemStack> stock = new ArrayList<>();
    private long collectedAt = Long.MIN_VALUE;
    private final Player player;
    private final @Nullable LogisticsTarget target;
    private final DataSlot start = DataSlot.standalone();
    private final DataSlot end = DataSlot.standalone();
    private String searchText = "";

    public MenuGolemLogistics(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, (LogisticsTarget) null);
    }

    public MenuGolemLogistics(int containerId, Inventory playerInventory, @Nullable LogisticsTarget target) {
        super(TTMenus.GOLEM_LOGISTICS.get(), containerId);
        this.player = playerInventory.player;
        this.target = target;
        for (int index = 0; index < SIZE; index++) {
            addSlot(new DisplaySlot(
                    display,
                    index,
                    SLOT_ORIGIN_X + index % COLUMNS * SLOT_STRIDE,
                    SLOT_ORIGIN_Y + index / COLUMNS * SLOT_STRIDE));
        }
        addDataSlot(start);
        addDataSlot(end);
        refresh(true);
    }

    public int start() {
        return start.get();
    }

    public int end() {
        return end.get();
    }

    public void setSearchText(String text) {
        searchText = text.length() > SEARCH_MAX_LENGTH ? text.substring(0, SEARCH_MAX_LENGTH) : text;
        start.set(0);
        refresh(true);
    }

    public void request(ItemStack requested, int amount) {
        if (requested.isEmpty() || amount <= 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        int available = availableCount(requested);
        int remaining = Math.min(Math.min(amount, available), requested.getMaxStackSize() * MAX_REQUEST_STACKS);
        for (int ui = 0; remaining > 0; ui++) {
            ItemStack batch = requested.copyWithCount(Math.min(remaining, requested.getMaxStackSize()));
            remaining -= batch.getCount();
            if (target == null) {
                GolemHelper.requestProvisioning(level, player, batch, ui);
            } else {
                GolemHelper.requestProvisioning(level, target.pos(), target.face(), batch, ui);
            }
        }
    }

    private int availableCount(ItemStack requested) {
        for (ItemStack stack : items) {
            if (ItemStack.isSameItemSameComponents(stack, requested)) {
                return stack.getCount();
            }
        }
        return 0;
    }

    @Override
    public boolean clickMenuButton(Player clicker, int id) {
        if (id == BUTTON_REFRESH) {
            refresh(true);
            return true;
        }
        if (id == BUTTON_PAGE_DOWN) {
            if (start.get() < lastPage()) {
                start.set(start.get() + 1);
                refresh(false);
            }
            return true;
        }
        if (id == BUTTON_PAGE_UP) {
            if (start.get() > 0) {
                start.set(start.get() - 1);
                refresh(false);
            }
            return true;
        }
        if (id >= BUTTON_SET_PAGE) {
            int page = id - BUTTON_SET_PAGE;
            if (page >= 0 && page <= lastPage()) {
                start.set(page);
                refresh(false);
            }
            return true;
        }
        return super.clickMenuButton(clicker, id);
    }

    private int lastPage() {
        return Math.max(0, items.size() / COLUMNS - (ROWS - 1));
    }

    private void refresh(boolean full) {
        if (full
                && player.level() instanceof ServerLevel level
                && level.getGameTime() - collectedAt >= COLLECT_INTERVAL_TICKS) {
            collect(level);
            collectedAt = level.getGameTime();
        }
        if (full) {
            String filter = searchText.toLowerCase(Locale.ROOT);
            items.clear();
            for (ItemStack stack : stock) {
                if (matchesSearch(stack, filter)) {
                    items.add(stack);
                }
            }
        }
        display.clearContent();
        int skipped = 0;
        int slot = 0;
        for (ItemStack stack : items) {
            if (++skipped > start.get() * COLUMNS) {
                display.setItem(slot, stack.copy());
                if (++slot >= SIZE) {
                    break;
                }
            }
        }
        end.set(lastPage());
    }

    private void collect(ServerLevel level) {
        Map<String, ItemStack> found = new TreeMap<>();
        for (SealEntity seal : SealHandler.getSealsInRange(level, player.blockPosition(), RANGE)) {
            if (!(seal.behavior() instanceof ProvideBehavior)
                    || !player.getUUID().equals(seal.owner())) {
                continue;
            }
            IItemHandler handler = InvHelper.getItemHandlerAt(
                    level, seal.pos().pos(), seal.pos().face());
            if (handler == null) {
                continue;
            }
            for (int index = 0; index < handler.getSlots(); index++) {
                ItemStack resource = handler.getStackInSlot(index);
                if (resource.isEmpty()) {
                    continue;
                }
                int amount = resource.getCount();
                if (amount <= 0) {
                    continue;
                }
                ItemStack stack = resource.copy();
                if (!ProvideBehavior.supplies(seal, stack)) {
                    continue;
                }
                found.merge(keyOf(stack), stack, MenuGolemLogistics::sum);
            }
        }
        stock.clear();
        stock.addAll(found.values());
    }

    private static boolean matchesSearch(ItemStack stack, String filter) {
        return filter.isEmpty()
                || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(filter);
    }

    private static ItemStack sum(ItemStack existing, ItemStack addition) {
        return existing.copyWithCount(existing.getCount() + addition.getCount());
    }

    private static String keyOf(ItemStack stack) {
        String name = stack.getHoverName().getString();
        return name
                + KEY_SEPARATOR
                + BuiltInRegistries.ITEM.getKey(stack.getItem())
                + KEY_SEPARATOR
                + stack.getComponentsPatch();
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player clicker) {
        return clicker == player;
    }

    private static final class DisplaySlot extends Slot {
        private DisplaySlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return Integer.MAX_VALUE;
        }
    }
}
