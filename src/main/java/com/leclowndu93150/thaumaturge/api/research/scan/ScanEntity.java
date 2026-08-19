package com.leclowndu93150.thaumaturge.api.research.scan;

import java.util.function.Predicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching entities either by exact {@link EntityType} or by class hierarchy.
 * An optional extra filter narrows matches further; it replaces the legacy NBT matcher.
 *
 * @since 1.0.0
 */
public class ScanEntity implements IScanThing {
    private final Identifier research;
    private final @Nullable EntityType<?> entityType;
    private final @Nullable Class<?> entityClass;
    private final boolean inheritedClasses;
    private final @Nullable Predicate<Entity> filter;

    /**
     * Creates a subject matching one exact entity type.
     *
     * @param research the research key granted on scan
     * @param entityType the entity type to match
     */
    public ScanEntity(Identifier research, EntityType<?> entityType) {
        this(research, entityType, null, false, null);
    }

    /**
     * Creates a subject matching an entity class.
     *
     * @param research the research key granted on scan
     * @param entityClass the class or interface to match
     * @param inheritedClasses whether subclasses and implementors match too
     */
    public ScanEntity(Identifier research, Class<?> entityClass, boolean inheritedClasses) {
        this(research, null, entityClass, inheritedClasses, null);
    }

    /**
     * Creates a subject matching an entity class with an extra per-entity filter.
     *
     * @param research the research key granted on scan
     * @param entityClass the class or interface to match
     * @param inheritedClasses whether subclasses and implementors match too
     * @param filter additional predicate an entity must pass to match
     */
    public ScanEntity(Identifier research, Class<?> entityClass, boolean inheritedClasses, Predicate<Entity> filter) {
        this(research, null, entityClass, inheritedClasses, filter);
    }

    private ScanEntity(Identifier research, @Nullable EntityType<?> entityType, @Nullable Class<?> entityClass, boolean inheritedClasses, @Nullable Predicate<Entity> filter) {
        this.research = research;
        this.entityType = entityType;
        this.entityClass = entityClass;
        this.inheritedClasses = inheritedClasses;
        this.filter = filter;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        if (!(target instanceof Entity entity)) {
            return false;
        }
        boolean matches;
        if (entityType != null) {
            matches = entity.getType() == entityType;
        } else if (entityClass != null) {
            matches = inheritedClasses ? entityClass.isInstance(entity) : entityClass == entity.getClass();
        } else {
            matches = false;
        }
        return matches && (filter == null || filter.test(entity));
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
