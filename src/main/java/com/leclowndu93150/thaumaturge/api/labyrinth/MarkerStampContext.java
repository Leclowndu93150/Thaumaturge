package com.leclowndu93150.thaumaturge.api.labyrinth;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What a {@link LabyrinthMarker} sees while its chunk generates.
 *
 * @apiNote Every call happens on a worldgen thread. Implementations must stay inside the chunk being generated and treat the context as valid only for the duration of the call.
 * @since 1.0.0
 */
public interface MarkerStampContext {
    /**
     * @return the generating level, limited to the current chunk
     */
    ServerLevelAccessor level();

    /**
     * @return a random source seeded from the maze and the marker position, so the result does not depend on chunk generation order
     */
    RandomSource random();

    /**
     * @return the maze being generated
     */
    MazeId maze();

    /**
     * Turns a direction written in the template's own frame into the world frame of the placed room.
     *
     * @param local a direction in template space
     * @return the same direction after the room's mirror and rotation
     */
    Direction orient(Direction local);

    /**
     * @return the server's loot chance multiplier for this maze, snapshotted when the maze was created
     */
    float lootScale();

    /**
     * @return the server's decoration density multiplier for this maze, snapshotted when the maze was created
     */
    float decorationScale();

    /**
     * Places a block if the position is inside the chunk being generated.
     *
     * @param pos   the world position
     * @param state the state to place
     * @return true when the block was placed
     */
    boolean place(BlockPos pos, BlockState state);

    /**
     * Places a block that carries a block entity and returns that block entity so the marker can configure it.
     *
     * @param pos   the world position
     * @param state the state to place
     * @param type  the block entity type the state creates
     * @param <T>   the block entity class
     * @return the placed block entity, or empty when the position is outside the chunk being generated or the state did not create a block entity of {@code type}
     * @since 1.0.0
     */
    default <T extends BlockEntity> Optional<T> placeEntity(BlockPos pos, BlockState state, BlockEntityType<T> type) {
        return place(pos, state) ? level().getBlockEntity(pos, type) : Optional.empty();
    }
}
