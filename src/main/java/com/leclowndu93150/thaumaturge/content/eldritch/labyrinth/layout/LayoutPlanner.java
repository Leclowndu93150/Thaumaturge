package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import java.util.List;

public final class LayoutPlanner {
    public static final int MIN_SIZE = 9;
    public static final int MAX_SIZE = 41;

    private static final List<LayoutStage> FULL = List.of(
            new AnchorStage(),
            new SpineStage(),
            new BranchStage(),
            new LoopStage(),
            new FeatureStage(),
            new RoomAssignStage(false),
            new WayfindStage());
    private static final List<LayoutStage> MINIMAL =
            List.of(new AnchorStage(), new SpineStage(), new RoomAssignStage(true), new WayfindStage());

    private LayoutPlanner() {}

    public static LayoutResult plan(LayoutContext context) {
        int size = size(context);
        try {
            return run(FULL, size, context, false);
        } catch (RuntimeException e) {
            Thaumaturge.LOGGER.warn("Labyrinth layout fell back to the minimal plan", e);
            return run(MINIMAL, size, context, true);
        }
    }

    private static LayoutResult run(List<LayoutStage> stages, int size, LayoutContext context, boolean minimal) {
        LayoutWork work = new LayoutWork(size, size);
        for (LayoutStage stage : stages) {
            stage.apply(work, context);
        }
        return LayoutResult.of(work, minimal);
    }

    private static int size(LayoutContext context) {
        int rolled = Math.round(context.definition().size().sample(context.random())
                * context.tuning().sizeScale());
        int clamped = Math.clamp(rolled, MIN_SIZE, MAX_SIZE);
        return (clamped & 1) == 0 ? clamped - 1 : clamped;
    }
}
