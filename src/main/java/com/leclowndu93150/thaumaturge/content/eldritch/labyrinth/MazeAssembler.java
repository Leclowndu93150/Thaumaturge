package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.LayoutResult;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.RoomPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.CompiledRoom;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

final class MazeAssembler {
    private static final int HALL_HEADROOM = 14;
    private static final int MAX_TRIGGERS = 256;

    private MazeAssembler() {}

    static Assembly assemble(
            MazeId id,
            GlobalPos origin,
            MazeGeometry geometry,
            long seed,
            ResourceKey<LabyrinthDefinition> definition,
            LabyrinthTuning tuning,
            LayoutResult layout,
            Function<ResourceLocation, Optional<CompiledRoom>> rooms) {
        List<ResourceKey<RoomType>> roomPalette = new ArrayList<>();
        List<ResourceLocation> templatePalette = new ArrayList<>();
        Map<ResourceKey<RoomType>, Integer> roomIndex = new HashMap<>();
        Map<ResourceLocation, Integer> templateIndex = new HashMap<>();
        long[] packedRooms = new long[layout.rooms().size()];
        Map<ResourceLocation, BlockPos> points = new LinkedHashMap<>();
        List<BlockPos> barriers = new ArrayList<>();
        List<BlockPos> glyphs = new ArrayList<>();
        List<PendingTrigger> triggers = new ArrayList<>();
        for (int r = 0; r < layout.rooms().size(); r++) {
            RoomPlacement placement = layout.rooms().get(r);
            ResourceKey<RoomType> key = placement.room().unwrapKey().orElseThrow();
            int paletteIndex = roomIndex.computeIfAbsent(key, ignored -> {
                roomPalette.add(key);
                return roomPalette.size() - 1;
            });
            int template = templateIndex.computeIfAbsent(placement.template(), ignored -> {
                templatePalette.add(placement.template());
                return templatePalette.size() - 1;
            });
            packedRooms[r] = MazeCells.packRoom(
                    paletteIndex, template, placement.transform(), placement.anchorX(), placement.anchorZ());
            Optional<CompiledRoom> compiled = rooms.apply(placement.template());
            if (compiled.isEmpty()) {
                continue;
            }
            BlockPos roomOrigin = RoomTransform.origin(
                    geometry.cellMin(placement.anchorX(), placement.anchorZ()),
                    placement.transform(),
                    compiled.get().size());
            for (CompiledRoom.Marker marker : compiled.get().markers()) {
                BlockPos world = RoomTransform.toWorld(marker.local(), placement.transform(), roomOrigin);
                record(marker.marker(), world, placement.transform(), tuning, points, glyphs, triggers);
            }
            for (BlockPos barrier : compiled.get().barriers()) {
                barriers.add(RoomTransform.toWorld(barrier, placement.transform(), roomOrigin));
            }
        }
        points.putIfAbsent(
                LabyrinthLandmarks.ARRIVAL,
                geometry.cellCenter(layout.cellX(layout.portalCell()), layout.cellZ(layout.portalCell())));
        int[] cells = new int[layout.edges().length];
        int[] directions = new int[MazeCells.TARGETS];
        for (int cell = 0; cell < cells.length; cell++) {
            for (int target = 0; target < MazeCells.TARGETS; target++) {
                directions[target] = layout.directions()[target][cell];
            }
            cells[cell] = MazeCells.packCell(layout.edges()[cell], layout.roomOfCell()[cell], directions);
        }
        BoundingBox hall = hallBounds(geometry, layout);
        MazeLandmarks landmarks =
                new MazeLandmarks(Map.copyOf(points), List.copyOf(barriers), List.copyOf(glyphs), hall);
        MazePlan plan = new MazePlan(
                id,
                origin,
                geometry,
                seed,
                definition,
                layout.encounter().flatMap(holder -> holder.unwrapKey()),
                tuning,
                List.copyOf(roomPalette),
                List.copyOf(templatePalette),
                packedRooms,
                cells,
                landmarks);
        return new Assembly(plan, List.copyOf(triggers));
    }

    private static void record(
            LabyrinthMarker marker,
            BlockPos world,
            int transform,
            LabyrinthTuning tuning,
            Map<ResourceLocation, BlockPos> points,
            List<BlockPos> glyphs,
            List<PendingTrigger> triggers) {
        if (tuning.disables(marker)) {
            return;
        }
        marker.landmark().ifPresent(landmark -> points.putIfAbsent(landmark, world));
        if (marker.wayfinding()) {
            glyphs.add(world);
        }
        if (marker.phase() == MarkerPhase.TRIGGER && triggers.size() < MAX_TRIGGERS) {
            triggers.add(new PendingTrigger(triggers.size(), world, marker.triggerRadius(), transform, marker));
        }
    }

    private static BoundingBox hallBounds(MazeGeometry geometry, LayoutResult layout) {
        int hallRoom = layout.hallCell() < 0 ? -1 : layout.roomOfCell()[layout.hallCell()];
        if (hallRoom < 0) {
            BlockPos center = geometry.cellCenter(0, 0);
            return new BoundingBox(center);
        }
        RoomPlacement placement = layout.rooms().get(hallRoom);
        BlockPos min = geometry.cellMin(placement.anchorX(), placement.anchorZ());
        BlockPos max =
                geometry.cellMin(placement.anchorX() + placement.width(), placement.anchorZ() + placement.depth());
        return new BoundingBox(
                min.getX(),
                geometry.floorY() + 1,
                min.getZ(),
                max.getX() - 1,
                geometry.floorY() + HALL_HEADROOM,
                max.getZ() - 1);
    }

    record Assembly(MazePlan plan, List<PendingTrigger> triggers) {}
}
