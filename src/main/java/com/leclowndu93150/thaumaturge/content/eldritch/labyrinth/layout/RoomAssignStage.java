package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

final class RoomAssignStage implements LayoutStage {
    private static final double VARIETY_PENALTY = 0.2;

    private final boolean fallbackOnly;

    RoomAssignStage(boolean fallbackOnly) {
        this.fallbackOnly = fallbackOnly;
    }

    @Override
    public void apply(LayoutWork work, LayoutContext context) {
        RoomCatalog fallback = RoomCatalog.of(context.definition().rooms().fallback(), context.enabled());
        RoomCatalog passages = fallbackOnly
                ? fallback
                : RoomCatalog.of(context.definition().rooms().passages(), context.enabled());
        int[] distance = GridSearch.edgeDistances(work, work.portalCell);
        for (int cell : GridSearch.walk(work, work.portalCell, (from, direction, to) -> true)) {
            if (!work.reserved[cell] && work.open[cell]) {
                assign(work, context, passages, fallback, distance[cell], cell);
            }
        }
    }

    private static void assign(
            LayoutWork work,
            LayoutContext context,
            RoomCatalog passages,
            RoomCatalog fallback,
            int distance,
            int cell) {
        int mask = work.edges[cell];
        Optional<RoomCatalog.Option> chosen = WeightedPick.pick(
                passages.available(mask, work, distance),
                option -> weight(work, cell, option.room()),
                context.random());
        if (chosen.isEmpty()) {
            chosen = WeightedPick.byRoomWeight(fallback.options(mask), RoomCatalog.Option::room, context.random());
        }
        RoomCatalog.Option option =
                chosen.orElseThrow(() -> new IllegalStateException("No room covers opening mask " + mask));
        work.placeSingle(context, option.room(), option.randomTransform(context.random()), cell);
    }

    private static double weight(LayoutWork work, int cell, Holder<RoomType> room) {
        double weight = room.value().weight();
        Optional<ResourceLocation> group = room.value().varietyGroup();
        if (group.isEmpty()) {
            return weight;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int next = work.neighbor(cell, direction);
            if (next != LayoutWork.NONE
                    && work.room[next] != LayoutWork.NONE
                    && work.placements
                            .get(work.room[next])
                            .room()
                            .value()
                            .varietyGroup()
                            .equals(group)) {
                weight *= VARIETY_PENALTY;
            }
        }
        return weight;
    }
}
