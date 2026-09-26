package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class ResearchProgression {

    private ResearchProgression() {}

    public static void reset(ServerPlayer player) {
        KnowledgeAccess.of(player).clear();
        ResearchManager.applyAutoUnlock(player);
    }

    public static int grant(ServerPlayer player, Identifier research) {
        Set<Identifier> ordered = new LinkedHashSet<>();
        collect(entries(player), research, new HashSet<>(), ordered);
        return completeAll(player, ordered);
    }

    public static int prepare(ServerPlayer player, Identifier research) {
        Set<Identifier> ordered = new LinkedHashSet<>();
        collect(entries(player), research, new HashSet<>(), ordered);
        ordered.remove(research);
        int completed = completeAll(player, ordered);
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        if (knowledge.removeResearch(research)) {
            knowledge.sync(player);
        }
        ResearchManager.unlock(player, research);
        return completed;
    }

    public static int revoke(ServerPlayer player, Identifier research) {
        Map<Identifier, List<Identifier>> dependents = new HashMap<>();
        entries(player).listElements().forEach(entry -> {
            for (Identifier requirement : requirements(entry.value())) {
                dependents.computeIfAbsent(requirement, key -> new ArrayList<>()).add(entry.key().identifier());
            }
        });
        Set<Identifier> affected = new LinkedHashSet<>();
        Deque<Identifier> queue = new ArrayDeque<>();
        queue.add(research);
        while (!queue.isEmpty()) {
            Identifier next = queue.poll();
            if (affected.add(next)) {
                queue.addAll(dependents.getOrDefault(next, List.of()));
            }
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        int removed = 0;
        for (Identifier id : affected) {
            if (knowledge.removeResearch(id) && !id.equals(research)) {
                removed++;
            }
        }
        knowledge.sync(player);
        return removed;
    }

    public static int completeCategory(ServerPlayer player, ResourceKey<IResearchCategory> category) {
        HolderLookup.RegistryLookup<IResearchEntry> entries = entries(player);
        Set<Identifier> visited = new HashSet<>();
        Set<Identifier> ordered = new LinkedHashSet<>();
        entries.listElements().filter(entry -> entry.value().category().is(category)).forEach(entry -> collect(entries, entry.key().identifier(), visited, ordered));
        return completeAll(player, ordered);
    }

    public static int setStage(ServerPlayer player, Holder<IResearchCategory> stage) {
        reset(player);
        HolderLookup.RegistryLookup<IResearchEntry> entries = entries(player);
        Set<Identifier> visited = new HashSet<>();
        Set<Identifier> ordered = new LinkedHashSet<>();
        int index = stage.value().index();
        entries.listElements().filter(entry -> entry.value().category().value().index() < index).forEach(entry -> collect(entries, entry.key().identifier(), visited, ordered));
        stage.value().requiredResearch().ifPresent(gate -> collect(entries, gate, visited, ordered));
        return completeAll(player, ordered);
    }

    private static HolderLookup.RegistryLookup<IResearchEntry> entries(ServerPlayer player) {
        return player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY);
    }

    private static void collect(HolderLookup.RegistryLookup<IResearchEntry> entries, Identifier research, Set<Identifier> visited, Set<Identifier> ordered) {
        if (!visited.add(research)) {
            return;
        }
        entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)).ifPresent(entry -> {
            for (Identifier requirement : requirements(entry.value())) {
                collect(entries, requirement, visited, ordered);
            }
        });
        ordered.add(research);
    }

    private static List<Identifier> requirements(IResearchEntry entry) {
        List<Identifier> requirements = new ArrayList<>();
        entry.category().value().requiredResearch().ifPresent(requirements::add);
        for (ResearchParent parent : entry.parents()) {
            requirements.add(parent.id());
        }
        for (IResearchStage stage : entry.stages()) {
            requirements.addAll(stage.requiredResearch());
        }
        return requirements;
    }

    private static int completeAll(ServerPlayer player, Collection<Identifier> research) {
        int completed = 0;
        for (Identifier id : research) {
            if (ResearchManager.complete(player, id)) {
                completed++;
            }
        }
        return completed;
    }
}
