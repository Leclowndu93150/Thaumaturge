package com.leclowndu93150.thaumaturge.api.labyrinth;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

/**
 * What a {@link LabyrinthMarker} sees when a player sets it off. Calls happen on the server thread.
 *
 * @since 1.0.0
 */
public interface MarkerTriggerContext {
    /**
     * @return the Outer Lands level
     */
    ServerLevel level();

    /**
     * @return the level's random source
     */
    RandomSource random();

    /**
     * @return the maze that owns the marker
     */
    MazeId maze();

    /**
     * @return the marker's index among the maze's triggers, stable for the life of the maze
     */
    int index();

    /**
     * Turns a direction written in the template's own frame into the world frame of the placed room.
     *
     * @param local a direction in template space
     * @return the same direction after the room's mirror and rotation
     */
    Direction orient(Direction local);
}
