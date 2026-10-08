package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Collection;
import org.jspecify.annotations.Nullable;

public final class LabyrinthIndex {
    public static final LabyrinthIndex EMPTY = new LabyrinthIndex(Long2ObjectMaps.emptyMap());
    public static final int MARGIN_CHUNKS = MazeRegions.MARGIN_CHUNKS - 1;

    private final Long2ObjectMap<MazePlan> byRegion;

    private LabyrinthIndex(Long2ObjectMap<MazePlan> byRegion) {
        this.byRegion = byRegion;
    }

    public static LabyrinthIndex of(Collection<MazePlan> plans) {
        Long2ObjectOpenHashMap<MazePlan> map = new Long2ObjectOpenHashMap<>(plans.size());
        for (MazePlan plan : plans) {
            map.put(
                    MazeRegions.regionKey(
                            plan.geometry().origin().x, plan.geometry().origin().z),
                    plan);
        }
        return new LabyrinthIndex(Long2ObjectMaps.unmodifiable(map));
    }

    public @Nullable MazePlan planAt(int chunkX, int chunkZ, int margin) {
        MazePlan plan = byRegion.get(MazeRegions.regionKey(chunkX, chunkZ));
        return plan != null && plan.geometry().containsChunk(chunkX, chunkZ, margin) ? plan : null;
    }

    public @Nullable MazePlan planAtBlock(int blockX, int blockZ) {
        return planAt(blockX >> MazeGeometry.CELL_SHIFT, blockZ >> MazeGeometry.CELL_SHIFT, 0);
    }
}
