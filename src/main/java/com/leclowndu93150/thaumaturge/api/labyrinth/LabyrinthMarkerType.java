package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registration entry for a kind of {@link LabyrinthMarker}. Addons register one with a {@code DeferredRegister} on {@link #REGISTRY_KEY}; the id becomes the {@code type} field of the marker
 * JSON.
 *
 * @param codec the codec for the marker's own fields
 * @param <T>   the marker class
 * @since 1.0.0
 */
public record LabyrinthMarkerType<T extends LabyrinthMarker>(MapCodec<T> codec) {
    /**
     * Key of the built-in registry that holds marker types, {@code thaumaturge:labyrinth_marker_type}. Addons register their own types into it.
     */
    public static final ResourceKey<Registry<LabyrinthMarkerType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("labyrinth_marker_type"));
}
