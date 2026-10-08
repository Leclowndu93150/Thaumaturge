package com.leclowndu93150.thaumaturge.compat.jei.ingredient;

import java.util.function.BiFunction;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record StringSubtypeInterpreter(BiFunction<ItemStack, UidContext, String> subtype)
        implements ISubtypeInterpreter<ItemStack> {
    @Override
    public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
        String value = subtype.apply(stack, context);
        return value.isEmpty() ? null : value;
    }

    @Override
    @Deprecated
    public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
        return subtype.apply(stack, context);
    }
}
