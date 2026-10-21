package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An ingredient and assembly policy for one registered rod or cap. Ingredients use the normal
 * recipe codec, including NeoForge component predicates. Research requirements are conjunctive
 * with the other part and the rod's intrinsic assembly research. Variants must be opted into;
 * staff rods can only produce staves, even when another variant is requested.
 *
 * @since 1.0.0
 */
public record WandAssemblyPart<T>(ResourceKey<T> key, Ingredient ingredient, List<ResearchGate> research, Set<Variant> variants, boolean enabled) {
    public WandAssemblyPart {
        Objects.requireNonNull(key);
        Objects.requireNonNull(ingredient);
        research = List.copyOf(research);
        variants = Set.copyOf(variants);
    }

    public enum Variant {
        WAND, STAFF, SCEPTRE
    }
}
