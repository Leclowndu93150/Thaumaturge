package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registration entry for a kind of {@link LabyrinthEncounter}. The id becomes the {@code type} field of encounter JSON files.
 *
 * @param codec the codec for the encounter's fields
 * @param <E>   the encounter class
 * @since 1.0.0
 */
public record LabyrinthEncounterType<E extends LabyrinthEncounter>(MapCodec<E> codec) {
    /**
     * Key of the built-in registry that holds encounter types, {@code thaumaturge:labyrinth_encounter_type}. Addons register their own types into it.
     */
    public static final ResourceKey<Registry<LabyrinthEncounterType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("labyrinth_encounter_type"));
}
