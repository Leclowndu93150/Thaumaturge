package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealFilterMode;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealFilterSpec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public final class SealFilterState implements ISealFilter {
    private final SealFilterSpec spec;
    private final NonNullList<ItemStack> stacks;
    private final NonNullList<Integer> limits;
    private boolean blacklist = true;

    public SealFilterState(SealFilterSpec spec) {
        this.spec = spec;
        this.stacks = NonNullList.withSize(spec.slots(), ItemStack.EMPTY);
        this.limits = NonNullList.withSize(spec.slots(), 0);
    }

    public static MapCodec<SealFilterState> codec(SealFilterSpec spec) {
        return RecordCodecBuilder.mapCodec(instance -> instance
                .group(ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("filter", List.of()).forGetter(SealFilterState::stacks),
                        Codec.BOOL.optionalFieldOf("bl", true).forGetter(state -> state.blacklist), Codec.INT.listOf().optionalFieldOf("sizes", List.of()).forGetter(SealFilterState::limits))
                .apply(instance, (stacks, blacklist, limits) -> restore(spec, stacks, blacklist, limits)));
    }

    private static SealFilterState restore(SealFilterSpec spec, List<ItemStack> stacks, boolean blacklist, List<Integer> limits) {
        SealFilterState state = new SealFilterState(spec);
        for (int slot = 0; slot < Math.min(stacks.size(), spec.slots()); slot++) {
            state.stacks.set(slot, stacks.get(slot).copyWithCount(Math.min(1, stacks.get(slot).getCount())));
        }
        for (int slot = 0; slot < Math.min(limits.size(), spec.slots()); slot++) {
            state.limits.set(slot, limits.get(slot));
        }
        state.blacklist = blacklist;
        return state;
    }

    @Override
    public SealFilterSpec spec() {
        return spec;
    }

    @Override
    public ItemStack stack(int slot) {
        return stacks.get(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        stacks.set(slot, stack.copy());
    }

    @Override
    public int limit(int slot) {
        return limits.get(slot);
    }

    @Override
    public void setLimit(int slot, int limit) {
        limits.set(slot, limit);
    }

    @Override
    public boolean isBlacklist() {
        return spec.mode() != SealFilterMode.WHITELIST_WITH_LIMITS && blacklist;
    }

    @Override
    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public List<ItemStack> stacks() {
        return Collections.unmodifiableList(stacks);
    }

    @Override
    public List<Integer> limits() {
        return Collections.unmodifiableList(limits);
    }
}
