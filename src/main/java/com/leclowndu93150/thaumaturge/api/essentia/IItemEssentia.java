package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;

/**
 * The essentia carried by one item stack: phials, filled jars, essentia crystals, mana beans.
 *
 * <p>Obtained through the {@link EssentiaCapabilities#CONTAINER} item capability, which returns a view bound to the queried stack.
 * Writes go straight into that stack's data components; to change another stack, query its own view.
 *
 * @since 1.0.0
 */
public interface IItemEssentia {
    /**
     * @return the carried aspects; an empty list when the stack carries none
     */
    AspectList getAspects();

    /**
     * Replaces the carried aspects on the bound stack. An empty list clears them.
     *
     * @param aspects the new contents, never null
     */
    void setAspects(AspectList aspects);

    /**
     * @return whether the carried aspects count toward the stack's own aspects when it is scanned or valued; true by default
     */
    default boolean countsTowardItemAspects() {
        return true;
    }
}
