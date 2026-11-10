package com.leclowndu93150.thaumaturge.api.items;

import java.util.Objects;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Immutable rules for matching item stacks, excluding their counts. */
public record ItemMatch(
        boolean anyDamage, boolean anyComponents, boolean componentSubset, boolean sharedTag, boolean sameMod) {
    public static final ItemMatch EXACT = new ItemMatch(false, false, false, false, false);
    public static final ItemMatch ITEM_OR_TAG = EXACT.byTag(true);

    public ItemMatch ignoringDamage(boolean ignore) {
        return new ItemMatch(ignore, anyComponents, componentSubset, sharedTag, sameMod);
    }

    public ItemMatch ignoringComponents(boolean ignore) {
        return new ItemMatch(anyDamage, ignore, componentSubset, sharedTag, sameMod);
    }

    public ItemMatch withComponentSubset(boolean subset) {
        return new ItemMatch(anyDamage, anyComponents, subset, sharedTag, sameMod);
    }

    public ItemMatch byTag(boolean enabled) {
        return new ItemMatch(anyDamage, anyComponents, componentSubset, enabled, sameMod);
    }

    public ItemMatch byMod(boolean enabled) {
        return new ItemMatch(anyDamage, anyComponents, componentSubset, sharedTag, enabled);
    }

    /** Tests candidate against the reference; tag and component-subset matching are directional. */
    public boolean test(ItemStack reference, ItemStack candidate) {
        if (reference.isEmpty() || candidate.isEmpty()) {
            return reference.isEmpty() && candidate.isEmpty();
        }
        if (sameMod && namespace(reference).equals(namespace(candidate))) {
            return true;
        }
        if (sharedTag
                && (reference.getItem() == candidate.getItem()
                        || reference.getTags().anyMatch(candidate::is))) {
            return true;
        }
        if (reference.getItem() != candidate.getItem()) {
            return false;
        }
        if (!anyDamage && reference.getDamageValue() != candidate.getDamageValue()) {
            return false;
        }
        if (anyComponents) {
            return true;
        }
        if (componentSubset) {
            return contained(reference.getComponents(), candidate.getComponents());
        }
        if (!anyDamage) {
            return ItemStack.isSameItemSameComponents(reference, candidate);
        }
        return contained(reference.getComponents(), candidate.getComponents())
                && contained(candidate.getComponents(), reference.getComponents());
    }

    private boolean contained(DataComponentMap subset, DataComponentMap superset) {
        for (TypedDataComponent<?> component : subset) {
            if (anyDamage && component.type() == DataComponents.DAMAGE) {
                continue;
            }
            if (!Objects.equals(component.value(), superset.get(component.type()))) {
                return false;
            }
        }
        return true;
    }

    private static String namespace(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }
}
