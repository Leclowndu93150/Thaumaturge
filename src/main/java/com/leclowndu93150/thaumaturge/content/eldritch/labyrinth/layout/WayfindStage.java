package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;

final class WayfindStage implements LayoutStage {
    @Override
    public void apply(LayoutWork work, LayoutContext context) {
        trace(work, work.portalCell, work.directions[MazeCells.TARGET_ARRIVAL]);
        trace(work, work.keyCell, work.directions[MazeCells.TARGET_KEY]);
        trace(work, work.hallCell, work.directions[MazeCells.TARGET_DOOR]);
    }

    private static void trace(LayoutWork work, int target, int[] directions) {
        if (target == LayoutWork.NONE) {
            return;
        }
        GridSearch.walk(work, target, (from, direction, to) -> {
            directions[to] = direction.getOpposite().get2DDataValue();
            return true;
        });
    }
}
