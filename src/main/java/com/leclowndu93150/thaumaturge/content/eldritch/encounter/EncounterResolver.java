package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.EncounterEntry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.LootTable;

public final class EncounterResolver {
    private EncounterResolver() {}

    static Optional<Holder.Reference<LabyrinthEncounter>> forActivation(ServerLevel level, MazeRecord record) {
        HolderLookup.RegistryLookup<LabyrinthEncounter> registry =
                level.registryAccess().lookupOrThrow(LabyrinthEncounter.REGISTRY_KEY);
        Optional<Holder.Reference<LabyrinthEncounter>> chosen =
                lookup(registry, record.state().encounter().override());
        if (chosen.isEmpty()) {
            chosen = lookup(registry, record.plan().encounter());
        }
        if (chosen.isEmpty()) {
            chosen = repick(level, record, registry);
        }
        return chosen;
    }

    public static Optional<ResourceKey<LootTable>> rewardTable(ServerLevel level, MazeRecord record) {
        return active(level, record).map(holder -> holder.value().settings().reward());
    }

    static Optional<Holder.Reference<LabyrinthEncounter>> active(ServerLevel level, MazeRecord record) {
        return lookup(
                level.registryAccess().lookupOrThrow(LabyrinthEncounter.REGISTRY_KEY),
                record.state().encounter().active());
    }

    private static Optional<Holder.Reference<LabyrinthEncounter>> lookup(
            HolderLookup.RegistryLookup<LabyrinthEncounter> registry, Optional<ResourceKey<LabyrinthEncounter>> key) {
        return key.flatMap(registry::get);
    }

    private static Optional<Holder.Reference<LabyrinthEncounter>> repick(
            ServerLevel level, MazeRecord record, HolderLookup.RegistryLookup<LabyrinthEncounter> registry) {
        Optional<LabyrinthDefinition> definition = LabyrinthService.definition(level.getServer(), record);
        if (definition.isEmpty()) {
            return Optional.empty();
        }
        SimpleWeightedRandomList.Builder<Holder<LabyrinthEncounter>> pool = SimpleWeightedRandomList.builder();
        for (EncounterEntry entry : definition.get().encounters()) {
            if (entry.encounter().isBound()) {
                pool.add(entry.encounter(), entry.weight());
            }
        }
        return pool.build()
                .getRandomValue(level.getRandom())
                .flatMap(Holder::unwrapKey)
                .flatMap(registry::get);
    }
}
