package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import net.minecraft.world.item.ItemStack;

/**
 * Callback attached to a {@link WandRod} that runs when an arcane workbench recipe assembles a wand,
 * sceptre or staff from that rod. Use it to carry data from the rod item, or any other grid item,
 * onto the finished wand.
 *
 * <p>The callback runs on the logical server every time the workbench builds the result, including
 * the preview shown in the result slot, so the same input can be seen more than once. It must only
 * modify {@code wand}, never the grid.
 *
 * @since 1.0.0
 */
public interface IWandRodOnAssemble {
    /**
     * Called after the recipe has built the wand and before it reaches the result slot.
     *
     * @param wand  the freshly assembled wand stack, modified in place
     * @param input the crafting grid the wand was assembled from; read only
     */
    void onAssemble(ItemStack wand, IArcaneCraftingInput input);
}
