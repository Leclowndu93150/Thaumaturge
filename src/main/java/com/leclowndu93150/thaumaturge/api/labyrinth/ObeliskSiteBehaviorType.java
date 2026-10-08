package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registration entry for a kind of {@link ObeliskSiteBehavior}. The id becomes the {@code type} field of a site's {@code behavior} object.
 *
 * @param codec the codec for the behavior's fields
 * @param <B>   the behavior class
 * @since 1.0.0
 */
public record ObeliskSiteBehaviorType<B extends ObeliskSiteBehavior>(MapCodec<B> codec) {
    /**
     * Key of the built-in registry that holds obelisk site behavior types, {@code thaumaturge:obelisk_site_behavior_type}. Addons register their own types into it.
     */
    public static final ResourceKey<Registry<ObeliskSiteBehaviorType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("obelisk_site_behavior_type"));
}
