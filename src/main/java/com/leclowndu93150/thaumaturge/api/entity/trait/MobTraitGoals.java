package com.leclowndu93150.thaumaturge.api.entity.trait;

import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Receives the AI goals a {@link MobTrait} injects into its carrier. The engine records every
 * goal it receives so it can remove exactly those goals when the trait goes away.
 *
 * @since 1.0.0
 */
public interface MobTraitGoals {
    /**
     * Adds a behaviour goal to the carrier's goal selector.
     *
     * @param priority the goal priority, lower runs first
     * @param goal     the goal instance, owned by this carrier only
     */
    void add(int priority, Goal goal);

    /**
     * Adds a targeting goal to the carrier's target selector.
     *
     * @param priority the goal priority, lower runs first
     * @param goal     the goal instance, owned by this carrier only
     */
    void addTarget(int priority, Goal goal);
}
