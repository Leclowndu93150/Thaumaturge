package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;

final class RoomCatalog {
    private static final int MASKS = 16;

    private final List<List<Option>> byMask = new ArrayList<>(MASKS);

    private RoomCatalog() {
        for (int mask = 0; mask < MASKS; mask++) {
            byMask.add(new ArrayList<>());
        }
    }

    static List<Holder<RoomType>> sorted(HolderSet<RoomType> set, Predicate<Holder<RoomType>> enabled) {
        List<Holder<RoomType>> rooms = new ArrayList<>();
        for (Holder<RoomType> room : set) {
            if (room.isBound() && enabled.test(room)) {
                rooms.add(room);
            }
        }
        rooms.sort(Comparator.comparing(RoomType::id));
        return rooms;
    }

    static RoomCatalog of(HolderSet<RoomType> set, Predicate<Holder<RoomType>> enabled) {
        RoomCatalog catalog = new RoomCatalog();
        for (Holder<RoomType> room : sorted(set, enabled)) {
            RoomType type = room.value();
            if (type.width() != 1 || type.depth() != 1) {
                continue;
            }
            IntList[] transformsByMask = new IntList[MASKS];
            for (int transform : Dihedral.allowedTransforms(type.transforms())) {
                int mask = Dihedral.mask(type, transform);
                if (transformsByMask[mask] == null) {
                    transformsByMask[mask] = new IntArrayList();
                }
                transformsByMask[mask].add(transform);
            }
            for (int mask = 0; mask < MASKS; mask++) {
                if (transformsByMask[mask] != null) {
                    catalog.byMask.get(mask).add(new Option(room, transformsByMask[mask]));
                }
            }
        }
        return catalog;
    }

    List<Option> options(int mask) {
        return byMask.get(mask);
    }

    List<Option> available(int mask, LayoutWork work, int portalDistance) {
        return byMask.get(mask).stream()
                .filter(option ->
                        work.count(option.room()) < option.room().value().maxPerMaze()
                                && portalDistance >= option.room().value().minPortalDistance())
                .toList();
    }

    record Option(Holder<RoomType> room, IntList transforms) {
        int randomTransform(RandomSource random) {
            return WeightedPick.any(transforms, random);
        }
    }
}
