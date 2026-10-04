package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/**
 * An arcane workbench recipe, carrying its vis and crystal essentia cost alongside the standard crafting shape.
 *
 * @since 1.0.0
 */
public interface IArcaneRecipe extends ResearchGated, Recipe<IArcaneCraftingInput> {
    /**
     * @return the vis one craft draws from the aura or a wand, before vis discounts
     */
    int visCost();

    /**
     * @return the crystal essentia one craft consumes from the workbench crystal slots; empty when the recipe needs none
     */
    AspectList crystalCost();

    /**
     * Returns what one craft leaves behind, such as empty buckets.
     *
     * @param input the input the recipe matched
     * @return the remainder for each input position; the first {@code width * height} entries
     *         follow the grid order of {@link IArcaneCraftingInput#getItem(int, int)}, and any
     *         further entries are ignored
     * @since 1.0.0
     */
    List<ItemStack> getRemainingItems(IArcaneCraftingInput input);
}
