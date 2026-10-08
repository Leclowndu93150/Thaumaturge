package com.leclowndu93150.thaumaturge.content.essentia.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record ComponentEssentia<T>(
        ItemStack stack,
        Supplier<DataComponentType<T>> type,
        Function<T, AspectList> reader,
        Function<AspectList, T> writer)
        implements IItemEssentia {
    public static ComponentEssentia<EssentiaList> jar(ItemStack stack) {
        return new ComponentEssentia<>(
                stack, TTDataComponents.ESSENTIA_CONTENTS, EssentiaList::contents, EssentiaList::new);
    }

    public static ComponentEssentia<AspectList> phial(ItemStack stack) {
        return new ComponentEssentia<>(stack, TTDataComponents.ASPECTS, Function.identity(), Function.identity());
    }

    public static ComponentEssentia<AspectInstance> crystal(ItemStack stack) {
        return new ComponentEssentia<>(
                stack,
                TTDataComponents.CRYSTAL_ASPECT,
                AspectList::of,
                aspects -> aspects.entries().getFirst().withAmount(1));
    }

    @Override
    public AspectList getAspects() {
        T stored = stack.get(type.get());
        return stored == null ? AspectList.EMPTY : reader.apply(stored);
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects.isEmpty()) {
            stack.remove(type.get());
        } else {
            stack.set(type.get(), writer.apply(aspects));
        }
    }
}
