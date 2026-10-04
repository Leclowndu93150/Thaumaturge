package com.leclowndu93150.thaumaturge.api.golems.tasks;

import net.minecraft.core.BlockPos;

/**
 * What a task points a golem at.
 *
 * @since 1.0.0
 */
public sealed interface TaskTarget permits BlockTarget, EntityTarget {
    /**
     * @return the block the golem walks to; for an entity target, the block the entity currently stands in
     */
    BlockPos pos();
}
