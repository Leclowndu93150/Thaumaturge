package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.encounter.EncounterSessions;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world.BandSettings;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.world.level.ChunkPos;

public final class LabyrinthRuntime {
    private static final int MAX_REPAIR_RETRIES = 200;

    private final BandSettings band;
    private final RoomTemplates templates = new RoomTemplates();
    private final Queue<Long> repairs = new ConcurrentLinkedQueue<>();
    private final Set<Long> queued = ConcurrentHashMap.newKeySet();
    private final Map<Long, Integer> retries = new ConcurrentHashMap<>();
    private final EncounterSessions encounters = new EncounterSessions();
    private volatile LabyrinthIndex index = LabyrinthIndex.EMPTY;

    public LabyrinthRuntime(BandSettings band) {
        this.band = band;
    }

    public BandSettings band() {
        return band;
    }

    public RoomTemplates templates() {
        return templates;
    }

    public EncounterSessions encounters() {
        return encounters;
    }

    public LabyrinthIndex index() {
        return index;
    }

    public void publish(Collection<MazePlan> plans) {
        index = LabyrinthIndex.of(plans);
    }

    public void clear() {
        index = LabyrinthIndex.EMPTY;
        repairs.clear();
        queued.clear();
        retries.clear();
        encounters.clear();
    }

    public void queueRepair(int chunkX, int chunkZ) {
        long key = ChunkPos.asLong(chunkX, chunkZ);
        if (queued.add(key)) {
            repairs.add(key);
        }
    }

    public void retryRepair(long key) {
        if (retries.merge(key, 1, Integer::sum) > MAX_REPAIR_RETRIES) {
            retries.remove(key);
            return;
        }
        if (queued.add(key)) {
            repairs.add(key);
        }
    }

    public void repairDone(long key) {
        retries.remove(key);
    }

    public Long nextRepair() {
        Long key = repairs.poll();
        if (key != null) {
            queued.remove(key);
        }
        return key;
    }
}
