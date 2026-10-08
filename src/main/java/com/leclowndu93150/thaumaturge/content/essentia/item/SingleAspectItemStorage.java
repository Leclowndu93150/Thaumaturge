package com.leclowndu93150.thaumaturge.content.essentia.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaItemStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import com.leclowndu93150.thaumaturge.api.essentia.ItemEssentiaTransferResult;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SingleAspectItemStorage implements IEssentiaItemStorage {
    private static final float FEEDBACK_VOLUME = 0.25F;

    private final ItemStack stack;
    private final Function<ItemStack, ? extends IItemEssentia> view;
    private final int capacity;
    private final boolean wholeLoads;

    public SingleAspectItemStorage(
            ItemStack stack, Function<ItemStack, ? extends IItemEssentia> view, int capacity, boolean wholeLoads) {
        this.stack = stack.copy();
        this.view = view;
        this.capacity = capacity;
        this.wholeLoads = wholeLoads;
    }

    @Override
    public AspectList contents() {
        return view.apply(stack).getAspects();
    }

    @Override
    public int capacity(Holder<IAspect> aspect) {
        return stack.getCount() == 1 && passesFilter(aspect) ? capacity : 0;
    }

    @Override
    public boolean canInsert(Holder<IAspect> aspect) {
        if (stack.getCount() != 1 || !passesFilter(aspect)) {
            return false;
        }
        AspectList contents = contents();
        return contents.isEmpty() || contents.amountOf(aspect) > 0 && contents.totalAmount() < capacity && !wholeLoads;
    }

    @Override
    public ItemEssentiaTransferResult insert(Holder<IAspect> aspect, int amount) {
        if (amount <= 0 || !canInsert(aspect)) {
            return unchanged();
        }
        AspectList contents = contents();
        int moved = Math.min(amount, capacity - contents.totalAmount());
        if (moved <= 0 || wholeLoads && moved < capacity) {
            return unchanged();
        }
        ItemStack result = stack.copy();
        view.apply(result).setAspects(contents.add(aspect, moved));
        return new ItemEssentiaTransferResult(moved, result);
    }

    @Override
    public ItemEssentiaTransferResult extract(Holder<IAspect> aspect, int amount) {
        if (amount <= 0 || stack.getCount() != 1) {
            return unchanged();
        }
        AspectList contents = contents();
        int stored = contents.amountOf(aspect);
        int moved = Math.min(amount, stored);
        if (moved <= 0 || wholeLoads && moved < stored) {
            return unchanged();
        }
        ItemStack result = stack.copy();
        view.apply(result).setAspects(contents.reduce(aspect, moved));
        return new ItemEssentiaTransferResult(moved, result);
    }

    @Override
    public void playTransferFeedback(Player player, TransferDirection direction) {
        player.level()
                .playSound(
                        null, player.blockPosition(), TTSounds.JAR.get(), SoundSource.PLAYERS, FEEDBACK_VOLUME, 1.0F);
    }

    private boolean passesFilter(Holder<IAspect> aspect) {
        ResourceKey<IAspect> filter = stack.get(TTDataComponents.ASPECT_FILTER.get());
        return filter == null || aspect.is(filter);
    }

    private ItemEssentiaTransferResult unchanged() {
        return new ItemEssentiaTransferResult(0, stack);
    }
}
