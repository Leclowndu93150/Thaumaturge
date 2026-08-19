package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Resolution helpers that turn an aspect {@link ResourceKey} into a live {@link Holder}.
 *
 * <p>Addons may safely hold {@code ResourceKey<IAspect>} constants in static initializers because
 * keys do not touch the registry. A {@link Holder} is required before an aspect can be placed into
 * an {@link AspectList} or compared, and resolving one needs a registry view. These helpers provide
 * that view from either a {@link HolderLookup.Provider} or a {@link Level}.
 *
 * <p>Every method returns {@code null} when the registry is unavailable or the key is unregistered,
 * so callers holding keys for aspects that may not exist can resolve defensively.
 *
 * @since 1.0.0
 */
public final class Aspects {
    private Aspects() {}

    /**
     * Resolves an aspect key against a holder-lookup provider.
     *
     * @param registries the registry view for the current reload
     * @param key        the aspect key
     * @return the aspect holder, or {@code null} when the registry or key is unavailable
     */
    public static @Nullable Holder<IAspect> resolve(HolderLookup.Provider registries, ResourceKey<IAspect> key) {
        HolderLookup.RegistryLookup<IAspect> lookup = registries.lookup(IAspect.REGISTRY_KEY).orElse(null);
        if (lookup == null) {
            return null;
        }
        return lookup.get(key).orElse(null);
    }

    /**
     * Resolves an aspect key against a level's registry access.
     *
     * @param level the level; may be {@code null}
     * @param key   the aspect key
     * @return the aspect holder, or {@code null} when the level is null or the key is unavailable
     */
    public static @Nullable Holder<IAspect> resolve(@Nullable Level level, ResourceKey<IAspect> key) {
        if (level == null) {
            return null;
        }
        return resolve(level.registryAccess(), key);
    }

    /**
     * Resolves an aspect key against a concrete registry.
     *
     * @param registry the aspect registry
     * @param key      the aspect key
     * @return the aspect holder, or {@code null} when the key is unregistered
     */
    public static @Nullable Holder<IAspect> resolve(Registry<IAspect> registry, ResourceKey<IAspect> key) {
        return registry.get(key).orElse(null);
    }
}
