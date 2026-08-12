package com.leclowndu93150.thaumaturge.content.recipe.label;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class LabelFilterRecipe extends CustomRecipe {

    public static final LabelFilterRecipe INSTANCE = new LabelFilterRecipe();

    public static final RecipeSerializer<LabelFilterRecipe> SERIALIZER =
            new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return checkAndGetAspectFromInput(input) != null;
    }

    private @Nullable Holder<IAspect> checkAndGetAspectFromInput(CraftingInput input) {
        boolean hasLabel = false;
        Holder<IAspect> aspect = null;
        for (ItemStack stack : input.items()) {
            if (stack.is(TCItems.LABEL)) {
                if (hasLabel) return null;
                hasLabel = true;
                continue;
            }

            if (stack.is(TCItems.PHIAL)) {
                if (aspect != null) return null;
                if (!(stack.getItem() instanceof IEssentiaContainerItem it)
                        || it.getAspects(stack).isEmpty()) return null;
                aspect = it.getAspects(stack).entries().getFirst().aspect();
            }
        }
        if (!hasLabel) return null;
        return aspect;
    }

    @Override
    public @NonNull ItemStack assemble(CraftingInput craftingInput) {
        Holder<IAspect> aspect = checkAndGetAspectFromInput(craftingInput);
        if (aspect == null) return ItemStack.EMPTY;
        return LabelItem.withAspect(aspect);
    }

    @Override
    public @NonNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NonNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int slot = 0; slot < result.size(); ++slot) {
            ItemStack item = input.getItem(slot);
            ItemStackTemplate remainder = item.getCraftingRemainder();
            if (item.is(TCItems.PHIAL)) result.set(slot, item.copyWithCount(1));
            else result.set(slot, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }

        return result;
    }
}
