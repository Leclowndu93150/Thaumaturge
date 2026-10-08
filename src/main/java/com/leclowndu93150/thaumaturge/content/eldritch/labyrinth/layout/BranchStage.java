package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

final class BranchStage implements LayoutStage {
    private static final int ITERATIONS_PER_CELL = 4;

    @Override
    public void apply(LayoutWork work, LayoutContext context) {
        RandomSource random = context.random();
        IntList grown = GridSearch.component(work);
        int target = Math.round(context.definition().layout().coverage() * (work.cells() - work.reservedCount()));
        int budget = work.cells() * ITERATIONS_PER_CELL;
        float straightness = context.definition().layout().straightness();
        List<Direction> options = new ArrayList<>(Direction.Plane.HORIZONTAL.length());
        for (int iteration = 0; iteration < budget && grown.size() < target; iteration++) {
            int current = WeightedPick.any(grown, random);
            Direction heading = null;
            int length = context.definition().layout().branchLength().sample(random);
            for (int step = 0; step < length; step++) {
                options.clear();
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (growable(work, current, direction)) {
                        options.add(direction);
                    }
                }
                if (options.isEmpty()) {
                    break;
                }
                Direction next = heading != null && options.contains(heading) && random.nextFloat() < straightness
                        ? heading
                        : options.get(random.nextInt(options.size()));
                work.connect(current, next);
                current = work.neighbor(current, next);
                grown.add(current);
                heading = next;
            }
        }
    }

    private static boolean growable(LayoutWork work, int cell, Direction direction) {
        return !work.reserved[cell] && work.canGrow(cell, direction);
    }
}
