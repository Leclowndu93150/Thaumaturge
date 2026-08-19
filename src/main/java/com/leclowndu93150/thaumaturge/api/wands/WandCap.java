package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * A wand cap type. Caps determine how efficiently a wand spends its stored primal vis: the
 * cost of any drain is multiplied by the cap's cost modifier, so values below 1 are discounts.
 * A cap may declare special aspects that use a different modifier than the base one.
 *
 * @since 1.0.0
 */
public final class WandCap {
    /** The registry key for wand caps. */
    public static final ResourceKey<Registry<WandCap>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "wand_cap"));

    private final float baseCostModifier;
    private final List<ResourceKey<IAspect>> specialCostAspects;
    private final float specialCostModifier;
    private final int craftCost;
    private final Identifier texture;

    /**
     * @param baseCostModifier    the vis cost multiplier for aspects not listed as special
     * @param specialCostAspects  aspects that use {@code specialCostModifier} instead of the
     *                            base modifier; empty for none
     * @param specialCostModifier the vis cost multiplier for the special aspects; ignored when
     *                            no special aspects are declared
     * @param craftCost           the crafting cost factor of this cap, multiplied with the
     *                            rod's factor to price wand assembly
     * @param texture             the texture rendered on wand models built with this cap
     */
    public WandCap(float baseCostModifier, List<ResourceKey<IAspect>> specialCostAspects, float specialCostModifier, int craftCost, Identifier texture) {
        this.baseCostModifier = baseCostModifier;
        this.specialCostAspects = List.copyOf(specialCostAspects);
        this.specialCostModifier = specialCostModifier;
        this.craftCost = craftCost;
        this.texture = texture;
    }

    /**
     * @return the vis cost multiplier for aspects without a special modifier
     */
    public float baseCostModifier() {
        return baseCostModifier;
    }

    /**
     * @return aspects priced with {@link #specialCostModifier()}; empty when none
     */
    public List<ResourceKey<IAspect>> specialCostAspects() {
        return specialCostAspects;
    }

    /**
     * @return the vis cost multiplier applied to {@link #specialCostAspects()}
     */
    public float specialCostModifier() {
        return specialCostModifier;
    }

    /**
     * The cost multiplier this cap applies when spending the given aspect.
     *
     * @param aspect the primal aspect being spent
     * @return the applicable cost multiplier
     */
    public float costModifier(ResourceKey<IAspect> aspect) {
        return specialCostAspects.contains(aspect) ? specialCostModifier : baseCostModifier;
    }

    /**
     * @return the crafting cost factor used to price wand assembly recipes
     */
    public int craftCost() {
        return craftCost;
    }

    /**
     * @return the texture rendered on wand models built with this cap
     */
    public Identifier texture() {
        return texture;
    }
}
