package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;

/**
 * A research category. Categories partition the Thaumonomicon into thematic tabs and
 * supply the formula used to convert scanned aspects into category progress.
 *
 * <p>Categories are loaded from the {@link #REGISTRY_KEY} datapack registry under
 * {@code data/<namespace>/thaumaturge/research_category/}. Code typically resolves a category by
 * {@link ResourceKey} and looks up the holder through a registry access.
 *
 * @since 1.0.0
 */
public interface IResearchCategory {
    /** Datapack registry key for research categories. */
    ResourceKey<Registry<IResearchCategory>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "research_category"));

    /**
     * Identifier of the research entry that gates access to this category in the Thaumonomicon.
     * When absent the category is unconditionally visible.
     *
     * @return the gating research entry identifier, or empty
     */
    Optional<Identifier> requiredResearch();

    /**
     * Aspect-weighted formula used to convert scanned aspect amounts into raw category knowledge.
     *
     * @return the formula, never null
     */
    AspectList formula();

    /**
     * Converts an aspect composition into raw category knowledge using this category's
     * {@link #formula() formula}. Each formula aspect contributes the scanned amount weighted by
     * a tenth of the formula amount; the result is the square root of the weighted sum, rounded
     * up.
     *
     * @param aspects the scanned aspect composition
     * @return the raw knowledge points earned, non-negative
     */
    default int applyFormula(AspectList aspects) {
        return applyFormula(aspects, 1.0);
    }

    /**
     * Converts an aspect composition into raw category knowledge with a multiplier applied
     * inside the formula. The multiplier is squared before weighting so it scales the
     * pre-root sum.
     *
     * @param aspects the scanned aspect composition
     * @param modifier the multiplier
     * @return the raw knowledge points earned, non-negative
     */
    default int applyFormula(AspectList aspects, double modifier) {
        double total = 0.0;
        for (AspectInstance entry : formula().entries()) {
            total += modifier * modifier * aspects.amountOf(entry.aspect()) * (entry.amount() / 10.0);
        }
        if (total > 0.0) {
            total = Math.sqrt(total);
        }
        return Mth.ceil(total);
    }

    /**
     * Identifier of the icon sprite for this category's tab.
     *
     * @return the texture identifier
     */
    Identifier icon();

    /**
     * Identifier of the background sprite drawn behind the category's entry grid.
     *
     * @return the texture identifier
     */
    Identifier background();

    /**
     * Identifier of an optional second background sprite drawn over the primary background.
     *
     * @return the texture identifier, or empty when the category uses a single layer
     */
    Optional<Identifier> overlayBackground();

    /**
     * An index used for ordering categories in the Thaumonomicon. Categories with lower indices are drawn first.
     *
     * @return the index, non-negative
     */
    int index();
}
