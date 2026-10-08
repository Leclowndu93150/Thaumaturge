package com.leclowndu93150.thaumaturge.api.labyrinth;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * What an encounter sees while it runs. Every call happens on the server thread.
 *
 * @since 1.0.0
 */
public interface EncounterContext {
    /**
     * @return the Outer Lands level
     */
    ServerLevel level();

    /**
     * @return the level's random source
     */
    RandomSource random();

    /**
     * @return the maze hosting the encounter
     */
    MazeId maze();

    /**
     * @return the centre of the boss hall floor, one block above the floor
     */
    BlockPos anchor();

    /**
     * Spawn positions recorded from the arena template, or the anchor alone when the template has none.
     *
     * @return the spawn positions, never empty
     */
    List<BlockPos> spawnPoints();

    /**
     * @return the boss hall interior
     */
    BoundingBox arena();

    /**
     * @return the number of players counted for scaling, at least 1
     */
    int participants();

    /**
     * Creates, scales and adds an entity bound to this encounter. The entity is persistent, homed to the hall, and marked so champion rolls and wild boss drops do not apply.
     *
     * @param type the entity type
     * @param pos  the spawn position
     * @param role the entity's part in the encounter
     * @return the spawned entity, or empty when the type could not be created or placed
     */
    Optional<Entity> spawn(EntityType<?> type, BlockPos pos, EncounterRole role);

    /**
     * Sends a title to every player inside the maze.
     *
     * @param message the title text
     */
    void announce(Component message);

    /**
     * @return ticks since {@link LabyrinthEncounter#begin} was called
     */
    long age();
}
