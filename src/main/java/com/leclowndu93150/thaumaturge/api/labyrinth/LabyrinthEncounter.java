package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;

/**
 * A boss encounter fought in a labyrinth's boss hall.
 *
 * <p>Encounters are datapack entries in {@link #REGISTRY_KEY} ({@code data/<namespace>/thaumaturge/labyrinth_encounter/<path>.json}). Each labyrinth definition lists a weighted pool of them,
 * and one is rolled when a maze is created.
 *
 * <p>When the lock opens the mod calls {@link #begin} once, then {@link #tick} every 20 ticks while the encounter is active. Every entity the encounter spawns through
 * {@link EncounterContext#spawn} with {@link EncounterRole#PRIMARY} must die for the encounter to count as beaten.
 *
 * @since 1.0.0
 */
public interface LabyrinthEncounter {
    /**
     * Key of the datapack registry that holds encounter entries, {@code thaumaturge:labyrinth_encounter}.
     */
    ResourceKey<Registry<LabyrinthEncounter>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("labyrinth_encounter"));

    /**
     * Inline encounter codec, dispatching on {@code type} through the {@link LabyrinthEncounterType} registry.
     */
    Codec<LabyrinthEncounter> DIRECT_CODEC = Codec.lazyInitialized(() -> LabyrinthHelper.encounterTypes()
            .byNameCodec()
            .dispatch("type", LabyrinthEncounter::type, LabyrinthEncounterType::codec));

    /**
     * Reference codec: accepts an encounter id or an inline encounter.
     */
    Codec<Holder<LabyrinthEncounter>> CODEC = RegistryFileCodec.create(REGISTRY_KEY, DIRECT_CODEC);

    /**
     * @return the registered type of this encounter
     */
    LabyrinthEncounterType<?> type();

    /**
     * @return the shared settings: names, arena, reward and scaling
     */
    EncounterSettings settings();

    /**
     * Spawns the encounter. Called once on the server thread when the boss door opens.
     *
     * @param context the encounter context
     */
    void begin(EncounterContext context);

    /**
     * Runs every 20 ticks while the encounter is active.
     *
     * @param context the encounter context
     */
    default void tick(EncounterContext context) {}
}
