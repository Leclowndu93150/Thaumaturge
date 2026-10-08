package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerTriggerContext;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

record TriggerContext(ServerLevel level, MazeId maze, int index, int transform) implements MarkerTriggerContext {
    @Override
    public RandomSource random() {
        return level.getRandom();
    }

    @Override
    public Direction orient(Direction local) {
        return MazeCells.orient(transform, local);
    }
}
