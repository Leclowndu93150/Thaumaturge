package com.leclowndu93150.thaumaturge.content.device.bore;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class BoreToolContainer implements Container {
    private final @Nullable ArcaneBoreHost host;

    public BoreToolContainer(@Nullable ArcaneBoreHost host) {
        this.host = host;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return host == null ? ItemStack.EMPTY : host.boreTool();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack held = getItem(slot);
        if (held.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack split = held.split(amount);
        if (held.isEmpty() && host != null) {
            host.setBoreTool(ItemStack.EMPTY);
        }
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack held = getItem(slot);
        if (host != null) {
            host.setBoreTool(ItemStack.EMPTY);
        }
        return held;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (host != null) {
            host.setBoreTool(stack);
        }
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return host != null && host.boreValid();
    }

    @Override
    public void clearContent() {
        if (host != null) {
            host.setBoreTool(ItemStack.EMPTY);
        }
    }
}
