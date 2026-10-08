package com.leclowndu93150.thaumaturge.content.menu;

import java.util.function.Predicate;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTTMenu extends AbstractContainerMenu {
    protected AbstractTTMenu(@Nullable MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    protected final ItemStack quickMoveBetween(int index, int machineSlots, Predicate<ItemStack> machineAccepts) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack moved = slot.getItem();
        ItemStack original = moved.copy();
        if (index < machineSlots) {
            if (!moveItemStackTo(moved, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!machineAccepts.test(moved) || !moveItemStackTo(moved, 0, machineSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (moved.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    protected final void addInventoryExtendedSlots(Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
    }

    protected final void addInventoryHotbarSlots(Inventory inventory, int x, int y) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * 18, y));
        }
    }
}
