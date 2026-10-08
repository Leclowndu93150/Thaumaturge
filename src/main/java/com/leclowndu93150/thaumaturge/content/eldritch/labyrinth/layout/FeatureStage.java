package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.FeatureQuota;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

final class FeatureStage implements LayoutStage {
    @Override
    public void apply(LayoutWork work, LayoutContext context) {
        for (FeatureQuota quota : context.definition().rooms().features()) {
            RoomCatalog catalog = RoomCatalog.of(quota.rooms(), context.enabled());
            int count = quota.count().sample(context.random());
            for (int i = 0; i < count; i++) {
                if (!placeOne(work, context, quota, catalog)) {
                    break;
                }
            }
        }
    }

    private static boolean placeOne(LayoutWork work, LayoutContext context, FeatureQuota quota, RoomCatalog catalog) {
        RandomSource random = context.random();
        int[] distance = GridSearch.edgeDistances(work, work.portalCell);
        IntList candidates = new IntArrayList();
        for (int cell = 0; cell < work.cells(); cell++) {
            if (eligible(work, quota, catalog, distance, cell)) {
                candidates.add(cell);
            }
        }
        if (candidates.isEmpty() && quota.placement() == FeatureQuota.Placement.DEAD_END) {
            int spur = sprout(work, quota, distance, random);
            if (spur != LayoutWork.NONE && eligible(work, quota, catalog, distance, spur)) {
                candidates.add(spur);
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }
        int cell = WeightedPick.any(candidates, random);
        Optional<RoomCatalog.Option> option = WeightedPick.byRoomWeight(
                catalog.available(work.edges[cell], work, distance[cell]), RoomCatalog.Option::room, random);
        option.ifPresent(chosen -> work.placeSingle(context, chosen.room(), chosen.randomTransform(random), cell));
        return option.isPresent();
    }

    private static boolean eligible(
            LayoutWork work, FeatureQuota quota, RoomCatalog catalog, int[] distance, int cell) {
        if (!work.open[cell]
                || work.reserved[cell]
                || distance[cell] == Integer.MAX_VALUE
                || distance[cell] < quota.minPortalDistance()) {
            return false;
        }
        if (quota.placement() == FeatureQuota.Placement.DEAD_END && work.degree(cell) != 1) {
            return false;
        }
        return !catalog.available(work.edges[cell], work, distance[cell]).isEmpty();
    }

    private static int sprout(LayoutWork work, FeatureQuota quota, int[] distance, RandomSource random) {
        IntList parents = new IntArrayList();
        for (int cell = 0; cell < work.cells(); cell++) {
            if (work.open[cell]
                    && !work.reserved[cell]
                    && distance[cell] != Integer.MAX_VALUE
                    && distance[cell] + 1 >= quota.minPortalDistance()
                    && hasRoom(work, cell)) {
                parents.add(cell);
            }
        }
        if (parents.isEmpty()) {
            return LayoutWork.NONE;
        }
        int parent = WeightedPick.any(parents, random);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (work.canGrow(parent, direction)) {
                int next = work.neighbor(parent, direction);
                work.connect(parent, direction);
                distance[next] = distance[parent] + 1;
                return next;
            }
        }
        return LayoutWork.NONE;
    }

    private static boolean hasRoom(LayoutWork work, int cell) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (work.canGrow(cell, direction)) {
                return true;
            }
        }
        return false;
    }
}
