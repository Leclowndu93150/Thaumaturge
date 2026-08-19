package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuFocusPouch extends AbstractContainerMenu {
    private static final int POUCH_COLUMNS = 6;
    private static final int POUCH_SLOT_X = 40;
    private static final int POUCH_SLOT_Y = 51;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_X_SPACING = -1;
    private static final int SLOT_Y_SPACING = -1;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 151;
    private static final int HOTBAR_Y = 209;

    private final InteractionHand hand;
    private final Player player;
    private final SimpleContainer pouchInventory = new SimpleContainer(FocusPouchItem.SIZE);
    public final int blockedHotbarSlot;

    public MenuFocusPouch(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public MenuFocusPouch(int containerId, Inventory inventory, InteractionHand hand) {
        super(TCMenus.FOCUS_POUCH.get(), containerId);
        this.hand = hand;
        this.player = inventory.player;
        this.blockedHotbarSlot = hand == InteractionHand.MAIN_HAND ? inventory.getSelectedSlot() : -1;
        NonNullList<ItemStack> stored = FocusPouchItem.getInventory(pouch());
        for (int slot = 0; slot < stored.size(); slot++) {
            pouchInventory.setItem(slot, stored.get(slot));
        }
        for (int slot = 0; slot < FocusPouchItem.SIZE; slot++) {
            addSlot(new FocusSlot(pouchInventory, slot, POUCH_SLOT_X + slot % POUCH_COLUMNS * (SLOT_SIZE + SLOT_X_SPACING), POUCH_SLOT_Y + slot / POUCH_COLUMNS * (SLOT_SIZE + SLOT_Y_SPACING)));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, PLAYER_INV_X + column * SLOT_SIZE, PLAYER_INV_Y + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PLAYER_INV_X + column * SLOT_SIZE, HOTBAR_Y));
        }
    }

    private ItemStack pouch() {
        return player.getItemInHand(hand);
    }

    private void saveToStack() {
        ItemStack pouch = pouch();
        if (pouch.getItem() instanceof FocusPouchItem) {
            NonNullList<ItemStack> list = NonNullList.withSize(FocusPouchItem.SIZE, ItemStack.EMPTY);
            for (int slot = 0; slot < list.size(); slot++) {
                list.set(slot, pouchInventory.getItem(slot));
            }
            FocusPouchItem.setInventory(pouch, list);
        }
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput containerInput, Player clickPlayer) {
        if (blockedHotbarSlot >= 0) {
            if (slotId >= 0 && slotId < slots.size() && slots.get(slotId).container == clickPlayer.getInventory() && slots.get(slotId).getContainerSlot() == blockedHotbarSlot) {
                return;
            }
            if (containerInput == ContainerInput.SWAP && button == blockedHotbarSlot) {
                return;
            }
        }
        super.clicked(slotId, button, containerInput, clickPlayer);
        saveToStack();
    }

    @Override
    public void removed(Player removedPlayer) {
        super.removed(removedPlayer);
        saveToStack();
    }

    @Override
    public ItemStack quickMoveStack(Player quickMovePlayer, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack moved = slot.getItem();
        ItemStack original = moved.copy();
        if (index < FocusPouchItem.SIZE) {
            if (!moveItemStackTo(moved, FocusPouchItem.SIZE, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!(moved.getItem() instanceof ItemFocus) || !moveItemStackTo(moved, 0, FocusPouchItem.SIZE, false)) {
            return ItemStack.EMPTY;
        }
        if (moved.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        saveToStack();
        return original;
    }

    @Override
    public boolean stillValid(Player checkPlayer) {
        return checkPlayer.getItemInHand(hand).getItem() instanceof FocusPouchItem;
    }

    private static final class FocusSlot extends Slot {
        FocusSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ItemFocus;
        }
    }
}
