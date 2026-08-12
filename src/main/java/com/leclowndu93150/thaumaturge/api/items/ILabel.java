package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Represents an item that acts as a label, capable of filtering by a specific {@link IAspect}.
 *
 * <p>Implementing this interface allows an item to carry an aspect filter, which can be used
 * to restrict or categorize operations (e.g., sorting, filtering) based on a particular aspect.
 */
public interface ILabel {

    /**
     * Returns the {@link IAspect} resource key that this label is filtering for, or {@code null}
     * if no aspect filter has been set on the given stack.
     *
     * @param stack the {@link ItemStack} to retrieve the aspect filter from
     * @return the filtered aspect's {@link ResourceKey}, or {@code null} if none is set
     */
    @Nullable
    ResourceKey<IAspect> getFilteredAspect(ItemStack stack);
}
