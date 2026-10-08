package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounterType;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.SingleBossEncounter;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.SwarmEncounter;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTLabyrinthEncounterTypes {
    public static final DeferredRegister<LabyrinthEncounterType<?>> TYPES =
            DeferredRegister.create(LabyrinthEncounterType.REGISTRY_KEY, TTIds.MODID);
    private static final Registry<LabyrinthEncounterType<?>> REGISTRY =
            TYPES.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<LabyrinthEncounterType<?>, LabyrinthEncounterType<SingleBossEncounter>>
            SINGLE_BOSS = TYPES.register("single_boss", () -> new LabyrinthEncounterType<>(SingleBossEncounter.CODEC));
    public static final DeferredHolder<LabyrinthEncounterType<?>, LabyrinthEncounterType<SwarmEncounter>> SWARM =
            TYPES.register("swarm", () -> new LabyrinthEncounterType<>(SwarmEncounter.CODEC));

    private TTLabyrinthEncounterTypes() {}

    public static Registry<LabyrinthEncounterType<?>> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
