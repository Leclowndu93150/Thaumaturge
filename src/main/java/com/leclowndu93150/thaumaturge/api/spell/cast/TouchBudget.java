package com.leclowndu93150.thaumaturge.api.spell.cast;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

/**
 * The cap on how much one cast may touch per tick. Everything spawned from the same cast shares one
 * budget; it refills every game tick.
 *
 * @since 1.0.0
 */
public interface TouchBudget {
    /**
     * Claims one entity touch.
     *
     * @param entity the entity about to be affected
     * @return true when the touch fits the budget and the caller may proceed
     */
    boolean claim(Entity entity);

    /**
     * Claims one block touch.
     *
     * @param pos the block about to be affected
     * @return true when the touch fits the budget and the caller may proceed
     */
    boolean claim(BlockPos pos);
}
