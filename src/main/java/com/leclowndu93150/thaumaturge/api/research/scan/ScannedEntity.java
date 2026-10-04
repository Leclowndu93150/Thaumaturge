package com.leclowndu93150.thaumaturge.api.research.scan;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * A scan aimed at an entity.
 *
 * @param entity the entity
 * @since 1.0.0
 */
public record ScannedEntity(Entity entity) implements ScanTarget {
    @Override
    public @Nullable Entity creature() {
        return entity instanceof ItemEntity ? null : entity;
    }

    @Override
    public ItemStack carriedStack() {
        return entity instanceof ItemEntity item ? item.getItem() : ItemStack.EMPTY;
    }
}
