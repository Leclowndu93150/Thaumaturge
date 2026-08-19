package com.leclowndu93150.thaumaturge.content.research.note;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Holder;

public final class NoteRules {
    private NoteRules() {}

    public static boolean connects(Holder<IAspect> a, Holder<IAspect> b, Predicate<Holder<IAspect>> discovered) {
        if (a == null || b == null) {
            return false;
        }
        if (!discovered.test(a) || !discovered.test(b)) {
            return false;
        }
        return componentOf(a, b) || componentOf(b, a);
    }

    private static boolean componentOf(Holder<IAspect> compound, Holder<IAspect> component) {
        if (compound.value().isPrimal()) {
            return false;
        }
        for (Holder<IAspect> part : compound.value().components()) {
            if (part.is(component.getKey())) {
                return true;
            }
        }
        return false;
    }

    public record Completion(boolean complete, List<ResearchNoteData.Cell> prunedCells) {
    }

    public static Completion checkCompletion(ResearchNoteData data, Predicate<Holder<IAspect>> discovered) {
        Map<HexGrid.Hex, ResearchNoteData.Cell> map = data.cellMap();
        List<HexGrid.Hex> roots = new ArrayList<>();
        for (ResearchNoteData.Cell cell : data.cells()) {
            if (cell.type() == ResearchNoteData.TYPE_ROOT) {
                roots.add(cell.hex());
            }
        }
        if (roots.isEmpty()) {
            return new Completion(false, data.cells());
        }
        Set<HexGrid.Hex> visited = new HashSet<>();
        Deque<HexGrid.Hex> queue = new ArrayDeque<>();
        HexGrid.Hex start = roots.get(0);
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            HexGrid.Hex current = queue.poll();
            ResearchNoteData.Cell currentCell = map.get(current);
            for (int dir = 0; dir < 6; dir++) {
                HexGrid.Hex next = current.neighbour(dir);
                if (visited.contains(next)) {
                    continue;
                }
                ResearchNoteData.Cell nextCell = map.get(next);
                if (nextCell == null || !nextCell.active() || !currentCell.active()) {
                    continue;
                }
                if (connects(currentCell.aspectOrNull(), nextCell.aspectOrNull(), discovered)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        for (HexGrid.Hex root : roots) {
            if (!visited.contains(root)) {
                return new Completion(false, data.cells());
            }
        }
        List<ResearchNoteData.Cell> pruned = new ArrayList<>();
        for (ResearchNoteData.Cell cell : data.cells()) {
            if (cell.type() == ResearchNoteData.TYPE_ROOT || visited.contains(cell.hex())) {
                pruned.add(cell);
            }
        }
        return new Completion(true, pruned);
    }
}
