package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuHandMirror extends AbstractContainerMenu {
    private static final int INPUT_SLOT_X = 80;
    private static final int INPUT_SLOT_Y = 24;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int SLOT_SIZE = 18;

    private final Player player;
    private final InputContainer input;
    public final int mirrorHotbarSlot;
    private boolean transporting;

    public MenuHandMirror(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory);
    }

    public MenuHandMirror(int containerId, Inventory inventory) {
        super(TCMenus.HAND_MIRROR.get(), containerId);
        this.player = inventory.player;
        this.input = new InputContainer(this);
        this.mirrorHotbarSlot = inventory.selected;
        addSlot(new Slot(input, 0, INPUT_SLOT_X, INPUT_SLOT_Y));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        inventory,
                        column + row * 9 + 9,
                        PLAYER_INV_X + column * SLOT_SIZE,
                        PLAYER_INV_Y + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PLAYER_INV_X + column * SLOT_SIZE, HOTBAR_Y));
        }
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
    public ItemStack quickMoveStack(Player quickMovePlayer, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || slot.getItem().getItem() instanceof ItemHandMirror) {
            return ItemStack.EMPTY;
        }
        ItemStack moved = slot.getItem();
        ItemStack original = moved.copy();
        if (index == 0) {
            if (!moveItemStackTo(moved, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(moved, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (moved.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
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
