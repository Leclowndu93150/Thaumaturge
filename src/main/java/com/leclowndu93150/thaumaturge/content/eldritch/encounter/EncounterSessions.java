package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;

public final class EncounterSessions {
    private final Int2ObjectMap<ArenaManifestJob> jobs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<ServerBossEvent> bars = new Int2ObjectOpenHashMap<>();

    Optional<ArenaManifestJob> job(int maze) {
        return Optional.ofNullable(jobs.get(maze));
    }

    boolean hasJob(int maze) {
        return jobs.containsKey(maze);
    }

    void startJob(int maze, ArenaManifestJob job) {
        jobs.put(maze, job);
    }

    void endJob(int maze) {
        jobs.remove(maze);
    }

    void tickJobs(ServerLevel level, int budget) {
        for (ArenaManifestJob job : jobs.values()) {
            if (!job.done()) {
                job.step(level, budget);
            }
        }
    }

    ServerBossEvent bar(int maze, Supplier<ServerBossEvent> factory) {
        return bars.computeIfAbsent(maze, key -> factory.get());
    }

    void endBar(int maze) {
        ServerBossEvent bar = bars.remove(maze);
        if (bar != null) {
            bar.removeAllPlayers();
        }
    }

    public void forget(int maze) {
        endJob(maze);
        endBar(maze);
    }

    public void clear() {
        jobs.clear();
        for (ServerBossEvent bar : bars.values()) {
            bar.removeAllPlayers();
        }
        bars.clear();
    }
}
