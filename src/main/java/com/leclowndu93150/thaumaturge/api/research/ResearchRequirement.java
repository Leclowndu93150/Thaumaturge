package com.leclowndu93150.thaumaturge.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * A single obtain or craft requirement on a {@link IResearchStage}.
 *
 * <p>The requirement matches any item from {@code items}, accepting either a tag reference or
 * one or more direct item references. The player must obtain or craft at least {@code amount}
 * matching items, counted in aggregate across the matched set.
 *
 * <p>{@code components} narrows the match further. Every declared component must be present on
 * the candidate stack and equal to the declared value, and a component declared as removed must
 * be absent. Enchantment components are the one exception: they match as a lower bound, so a
 * requirement for Fortune I is satisfied by any level of Fortune and ignores whatever other
 * enchantments the stack carries.
 *
 * @param items items that satisfy this requirement
 * @param components component values a candidate stack must carry
 * @param amount the minimum aggregate count, must be positive
 * @since 1.0.0
 */
public record ResearchRequirement(HolderSet<Item> items, DataComponentPatch components, int amount) {
    /** Codec for datapack serialization. */
    public static final Codec<ResearchRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM)
                            .fieldOf("items")
                            .forGetter(ResearchRequirement::items),
                    DataComponentPatch.CODEC
                            .optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(ResearchRequirement::components),
                    Codec.INT.fieldOf("amount").forGetter(ResearchRequirement::amount))
            .apply(instance, ResearchRequirement::new));

    /**
     * Returns whether the given stack counts towards this requirement.
     *
     * @param stack the candidate stack, may be empty
     * @return {@code true} when the stack's item and components both satisfy the requirement
     */
    public boolean matches(ItemStack stack) {
        return items.contains(stack.getItem().builtInRegistryHolder()) && matchesComponents(stack);
    }

    private boolean matchesComponents(ItemStack stack) {
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            Optional<?> required = entry.getValue();
            if (required.isEmpty()) {
                if (stack.has(type)) {
                    return false;
                }
                continue;
            }
            Object value = required.get();
            Object present = stack.get(type);
            if (value instanceof ItemEnchantments enchantments) {
                if (!hasAtLeast(enchantments, present)) {
                    return false;
                }
                continue;
            }
            if (!value.equals(present)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasAtLeast(ItemEnchantments required, Object present) {
        ItemEnchantments held =
                present instanceof ItemEnchantments enchantments ? enchantments : ItemEnchantments.EMPTY;
        for (Holder<Enchantment> enchantment : required.keySet()) {
            if (held.getLevel(enchantment) < required.getLevel(enchantment)) {
                return false;
            }
        }
        return true;
    }
}
