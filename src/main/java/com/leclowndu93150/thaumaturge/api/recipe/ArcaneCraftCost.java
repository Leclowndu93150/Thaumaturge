package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

/**
 * The computed cost of crafting an {@link IArcaneRecipe} at a workbench for a given player, and
 * where that cost is paid from.
 *
 * <p>An arcane craft is paid in three ways, resolved in order: primal vis drawn from a wand in the
 * workbench's wand slot, essentia crystals consumed from the crystal slots, and aura vis drained
 * from the block entity. This record reports the outcome of that resolution so addons can display
 * the cost, decide affordability, or drive automation without recomputing the rules.
 *
 * <p>Instances are produced by {@link #of(IArcaneRecipe, IArcaneWorkbench, Player)} and are
 * delivered mutably-replaceable through {@link ArcaneCraftCostEvent}. All fields are immutable
 * views.
 *
 * @param paidFromWand    whether the whole craft is covered by wand vis, needing no crystals
 * @param wandCentivis    the per-aspect centivis to draw from the wand, keyed by aspect
 * @param crystalsNeeded  the crystal aspects that must be consumed from the crystal slots
 * @param auraVis         the vis to drain from the block entity's aura buffer
 * @param affordable      whether the workbench and player can currently satisfy the whole cost
 * @since 1.0.0
 */
public record ArcaneCraftCost(boolean paidFromWand, Map<ResourceKey<IAspect>, Integer> wandCentivis, AspectList crystalsNeeded, int auraVis, boolean affordable) {
    private static Factory factory;

    public ArcaneCraftCost {
        wandCentivis = Map.copyOf(wandCentivis);
    }

    /**
     * Computes the cost of crafting {@code recipe} at {@code workbench} for {@code player}.
     *
     * @param recipe    the arcane recipe
     * @param workbench the workbench inventory holding crystals and the wand
     * @param player    the crafting player, whose wand and gear affect the cost
     * @return the resolved cost
     * @throws IllegalStateException when accessed before the implementation has bound the factory
     */
    public static ArcaneCraftCost of(IArcaneRecipe recipe, IArcaneWorkbench workbench, Player player) {
        if (factory == null) {
            throw new IllegalStateException("ArcaneCraftCost accessed before binding");
        }
        return factory.compute(recipe, workbench, player);
    }

    /**
     * Binds the cost factory. Called once at mod init by Thaumaturge; addons must not call this.
     *
     * @param impl the factory implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Factory impl) {
        if (factory != null) {
            throw new IllegalStateException("ArcaneCraftCost already bound");
        }
        factory = impl;
    }

    /**
     * Implementation hook supplied by Thaumaturge that computes a cost from a recipe, workbench, and
     * player. Addons must not implement this interface.
     *
     * @since 1.0.0
     */
    public interface Factory {
        /**
         * Computes the cost for the given inputs.
         *
         * @param recipe    the arcane recipe
         * @param workbench the workbench inventory
         * @param player    the crafting player
         * @return the resolved cost
         */
        ArcaneCraftCost compute(IArcaneRecipe recipe, IArcaneWorkbench workbench, Player player);
    }
}
