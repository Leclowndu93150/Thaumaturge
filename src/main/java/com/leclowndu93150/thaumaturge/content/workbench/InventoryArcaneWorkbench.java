package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

public class InventoryArcaneWorkbench extends SimpleContainer implements IArcaneWorkbench, CraftingContainer {
    public static final int CRAFTING_SLOTS = 9;
    public static final int CRYSTAL_SLOTS = 6;
    public static final int WAND_SLOT = CRAFTING_SLOTS + CRYSTAL_SLOTS;
    public static final int SIZE = WAND_SLOT + 1;

    private final List<Runnable> changeListeners = new ArrayList<>();

    public InventoryArcaneWorkbench() {
        super(SIZE);
    }

    @Override
    public ItemStack wandStack() {
        return getItem(WAND_SLOT);
    }

    @Override
    public AspectList availableCrystals() {
        AspectList crystals = AspectList.EMPTY;
        for (int slot = CRAFTING_SLOTS; slot < WAND_SLOT; slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemEssentiaCrystal)) {
                continue;
            }
            Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(stack);
            if (aspect != null) {
                crystals = crystals.add(aspect, stack.getCount());
            }
        }
        return crystals;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == WAND_SLOT) {
            return SlotWorkbenchWand.isUsableWand(stack);
        }
        return super.canPlaceItem(slot, stack);
    }

    public void addChangedListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangedListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public CraftingInput asCraftInput() {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(getItem(i));
        }
        return CraftingInput.of(3, 3, grid);
    }

    public ArcaneCraftingInput asArcaneCraftInput() {
        return ArcaneCraftingInput.of(3, 3, getItems());
    }

    public ArcaneCraftingInput.Positioned asPositionedArcaneCraftInput() {
        return ArcaneCraftingInput.ofPositioned(3, 3, getItems());
    }
}
