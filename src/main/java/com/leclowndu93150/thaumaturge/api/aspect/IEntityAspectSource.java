package com.leclowndu93150.thaumaturge.api.aspect;

/**
 * Supplies per-instance aspects for an entity whose composition cannot be expressed by the
 * static {@code thaumaturge:entity_aspects} data map, such as a mob whose aspects depend on
 * synced entity data.
 *
 * <p>Implement this on the entity class. Aspect resolution consults this interface before the
 * data map, so an implementation fully replaces the type-level entry.
 *
 * @since 1.0.0
 */
public interface IEntityAspectSource {
    /**
     * The aspects this entity instance currently carries.
     *
     * @return the aspects, or {@link AspectList#EMPTY} when none apply
     */
    AspectList entityAspects();
}
