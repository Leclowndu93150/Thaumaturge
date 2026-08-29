package com.leclowndu93150.thaumaturge.content.essentia.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaItemStorage;
import com.leclowndu93150.thaumaturge.api.essentia.ItemEssentiaTransferResult;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;

public final class SingleAspectItemStorage implements IEssentiaItemStorage {
    private final ItemStack stack;
    private final IEssentiaContainerItem container;
    private final int capacity;

    public SingleAspectItemStorage(ItemStack stack, IEssentiaContainerItem container, int capacity) {
        this.stack = stack.copy();
        this.container = container;
        this.capacity = capacity;
    }

    @Override
    public AspectList contents() {
        return container.getAspects(stack);
    }

    @Override
    public int capacity(Holder<IAspect> aspect) {
        return stack.getCount() == 1 ? capacity : 0;
    }

    @Override
    public boolean canInsert(Holder<IAspect> aspect) {
        if (stack.getCount() != 1) return false;
        AspectList contents = contents();
        return contents.isEmpty() || contents.amountOf(aspect) > 0;
    }

    @Override
    public ItemEssentiaTransferResult insert(Holder<IAspect> aspect, int amount, boolean simulate) {
        if (amount <= 0 || !canInsert(aspect)) return unchanged();
        AspectList contents = contents();
        int moved = Math.min(amount, capacity - contents.totalAmount());
        if (moved <= 0) return unchanged();
        ItemStack result = stack.copy();
        container.setAspects(result, contents.add(aspect, moved));
        return new ItemEssentiaTransferResult(moved, result);
    }

    @Override
    public ItemEssentiaTransferResult extract(Holder<IAspect> aspect, int amount, boolean simulate) {
        if (amount <= 0 || stack.getCount() != 1) return unchanged();
        AspectList contents = contents();
        int moved = Math.min(amount, contents.amountOf(aspect));
        if (moved <= 0) return unchanged();
        ItemStack result = stack.copy();
        container.setAspects(result, contents.remove(aspect, moved));
        return new ItemEssentiaTransferResult(moved, result);
    }

    private ItemEssentiaTransferResult unchanged() {
        return new ItemEssentiaTransferResult(0, stack);
    }
}
