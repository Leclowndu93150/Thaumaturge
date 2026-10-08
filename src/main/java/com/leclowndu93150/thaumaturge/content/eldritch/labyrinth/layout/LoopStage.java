package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.Direction;

final class LoopStage implements LayoutStage {
    private static final Direction[] FORWARD = {Direction.EAST, Direction.SOUTH};
    private static final int DIRECTION_BITS = 2;
    private static final int DIRECTION_MASK = (1 << DIRECTION_BITS) - 1;

    @Override
    public void apply(LayoutWork work, LayoutContext context) {
        int open = 0;
        IntArrayList candidates = new IntArrayList();
        for (int cell = 0; cell < work.cells(); cell++) {
            if (!work.open[cell] || work.reserved[cell]) {
                continue;
            }
            open++;
            for (Direction direction : FORWARD) {
                int next = work.neighbor(cell, direction);
                if (next != LayoutWork.NONE
                        && work.open[next]
                        && !work.reserved[next]
                        && !work.hasEdge(cell, direction)
                        && work.canConnect(cell, direction)) {
                    candidates.add(cell << DIRECTION_BITS | direction.get2DDataValue());
                }
            }
        }
        int target = Math.round(open
                * context.definition().layout().loopRatio()
                * context.tuning().loopScale());
        int minLength = context.definition().layout().minLoopLength();
        int[] order = candidates.toIntArray();
        WeightedPick.shuffle(order, context.random());
        int added = 0;
        for (int candidate : order) {
            if (added >= target) {
                break;
            }
            int cell = candidate >>> DIRECTION_BITS;
            Direction direction = Direction.from2DDataValue(candidate & DIRECTION_MASK);
            if (GridSearch.edgeDistance(work, cell, work.neighbor(cell, direction), minLength) >= minLength) {
                work.connect(cell, direction);
                added++;
            }
        }
    }
}
