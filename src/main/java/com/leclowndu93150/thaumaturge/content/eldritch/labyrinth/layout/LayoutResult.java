package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;

public record LayoutResult(
        int width,
        int depth,
        Optional<Holder<LabyrinthEncounter>> encounter,
        int[] edges,
        int[] roomOfCell,
        List<RoomPlacement> rooms,
        int[][] directions,
        int portalCell,
        int keyCell,
        int hallCell,
        boolean minimal) {
    public int cellX(int cell) {
        return cell % width;
    }

    public int cellZ(int cell) {
        return cell / width;
    }

    static LayoutResult of(LayoutWork work, boolean minimal) {
        return new LayoutResult(
                work.width,
                work.depth,
                work.encounter,
                work.edges,
                work.room,
                List.copyOf(work.placements),
                work.directions,
                work.portalCell,
                work.keyCell,
                work.hallCell,
                minimal);
    }
}
