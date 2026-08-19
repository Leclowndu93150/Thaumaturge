package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealGui;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public abstract class SealFiltered implements ISeal, ISealGui, ISealConfigFilter {
    protected static final ISealConfigToggles.SealToggle[] NO_TOGGLES = new ISealConfigToggles.SealToggle[0];

    protected NonNullList<ItemStack> filter = NonNullList.withSize(getFilterSize(), ItemStack.EMPTY);
    protected NonNullList<Integer> filterSize = NonNullList.withSize(getFilterSize(), 0);
    protected boolean blacklist = true;

    protected InvHelper.InvFilter filterFlags(ISealConfigToggles.SealToggle[] props) {
        return new InvHelper.InvFilter(!props[0].getValue(), !props[1].getValue(), props[2].getValue(), props[3].getValue());
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
        filter = NonNullList.withSize(getFilterSize(), ItemStack.EMPTY);
        Tag filterTag = nbt.get("filter");
        if (filterTag != null) {
            List<ItemStack> loaded = ItemStack.OPTIONAL_CODEC.listOf().parse(NbtOps.INSTANCE, filterTag).result().orElse(List.of());
            for (int i = 0; i < loaded.size() && i < filter.size(); i++) {
                ItemStack stack = loaded.get(i);
                if (stack.getCount() > 1) {
                    stack.setCount(1);
                }
                filter.set(i, stack);
            }
        }
        blacklist = nbt.getBooleanOr("bl", true);
        filterSize = NonNullList.withSize(getFilterSize(), 0);
        nbt.getIntArray("sizes").ifPresent(sizes -> {
            for (int i = 0; i < sizes.length && i < filterSize.size(); i++) {
                filterSize.set(i, sizes[i]);
            }
        });
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
        Tag filterTag = ItemStack.OPTIONAL_CODEC.listOf().encodeStart(NbtOps.INSTANCE, new ArrayList<>(filter)).result().orElse(null);
        if (filterTag != null) {
            nbt.put("filter", filterTag);
        }
        nbt.putBoolean("bl", blacklist);
        int[] sizes = new int[filterSize.size()];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = filterSize.get(i);
        }
        nbt.putIntArray("sizes", sizes);
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_PRIORITY};
    }

    @Override
    public int getFilterSize() {
        return 1;
    }

    @Override
    public List<ItemStack> getInv() {
        return filter;
    }

    @Override
    public List<Integer> getSizes() {
        return filterSize;
    }

    @Override
    public ItemStack getFilterSlot(int slot) {
        return filter.get(slot);
    }

    @Override
    public int getFilterSlotSize(int slot) {
        return filterSize.get(slot);
    }

    @Override
    public void setFilterSlot(int slot, ItemStack stack) {
        filter.set(slot, stack.copy());
    }

    @Override
    public void setFilterSlotSize(int slot, int size) {
        filterSize.set(slot, size);
    }

    @Override
    public boolean isBlacklist() {
        return blacklist;
    }

    @Override
    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public boolean hasStacksizeLimiters() {
        return false;
    }
}
