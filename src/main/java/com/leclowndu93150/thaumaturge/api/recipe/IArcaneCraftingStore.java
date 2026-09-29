package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/**
 * The item side of an arcane craft: the storage that holds the crafting grid, the crystals and
 * the wand that an {@link IArcaneCraftingInput} was read from. The craft checks the vis and aura
 * payment, asks the store whether it can apply a {@link Consumption}, and only then pays and has
 * the store apply it.
 *
 * <p>The crafted output is not part of the consumption; the caller receives it in
 * {@link ArcaneCraftingTransaction.Result#output()} and places it itself.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IArcaneCraftingStore {
    /**
     * Applies one craft to the storage, or checks that it could.
     *
     * <p>For each grid position the store removes one item and puts the matching remainder in its
     * place, or elsewhere when that position still holds items. It removes the listed crystals and
     * replaces its wand with {@link Consumption#wand()}.
     *
     * @param consumption what the craft uses up
     * @param simulate    true to only check that the storage still matches {@link Consumption#grid()}
     *                    and holds the crystals, without changing it
     * @return true when the storage still matched the consumption (and, when not simulating, the
     *         change was applied); false when the storage changed since the input was read, in which
     *         case the craft fails
     */
    boolean consume(Consumption consumption, boolean simulate);

    /**
     * What one craft uses up. All stacks are copies owned by the receiver.
     *
     * @param grid       the crafting grid the recipe matched, trimmed to the pattern's bounding box
     *                   and indexed {@code x + y * width} like {@link IArcaneCraftingInput#getItem(int, int)}
     * @param remainders the item left behind at each grid position, indexed like {@code grid}
     * @param crystals   the essentia crystals to remove, one per aspect unit
     * @param wand       the wand as it must be after the craft; equal to the input's wand when the
     *                   craft draws no wand vis, and empty when there is no wand
     * @since 1.0.0
     */
    record Consumption(List<ItemStack> grid, List<ItemStack> remainders, AspectList crystals, ItemStack wand) {
        /**
         * Copies every stack so the receiver owns them.
         *
         * @throws NullPointerException when a component is null
         */
        public Consumption {
            grid = grid.stream().map(ItemStack::copy).toList();
            remainders = remainders.stream().map(ItemStack::copy).toList();
            Objects.requireNonNull(crystals, "crystals");
            wand = wand.copy();
        }
    }
}
