package com.leclowndu93150.thaumaturge.content.research.note;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class NoteGenerator {
    private static final int MAX_RADIUS_BONUS = 3;
    private static final int HOLES_PER_COMPLEXITY = 2;
    private static final int MIN_ROOT_NEIGHBOURS = 2;
    private static final int HOLE_ATTEMPT_LIMIT = 100;

    private NoteGenerator() {}

    public static ResearchNoteData generate(Identifier entry, int index, AspectList anchors, int complexity, RandomSource random) {
        int clamped = Mth.clamp(complexity, 1, 3);
        int radius = 1 + Math.min(MAX_RADIUS_BONUS, clamped);
        Map<HexGrid.Hex, ResearchNoteData.Cell> cells = new LinkedHashMap<>();
        for (HexGrid.Hex hex : HexGrid.disk(radius)) {
            cells.put(hex, new ResearchNoteData.Cell(hex, ResearchNoteData.TYPE_BLANK, Optional.empty()));
        }
        List<AspectInstance> tags = anchors.entries();
        List<HexGrid.Hex> ring = HexGrid.distributeRing(radius, tags.size(), random);
        for (int i = 0; i < tags.size() && i < ring.size(); i++) {
            HexGrid.Hex hex = ring.get(i);
            cells.put(hex, new ResearchNoteData.Cell(hex, ResearchNoteData.TYPE_ROOT, Optional.of(tags.get(i).aspect())));
        }
        if (clamped > 1) {
            punchHoles(cells, clamped * HOLES_PER_COMPLEXITY, random);
        }
        return new ResearchNoteData(entry, index, primaryColor(anchors), false, 0, List.copyOf(cells.values()));
    }

    private static void punchHoles(Map<HexGrid.Hex, ResearchNoteData.Cell> cells, int holes, RandomSource random) {
        int attempts = 0;
        while (holes > 0 && attempts++ < HOLE_ATTEMPT_LIMIT) {
            List<HexGrid.Hex> blanks = new ArrayList<>();
            for (ResearchNoteData.Cell cell : cells.values()) {
                if (cell.type() == ResearchNoteData.TYPE_BLANK) {
                    blanks.add(cell.hex());
                }
            }
            if (blanks.isEmpty()) {
                return;
            }
            HexGrid.Hex candidate = blanks.get(random.nextInt(blanks.size()));
            if (safeToRemove(cells, candidate)) {
                cells.remove(candidate);
                holes--;
            }
        }
    }

    private static boolean safeToRemove(Map<HexGrid.Hex, ResearchNoteData.Cell> cells, HexGrid.Hex candidate) {
        for (int dir = 0; dir < 6; dir++) {
            HexGrid.Hex neighbour = candidate.neighbour(dir);
            ResearchNoteData.Cell cell = cells.get(neighbour);
            if (cell == null || cell.type() != ResearchNoteData.TYPE_ROOT) {
                continue;
            }
            int remaining = 0;
            for (int d2 = 0; d2 < 6; d2++) {
                HexGrid.Hex around = neighbour.neighbour(d2);
                if (!around.equals(candidate) && cells.containsKey(around)) {
                    remaining++;
                }
            }
            if (remaining < MIN_ROOT_NEIGHBOURS) {
                return false;
            }
        }
        return true;
    }

    public static int primaryColor(AspectList anchors) {
        Holder<IAspect> primary = null;
        int best = -1;
        for (AspectInstance entry : anchors.entries()) {
            if (entry.amount() > best) {
                best = entry.amount();
                primary = entry.aspect();
            }
        }
        return primary == null ? 0x999999 : primary.value().color();
    }
}
