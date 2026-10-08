package com.leclowndu93150.thaumaturge.content.workbench;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SlotArcaneResult extends Slot {
    private final MenuArcaneWorkbench menu;
    private int amountCrafted;

    public SlotArcaneResult(ResultContainer result, MenuArcaneWorkbench menu, int x, int y) {
        super(result, 0, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        return menu.craftsOnServer() ? menu.takeCraft() : super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        amountCrafted += amount;
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        amountCrafted += numItemsCrafted;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        amountCrafted = 0;
    }
}
