package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuHandMirror extends AbstractTTMenu {
    private static final int MACHINE_SLOTS = 1;
    private static final int INPUT_SLOT_X = 80;
    private static final int INPUT_SLOT_Y = 24;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Player player;
    private final InputContainer input;
    public final int mirrorHotbarSlot;
    private boolean transporting;

    public MenuHandMirror(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory);
    }

    public MenuHandMirror(int containerId, Inventory inventory) {
        super(TTMenus.HAND_MIRROR.get(), containerId);
        this.player = inventory.player;
        this.input = new InputContainer(this);
        this.mirrorHotbarSlot = inventory.selected;
        addSlot(new Slot(input, 0, INPUT_SLOT_X, INPUT_SLOT_Y));
        addInventoryExtendedSlots(inventory, PLAYER_INV_X, PLAYER_INV_Y);
        addInventoryHotbarSlots(inventory, PLAYER_INV_X, HOTBAR_Y);
    }

    private ItemStack mirror() {
        return player.getMainHandItem();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == input) {
            inputChanged(container);
        }
    }

    private void inputChanged(Container container) {
        if (transporting || player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack inserted = container.getItem(0);
        if (inserted.isEmpty()) {
            return;
        }
        ItemStack moved = inserted.copy();
        transporting = true;
        try {
            container.setItem(0, ItemStack.EMPTY);
            if (!ItemHandMirror.transport(mirror(), moved, serverPlayer)) {
                container.setItem(0, moved);
            }
        } finally {
            transporting = false;
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType containerInput, Player clickPlayer) {
        if (slotId >= 0 && slotId < slots.size() && slots.get(slotId).getItem().getItem() instanceof ItemHandMirror) {
            return;
        }
        if (containerInput == ClickType.SWAP && button == mirrorHotbarSlot) {
            return;
        }
        super.clicked(slotId, button, containerInput, clickPlayer);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (slots.get(index).getItem().getItem() instanceof ItemHandMirror) {
            return ItemStack.EMPTY;
        }
        return quickMoveBetween(index, MACHINE_SLOTS, stack -> true);
    }

    @Override
    public void removed(Player removedPlayer) {
        super.removed(removedPlayer);
        clearContainer(removedPlayer, input);
    }

    @Override
    public boolean stillValid(Player checkPlayer) {
        return checkPlayer.getMainHandItem().getItem() instanceof ItemHandMirror;
    }

    private static final class InputContainer extends SimpleContainer {
        private final MenuHandMirror menu;

        private InputContainer(MenuHandMirror menu) {
            super(1);
            this.menu = menu;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            menu.slotsChanged(this);
        }
    }
}
