package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthTuning;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public final class LabyrinthPlaceSettings extends StructurePlaceSettings {
    private final long seed;
    private final LabyrinthTuning tuning;

    public LabyrinthPlaceSettings(long seed, LabyrinthTuning tuning) {
        this.seed = seed;
        this.tuning = tuning;
    }

    public long seed() {
        return seed;
    }

    public LabyrinthTuning tuning() {
        return tuning;
    }
}
