package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public interface InfusionJobRecipe extends IInfusionRecipe, Recipe<InfusionInput> {

    default @Nullable List<ItemStack> jobComponents(InfusionInput input) {
        return matchComponents(input.components());
    }

    default AspectList jobEssentia(InfusionInput input) {
        return aspects();
    }

    default int jobInstability(InfusionInput input) {
        return instability();
    }

    default ItemStack jobResult(InfusionInput input, RandomSource random, HolderLookup.Provider registries) {
        return assemble(input, registries);
    }

    default boolean exactResult() {
        return true;
    }
}
