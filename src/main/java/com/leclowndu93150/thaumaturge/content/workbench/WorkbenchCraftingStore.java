package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WorkbenchCraftingStore implements IArcaneCraftingStore {
    private static final int GRID_WIDTH = 3;

    private final InventoryArcaneWorkbench inventory;
    private final int left;
    private final int top;
    private final int width;
    private final int height;
    private final Consumer<ItemStack> overflowSink;

    public WorkbenchCraftingStore(
            InventoryArcaneWorkbench inventory, Player player, int left, int top, int width, int height) {
        this(inventory, player, left, top, width, height, player.getInventory()::placeItemBackInInventory);
    }

    WorkbenchCraftingStore(
            InventoryArcaneWorkbench inventory,
            Player player,
            int left,
            int top,
            int width,
            int height,
            Consumer<ItemStack> overflowSink) {
        this.overflowSink = overflowSink;
        this.inventory = inventory;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean consume(Consumption consumption, boolean simulate) {
        if (!gridMatches(consumption.grid())) {
            return false;
        }
        List<ItemStack> updated = new ArrayList<>(InventoryArcaneWorkbench.SIZE);
        for (int slot = 0; slot < InventoryArcaneWorkbench.SIZE; slot++) {
            updated.add(inventory.getItem(slot).copy());
        }
        List<ItemStack> overflow = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int compact = x + y * width;
                int slot = x + left + (y + top) * GRID_WIDTH;
                ItemStack remainder = compact < consumption.remainders().size()
                        ? consumption.remainders().get(compact).copy()
                        : ItemStack.EMPTY;
                updated.set(slot, consumeOne(updated.get(slot), remainder, overflow));
            }
        }
        if (!consumeCrystals(consumption, updated)) {
            return false;
        }
        if (!ItemStack.matches(inventory.wandStack(), consumption.wand())) {
            updated.set(InventoryArcaneWorkbench.WAND_SLOT, consumption.wand().copy());
        }
        if (!simulate) {
            inventory.applyCraft(updated);
            overflow.forEach(overflowSink);
        }
        return true;
    }

    private boolean gridMatches(List<ItemStack> grid) {
        if (grid.size() != width * height) {
            return false;
        }
        for (int slot = 0; slot < InventoryArcaneWorkbench.CRAFTING_SLOTS; slot++) {
            int x = slot % GRID_WIDTH - left;
            int y = slot / GRID_WIDTH - top;
            boolean inside = x >= 0 && x < width && y >= 0 && y < height;
            ItemStack expected = inside ? grid.get(x + y * width) : ItemStack.EMPTY;
            if (!ItemStack.matches(inventory.getItem(slot), expected)) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack consumeOne(ItemStack current, ItemStack remainder, List<ItemStack> overflow) {
        ItemStack left = shrunk(current, 1);
        if (remainder.isEmpty()) {
            return left;
        }
        if (left.isEmpty()) {
            return remainder;
        }
        if (ItemStack.isSameItemSameComponents(left, remainder)) {
            remainder.grow(left.getCount());
            return remainder;
        }
        overflow.add(remainder);
        return left;
    }

    private boolean consumeCrystals(Consumption consumption, List<ItemStack> updated) {
        for (AspectInstance entry : consumption.crystals().entries()) {
            int needed = entry.amount();
            for (int slot = InventoryArcaneWorkbench.CRAFTING_SLOTS;
                    slot < InventoryArcaneWorkbench.WAND_SLOT && needed > 0;
                    slot++) {
                ItemStack crystal = updated.get(slot);
                if (crystal.isEmpty() || !(crystal.getItem() instanceof ItemEssentiaCrystal)) {
                    continue;
                }
                Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(crystal);
                if (aspect != null
                        && aspect.value().tag().equals(entry.aspect().value().tag())) {
                    int removed = Math.min(needed, crystal.getCount());
                    updated.set(slot, shrunk(crystal, removed));
                    needed -= removed;
                }
            }
            if (needed > 0) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack shrunk(ItemStack stack, int amount) {
        return stack.getCount() <= amount ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - amount);
    }
}
