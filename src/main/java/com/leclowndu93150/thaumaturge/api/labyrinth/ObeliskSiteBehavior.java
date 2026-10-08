package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.mojang.serialization.Codec;

/**
 * What guards an overworld obelisk site before its portal is opened: a cult ritual, a guardian watch, or nothing.
 *
 * <p>The altar calls {@link #tick} on the server thread at a fixed interval until the site is quelled or its portal opens. Behaviors keep no state of their own; everything they need to remember
 * goes through {@link SiteContext}, which the altar saves.
 *
 * @since 1.0.0
 */
public interface ObeliskSiteBehavior {
    /**
     * Dispatches on the {@code type} field through the {@link ObeliskSiteBehaviorType} registry.
     */
    Codec<ObeliskSiteBehavior> CODEC = Codec.lazyInitialized(() -> LabyrinthHelper.siteBehaviorTypes()
            .byNameCodec()
            .dispatch("type", ObeliskSiteBehavior::type, ObeliskSiteBehaviorType::codec));

    /**
     * @return the registered type of this behavior
     */
    ObeliskSiteBehaviorType<?> type();

    /**
     * Advances the site's garrison.
     *
     * @param context the site context
     */
    void tick(SiteContext context);
}
