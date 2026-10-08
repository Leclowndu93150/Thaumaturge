package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPanel;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MenuSealBase extends AbstractTTMenu {
    public static final int BUTTON_BLACKLIST_ON = 20;
    public static final int BUTTON_BLACKLIST_OFF = 21;
    public static final int BUTTON_LOCK = 25;
    public static final int BUTTON_UNLOCK = 26;
    public static final int BUTTON_REDSTONE_ON = 27;
    public static final int BUTTON_REDSTONE_OFF = 28;
    public static final int BUTTON_TOGGLE_ON_BASE = 30;
    public static final int BUTTON_TOGGLE_OFF_BASE = 60;
    public static final int BUTTON_PRIORITY_DOWN = 80;
    public static final int BUTTON_PRIORITY_UP = 81;
    public static final int BUTTON_COLOR_DOWN = 82;
    public static final int BUTTON_COLOR_UP = 83;
    public static final int BUTTON_AREA_BASE = 90;
    public static final int MIDDLE_X = 88;
    public static final int MIDDLE_Y = 72;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 150;
    public static final int HOTBAR_Y = 208;
    private static final int MAX_PRIORITY = 5;
    private static final int MAX_COLOR = 16;
    private static final int MAX_AREA = 8;
    private static final int FILTER_COLUMNS = 3;

    private final ISealEntity seal;
    private final List<SealPanel> panels;
    private final int filterSlotCount;
    private SealPanel panel;
    private final DataSlot priority = DataSlot.standalone();
    private final DataSlot areaX = DataSlot.standalone();
    private final DataSlot areaY = DataSlot.standalone();
    private final DataSlot areaZ = DataSlot.standalone();
    private final DataSlot color = DataSlot.standalone();

    public MenuSealBase(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(
                containerId,
                playerInventory,
                ClientSealHolder.get(new SealPos(buf.readBlockPos(), buf.readEnum(Direction.class))));
    }

    public MenuSealBase(int containerId, Inventory playerInventory, @Nullable ISealEntity seal) {
        super(TTMenus.SEAL.get(), containerId);
        this.seal = seal;
        this.panels = seal != null ? seal.type().panels() : List.of(SealPanel.PRIORITY);
        this.panel = panels.get(0);
        ISealFilter filter = seal == null ? null : seal.filter().orElse(null);
        if (filter != null) {
            filterSlotCount = filter.spec().slots();
            int offsetX = 16 + (filterSlotCount - 1) % FILTER_COLUMNS * 12;
            int offsetY = 16 + (filterSlotCount - 1) / FILTER_COLUMNS * 12;
            FilterContainer container = new FilterContainer(filter);
            for (int i = 0; i < filterSlotCount; i++) {
                int col = i % FILTER_COLUMNS;
                int row = i / FILTER_COLUMNS;
                addSlot(new GhostSlot(
                        this, container, i, MIDDLE_X + col * 24 - offsetX + 8, MIDDLE_Y + row * 24 - offsetY + 8));
            }
        } else {
            filterSlotCount = 0;
        }
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
        addDataSlot(priority);
        addDataSlot(areaX);
        addDataSlot(areaY);
        addDataSlot(areaZ);
        addDataSlot(color);
    }

    public @Nullable ISealEntity seal() {
        return seal;
    }

    public List<SealPanel> panels() {
        return panels;
    }

    public SealPanel panel() {
        return panel;
    }

    public int priority() {
        return priority.get();
    }

    public BlockPos area() {
        return new BlockPos(areaX.get(), areaY.get(), areaZ.get());
    }

    public int color() {
        return color.get();
    }

    public int filterSlotCount() {
        return filterSlotCount;
    }

    @Override
    public void broadcastChanges() {
        if (seal != null) {
            priority.set(seal.priority());
            areaX.set(seal.area().getX());
            areaY.set(seal.area().getY());
            areaZ.set(seal.area().getZ());
            color.set(seal.color());
        }
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (seal == null) {
            return false;
        }
        boolean changed = handleButton(player, id);
        if (changed) {
            seal.markChanged(player.level());
        }
        return changed;
    }

    private boolean handleButton(Player player, int id) {
        if (id >= 0 && id < panels.size()) {
            panel = panels.get(id);
            return true;
        }
        if (id == BUTTON_REDSTONE_ON || id == BUTTON_REDSTONE_OFF) {
            seal.setRedstoneControlled(id == BUTTON_REDSTONE_ON);
            return true;
        }
        if (!SealAccess.mayEdit(player, seal)) {
            return false;
        }
        if (seal.type().showsSettings()) {
            List<SealSetting> settings = seal.type().settings();
            if (id >= BUTTON_TOGGLE_ON_BASE && id < BUTTON_TOGGLE_ON_BASE + settings.size()) {
                seal.setSetting(settings.get(id - BUTTON_TOGGLE_ON_BASE), true);
                return true;
            }
            if (id >= BUTTON_TOGGLE_OFF_BASE && id < BUTTON_TOGGLE_OFF_BASE + settings.size()) {
                seal.setSetting(settings.get(id - BUTTON_TOGGLE_OFF_BASE), false);
                return true;
            }
        }
        if (id == BUTTON_LOCK || id == BUTTON_UNLOCK) {
            if (player.getUUID().equals(seal.owner())) {
                seal.setLocked(id == BUTTON_LOCK);
                return true;
            }
            return false;
        }
        if ((id == BUTTON_BLACKLIST_ON || id == BUTTON_BLACKLIST_OFF)
                && seal.filter().isPresent()) {
            seal.filter().get().setBlacklist(id == BUTTON_BLACKLIST_ON);
            return true;
        }
        if (id == BUTTON_PRIORITY_DOWN && seal.priority() > -MAX_PRIORITY) {
            seal.setPriority((byte) (seal.priority() - 1));
            return true;
        }
        if (id == BUTTON_PRIORITY_UP && seal.priority() < MAX_PRIORITY) {
            seal.setPriority((byte) (seal.priority() + 1));
            return true;
        }
        if (id == BUTTON_COLOR_DOWN && seal.color() > 0) {
            seal.setColor((byte) (seal.color() - 1));
            return true;
        }
        if (id == BUTTON_COLOR_UP && seal.color() < MAX_COLOR) {
            seal.setColor((byte) (seal.color() + 1));
            return true;
        }
        if (seal.type().hasArea()) {
            BlockPos area = seal.area();
            switch (id) {
                case BUTTON_AREA_BASE -> {
                    if (area.getY() > 1) {
                        seal.setArea(area.offset(0, -1, 0));
                        return true;
                    }
                }
                case BUTTON_AREA_BASE + 1 -> {
                    if (area.getY() < MAX_AREA) {
                        seal.setArea(area.offset(0, 1, 0));
                        return true;
                    }
                }
                case BUTTON_AREA_BASE + 2 -> {
                    if (area.getX() > 1) {
                        seal.setArea(area.offset(-1, 0, 0));
                        return true;
                    }
                }
                case BUTTON_AREA_BASE + 3 -> {
                    if (area.getX() < MAX_AREA) {
                        seal.setArea(area.offset(1, 0, 0));
                        return true;
                    }
                }
                case BUTTON_AREA_BASE + 4 -> {
                    if (area.getZ() > 1) {
                        seal.setArea(area.offset(0, 0, -1));
                        return true;
                    }
                }
                case BUTTON_AREA_BASE + 5 -> {
                    if (area.getZ() < MAX_AREA) {
                        seal.setArea(area.offset(0, 0, 1));
                        return true;
                    }
                }
                default -> {}
            }
        }
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0
                && slotId < slots.size()
                && slots.get(slotId) instanceof GhostSlot ghost
                && seal != null
                && seal.filter().isPresent()) {
            if (!SealAccess.mayEdit(player, seal)) {
                return;
            }
            ghostClick(ghost, button, clickType, seal.filter().get());
            seal.markChanged(player.level());
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void ghostClick(GhostSlot slot, int button, ClickType clickType, ISealFilter filter) {
        boolean limiters = filter.usesLimits();
        ItemStack carried = getCarried().copy();
        int index = slot.getContainerSlot();
        if (button == 1) {
            if (!limiters) {
                slot.set(ItemStack.EMPTY);
                filter.setLimit(index, 0);
            } else if (carried.isEmpty()) {
                if (slot.hasItem()) {
                    filter.setLimit(index, filter.limit(index) - (clickType == ClickType.QUICK_MOVE ? 10 : 1));
                    if (filter.limit(index) < 0) {
                        slot.set(ItemStack.EMPTY);
                        filter.setLimit(index, 0);
                    }
                }
            } else if (slot.hasItem() && ItemStack.isSameItemSameComponents(carried, slot.getItem())) {
                filter.setLimit(index, filter.limit(index) - carried.getCount());
                if (filter.limit(index) < 0) {
                    slot.set(ItemStack.EMPTY);
                    filter.setLimit(index, 0);
                }
            }
        } else if (carried.isEmpty()) {
            if (limiters && slot.hasItem()) {
                filter.setLimit(index, filter.limit(index) + (clickType == ClickType.QUICK_MOVE ? 10 : 1));
            }
        } else {
            if (!limiters) {
                carried.setCount(1);
                filter.setLimit(index, 0);
            } else {
                int count = carried.getCount();
                carried.setCount(1);
                if (slot.hasItem() && ItemStack.isSameItemSameComponents(carried, slot.getItem())) {
                    filter.setLimit(index, filter.limit(index) + count);
                } else {
                    filter.setLimit(index, 0);
                }
            }
            slot.set(carried);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return seal != null && SealAccess.isLive(player, seal);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    static final class GhostSlot extends Slot {
        private final MenuSealBase menu;

        GhostSlot(MenuSealBase menu, SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean isActive() {
            return menu.panel() == SealPanel.FILTER;
        }
    }

    static final class FilterContainer extends SimpleContainer {
        private final ISealFilter filter;

        FilterContainer(ISealFilter filter) {
            super(filter.spec().slots());
            this.filter = filter;
            for (int i = 0; i < filter.spec().slots(); i++) {
                super.setItem(i, filter.stack(i));
            }
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            super.setItem(slot, stack);
            filter.setStack(slot, stack);
        }
    }
}
