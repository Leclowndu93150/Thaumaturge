package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;

public final class SpellPaths {
    private SpellPaths() {}

    public static SpellNode at(SpellNode root, List<Integer> path) {
        SpellNode node = root;
        for (int index : path) {
            if (index < 0 || index >= node.children().size()) {
                return node;
            }
            node = node.children().get(index);
        }
        return node;
    }

    public static boolean exists(SpellNode root, List<Integer> path) {
        SpellNode node = root;
        for (int index : path) {
            if (index < 0 || index >= node.children().size()) {
                return false;
            }
            node = node.children().get(index);
        }
        return true;
    }

    public static List<Integer> parent(List<Integer> path) {
        return path.isEmpty() ? path : List.copyOf(path.subList(0, path.size() - 1));
    }

    public static List<Integer> child(List<Integer> path, int index) {
        List<Integer> out = new ArrayList<>(path);
        out.add(index);
        return List.copyOf(out);
    }

    public static SpellNode update(SpellNode root, List<Integer> path, UnaryOperator<SpellNode> change) {
        if (path.isEmpty()) {
            return change.apply(root);
        }
        int index = path.getFirst();
        List<SpellNode> children = new ArrayList<>(root.children());
        children.set(index, update(children.get(index), path.subList(1, path.size()), change));
        return root.withChildren(children);
    }

    public static SpellNode append(SpellNode root, List<Integer> parent, SpellNode child) {
        return update(root, parent, node -> node.then(child));
    }

    public static SpellNode insertBelow(SpellNode root, List<Integer> path, SpellNode inserted) {
        return update(root, path, node -> node.withChildren(List.of(inserted.withChildren(node.children()))));
    }

    public static SpellNode remove(SpellNode root, List<Integer> path, ToIntFunction<SpellNode> maxChildren) {
        if (path.isEmpty()) {
            return root;
        }
        List<Integer> parentPath = parent(path);
        int index = path.getLast();
        SpellNode removed = at(root, path);
        return update(root, parentPath, parent -> {
            List<SpellNode> children = new ArrayList<>(parent.children());
            children.remove(index);
            if (children.size() + removed.children().size() <= maxChildren.applyAsInt(parent)) {
                children.addAll(index, removed.children());
            }
            return parent.withChildren(children);
        });
    }
}
