package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.core.Direction;

final class SpineStage implements LayoutStage {
    @Override
    public void apply(LayoutWork work, LayoutContext context) {
        CostField cost = CostField.create(
                work.width, work.depth, context.definition().layout().costNoise(), context.random());
        IntList toKey = GridSearch.cheapestPath(
                work, IntList.of(work.portalCell), work.keyCell, cost, cell -> !work.reserved[cell]);
        if (toKey.size() < 2) {
            throw new IllegalStateException("No corridor reaches the key cell");
        }
        carve(work, toKey);
        placeKeyRoom(work, context, GridSearch.between(work, work.keyCell, toKey.getInt(toKey.size() - 2)));
        IntList component = GridSearch.component(work);
        if (!component.contains(work.doorCell)) {
            IntList toDoor =
                    GridSearch.cheapestPath(work, component, work.doorCell, cost, cell -> !work.reserved[cell]);
            if (toDoor.size() < 2) {
                throw new IllegalStateException("No corridor reaches the boss door");
            }
            carve(work, toDoor);
        }
    }

    private static void carve(LayoutWork work, IntList path) {
        for (int i = 1; i < path.size(); i++) {
            work.connect(path.getInt(i - 1), GridSearch.between(work, path.getInt(i - 1), path.getInt(i)));
        }
    }

    private static void placeKeyRoom(LayoutWork work, LayoutContext context, Direction entry) {
        RoomCatalog catalog = RoomCatalog.of(context.definition().rooms().key(), context.enabled());
        List<RoomCatalog.Option> options = catalog.options(MazeCells.bit(entry));
        RoomCatalog.Option option = WeightedPick.byRoomWeight(options, RoomCatalog.Option::room, context.random())
                .orElseThrow(() -> new IllegalStateException("No key room has a single socket"));
        work.placeSingle(context, option.room(), option.randomTransform(context.random()), work.keyCell);
        work.sealExcept(work.keyCell, work.edges[work.keyCell]);
    }
}
