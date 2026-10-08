package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public final class SpellTreeLayout {
    private SpellTreeLayout() {}

    public static List<Placed> layout(SpellNode root, Predicate<List<Integer>> ghostBelow) {
        List<Placed> out = new ArrayList<>();
        place(root, List.of(), 0, 0, null, ghostBelow, out);
        return out;
    }

    private static int width(SpellNode node, List<Integer> path, Predicate<List<Integer>> ghostBelow) {
        int count = node.children().size() + (ghostBelow.test(path) ? 1 : 0);
        if (count == 0) {
            return 1;
        }
        int total = 0;
        for (int index = 0; index < node.children().size(); index++) {
            total += width(node.children().get(index), SpellPaths.child(path, index), ghostBelow);
        }
        if (ghostBelow.test(path)) {
            total += 1;
        }
        if (count > 1 && count % 2 == 0) {
            total += 1;
        }
        return Math.max(1, total);
    }

    private static void place(
            SpellNode node,
            List<Integer> path,
            int column,
            int row,
            @Nullable Placed parent,
            Predicate<List<Integer>> ghostBelow,
            List<Placed> out) {
        Placed placed = new Placed(
                path,
                node,
                column,
                row,
                parent == null ? column : parent.column(),
                parent != null && siblings(parent, ghostBelow) > 1);
        out.add(placed);
        boolean ghost = ghostBelow.test(path);
        int count = node.children().size() + (ghost ? 1 : 0);
        if (count == 0) {
            return;
        }
        int total = width(node, path, ghostBelow);
        int cursor = column - (total - 1) / 2;
        for (int index = 0; index < count; index++) {
            if (count > 1 && count % 2 == 0 && index == count / 2) {
                cursor++;
            }
            boolean isGhost = index == node.children().size();
            List<Integer> childPath = SpellPaths.child(path, index);
            int childWidth = isGhost ? 1 : width(node.children().get(index), childPath, ghostBelow);
            int childColumn = cursor + (childWidth - 1) / 2;
            if (isGhost) {
                out.add(new Placed(childPath, null, childColumn, row + 1, column, count > 1));
            } else {
                place(node.children().get(index), childPath, childColumn, row + 1, placed, ghostBelow, out);
            }
            cursor += childWidth;
        }
    }

    private static int siblings(Placed parent, Predicate<List<Integer>> ghostBelow) {
        return parent.node() == null ? 0 : parent.node().children().size() + (ghostBelow.test(parent.path()) ? 1 : 0);
    }

    public record Placed(
            List<Integer> path, @Nullable SpellNode node, int column, int row, int parentColumn, boolean branched) {
        public boolean ghost() {
            return node == null;
        }
    }
}
