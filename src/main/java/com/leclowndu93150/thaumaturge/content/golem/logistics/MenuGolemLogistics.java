package com.leclowndu93150.thaumaturge.content.golem.logistics;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealProvide;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final Comparator<ItemStack> ITEM_ORDER = Comparator.comparing(
                    (ItemStack stack) -> stack.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(stack -> stack.getHoverName().getString())
            .thenComparing(
                    stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
            .thenComparing(stack -> stack.getComponents().toString());

    private final SimpleContainer display = new SimpleContainer(SIZE);
    private final List<ItemStack> items = new ArrayList<>();
    private final Player player;
    private final @Nullable LogisticsTarget target;
    private final DataSlot start = DataSlot.standalone();
    private final DataSlot end = DataSlot.standalone();
    private String searchText = "";

    public MenuGolemLogistics(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, (LogisticsTarget) null);
    }

    public MenuGolemLogistics(int containerId, Inventory playerInventory, @Nullable LogisticsTarget target) {
        super(TCMenus.GOLEM_LOGISTICS.get(), containerId);
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
        int remaining = Math.min(amount, availableCount(requested));
        for (int batchIndex = 0; remaining > 0; batchIndex++) {
            ItemStack batch = requested.copyWithCount(Math.min(remaining, requested.getMaxStackSize()));
            remaining -= batch.getCount();
            int requestId = 31 * player.getId() + batchIndex;
            if (target == null) {
                GolemHelper.requestProvisioning(level, player, batch, requestId);
            } else {
                GolemHelper.requestProvisioning(level, target.pos(), target.face(), batch, requestId);
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
        int itemRows = (items.size() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, itemRows - ROWS);
    }

    private void refresh(boolean full) {
        if (full && player.level() instanceof ServerLevel level) {
            collect(level);
        }
        display.clearContent();
        int first = start.get() * COLUMNS;
        for (int slot = 0; slot < SIZE && first + slot < items.size(); slot++) {
            display.setItem(slot, items.get(first + slot).copy());
        }
        end.set(lastPage());
    }

    private void collect(ServerLevel level) {
        Map<StackKey, ItemStack> found = new HashMap<>();
        String filter = searchText.toLowerCase(Locale.ROOT);
        for (SealEntity seal : SealHandler.getSealsInRange(level, player.blockPosition(), RANGE)) {
            if (!(seal.getSeal() instanceof SealProvide provide)
                    || !player.getUUID().equals(seal.getOwner())) {
                continue;
            }
            IItemHandler handler = InvHelper.getItemHandlerAt(
                    level, seal.getSealPos().pos(), seal.getSealPos().face());
            if (handler == null) {
                continue;
            }
            for (int index = 0; index < handler.getSlots(); index++) {
                ItemStack stack = handler.getStackInSlot(index);
                if (stack.isEmpty() || !provide.matchesFilters(stack) || !matchesSearch(stack, filter)) {
                    continue;
                }
                found.merge(new StackKey(stack), stack.copy(), MenuGolemLogistics::sum);
            }
        }
        items.clear();
        items.addAll(found.values());
        items.sort(ITEM_ORDER);
    }

    private static boolean matchesSearch(ItemStack stack, String filter) {
        return filter.isEmpty()
                || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(filter);
    }

    private static ItemStack sum(ItemStack existing, ItemStack addition) {
        return existing.copyWithCount(existing.getCount() + addition.getCount());
    }

    private record StackKey(ItemStack stack) {
        private StackKey(ItemStack stack) {
            this.stack = stack.copyWithCount(1);
        }

        @Override
        public boolean equals(Object other) {
            return this == other
                    || other instanceof StackKey key && ItemStack.isSameItemSameComponents(stack, key.stack);
        }

        @Override
        public int hashCode() {
            return ItemStack.hashItemAndComponents(stack);
        }
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
