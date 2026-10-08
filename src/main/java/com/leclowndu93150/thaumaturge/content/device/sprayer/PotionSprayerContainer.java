package com.leclowndu93150.thaumaturge.content.device.sprayer;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class PotionSprayerContainer implements Container {
    private static final int POTION_SLOT = 0;
    private static final int SIZE = 1;

    private final BlockEntityPotionSprayer sprayer;

    PotionSprayerContainer(BlockEntityPotionSprayer sprayer) {
        this.sprayer = sprayer;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return sprayer.getPotion().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == POTION_SLOT ? sprayer.getPotion() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack potion = sprayer.getPotion();
        if (slot != POTION_SLOT || potion.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = potion.split(count);
        sprayer.setPotion(potion.isEmpty() ? ItemStack.EMPTY : potion);
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack potion = getItem(slot);
        if (!potion.isEmpty()) {
            sprayer.setPotion(ItemStack.EMPTY);
        }
        return potion;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == POTION_SLOT) {
            sprayer.setPotion(stack);
        }
    }

    @Override
    public int getMaxStackSize() {
        return SIZE;
    }

    @Override
    public void setChanged() {
        sprayer.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return !sprayer.isRemoved();
    }

    @Override
    public void clearContent() {
        sprayer.setPotion(ItemStack.EMPTY);
    }
}
