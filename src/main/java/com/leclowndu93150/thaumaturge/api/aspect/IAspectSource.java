package com.leclowndu93150.thaumaturge.api.aspect;

/**
 * An aspect container that devices such as the infusion matrix may drain essentia from or fill
 * with essentia.
 *
 * <p>Expose it through {@link AspectCapabilities#CONTAINER}. Source discovery queries that
 * capability with a {@code null} side, so a provider must return this view for a null side to be
 * found. Selection only looks at loaded positions and prefers higher-priority sources before
 * applying proximity and transport rules. It never loads chunks.
 *
 * <p>The infusion matrix searches a cube reaching 12 blocks from its centre in every direction.
 * Directional searches cover the same 25 by 25 cross-section and reach 12 layers forward, starting
 * at the searching block's own layer. After a search finds nothing usable, the searcher waits 200
 * server ticks before searching again. Each successful transfer moves one unit of one aspect
 * through the inherited {@link IAspectContainer} methods.
 *
 * <p>Implementations own their mutation, persistence and client sync. Capability instances follow
 * NeoForge block capability invalidation and must not be kept after they are invalidated.
 *
 * @since 1.0.0
 */
public interface IAspectSource extends IAspectContainer {
    /**
     * This source's preference in automated essentia selection. Higher values are tried first;
     * equal priorities retain the native selection order.
     *
     * <p>The default keeps existing third-party sources at neutral priority.
     *
     * @return source selection priority, defaulting to {@code 0}
     */
    default int getSourcePriority() {
        return 0;
    }

    /**
     * Whether source discovery should skip this container for now.
     *
     * @return true to block both draining and filling by automated devices
     */
    boolean isBlocked();
}
