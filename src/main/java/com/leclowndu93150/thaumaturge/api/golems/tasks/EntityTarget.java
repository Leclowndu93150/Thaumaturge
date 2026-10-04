package com.leclowndu93150.thaumaturge.api.golems.tasks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

/**
 * A task aimed at an entity. The target moves with the entity.
 *
 * @param entity the entity
 * @since 1.0.0
 */
public record EntityTarget(Entity entity) implements TaskTarget {
    @Override
    public BlockPos pos() {
        return entity.blockPosition();
    }
}
