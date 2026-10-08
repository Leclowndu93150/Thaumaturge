package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.content.menu.AbstractHeldItemMenu;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuFocusPouch extends AbstractHeldItemMenu {
    private static final int POUCH_COLUMNS = 6;
    private static final int POUCH_SLOT_X = 40;
    private static final int POUCH_SLOT_Y = 51;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_X_SPACING = -1;
    private static final int SLOT_Y_SPACING = -1;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 151;

    private final SimpleContainer pouchInventory = new SimpleContainer(FocusPouchItem.SIZE);

    public MenuFocusPouch(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public MenuFocusPouch(int containerId, Inventory inventory, InteractionHand hand) {
        super(TTMenus.FOCUS_POUCH.get(), containerId, inventory, hand);
        NonNullList<ItemStack> stored = FocusPouchItem.getInventory(held());
        for (int slot = 0; slot < stored.size(); slot++) {
            pouchInventory.setItem(slot, stored.get(slot));
        }
        for (int slot = 0; slot < FocusPouchItem.SIZE; slot++) {
            addSlot(new FocusSlot(
                    pouchInventory,
                    slot,
                    POUCH_SLOT_X + slot % POUCH_COLUMNS * (SLOT_SIZE + SLOT_X_SPACING),
                    POUCH_SLOT_Y + slot / POUCH_COLUMNS * (SLOT_SIZE + SLOT_Y_SPACING)));
        }
        addStandardInventorySlots(inventory, PLAYER_INV_X, PLAYER_INV_Y);
    }

    @Override
    protected void writeBack(ItemStack pouch) {
        if (pouch.getItem() instanceof FocusPouchItem) {
            NonNullList<ItemStack> list = NonNullList.withSize(FocusPouchItem.SIZE, ItemStack.EMPTY);
            for (int slot = 0; slot < list.size(); slot++) {
                list.set(slot, pouchInventory.getItem(slot));
            }
            FocusPouchItem.setInventory(pouch, list);
        }
    }

    @Override
    protected ItemStack quickMove(Player player, int index) {
        return quickMoveBetween(index, FocusPouchItem.SIZE, FocusItems::isFocus);
    }

    private static final class FocusSlot extends Slot {
        FocusSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return FocusItems.isFocus(stack);
        }
    }
}
