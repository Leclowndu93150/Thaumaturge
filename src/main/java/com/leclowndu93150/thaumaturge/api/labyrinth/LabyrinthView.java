package com.leclowndu93150.thaumaturge.api.labyrinth;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Read-only snapshot of one labyrinth, valid on the server thread for the tick it was obtained in.
 *
 * @since 1.0.0
 */
public interface LabyrinthView {
    /**
     * @return the maze id
     */
    MazeId id();

    /**
     * @return the current phase
     */
    LabyrinthPhase phase();

    /**
     * @return the altar that opened the maze; returns lead back here
     */
    GlobalPos origin();

    /**
     * @return the full extent of the maze in the Outer Lands
     */
    BoundingBox bounds();

    /**
     * @return the boss hall interior
     */
    BoundingBox bossHall();

    /**
     * @param id a landmark id, see {@link LabyrinthLandmarks}
     * @return the landmark position, or empty when the maze has none under that id
     */
    Optional<BlockPos> landmark(ResourceLocation id);

    /**
     * @return the encounter rolled for this maze, or empty when its pool was empty
     */
    Optional<ResourceKey<LabyrinthEncounter>> encounter();

    /**
     * The first step along the maze's corridors from a position toward a landmark. Supported targets are {@link LabyrinthLandmarks#ARRIVAL}, {@link LabyrinthLandmarks#KEY} and
     * {@link LabyrinthLandmarks#BOSS_DOOR}.
     *
     * @param landmark the target landmark id
     * @param from     a position inside the maze
     * @return the horizontal direction of the next corridor step, or empty when {@code from} is outside the maze, already in the target cell, or the target is unsupported
     */
    Optional<Direction> directionToward(ResourceLocation landmark, BlockPos from);

    /**
     * @param landmark the target landmark id, as for {@link #directionToward}
     * @param from     a position inside the maze
     * @return the number of cells along the corridors to the target, or empty under the same conditions as {@link #directionToward}
     */
    Optional<Integer> cellsToward(ResourceLocation landmark, BlockPos from);
}
