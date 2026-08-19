package com.leclowndu93150.thaumaturge.api.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Visual routing contract for blocks that emit or receive essentia stream effects.
 *
 * <p>By default a stream travels directly between its two endpoints and may clip through
 * the block models at either end. A {@link net.minecraft.world.level.block.Block} implementing
 * this interface declares a port instead: the stream attaches at {@link StreamPort#anchor()}
 * and travels straight through {@link StreamPort#clearance()} before it is allowed to curve
 * toward the far endpoint. Arriving streams pass the same two points in reverse order.
 *
 * <p>Queried on the client only, once per spawned stream. Implementations must derive the
 * port purely from the given arguments and must not access block entities or other level
 * state.
 *
 * @since 1.0.0
 */
public interface IEssentiaStreamPort {
    /**
     * Describes where essentia streams attach to this block.
     *
     * @param level the level the block sits in
     * @param pos the position of this block
     * @param state the current block state
     * @param farEnd the opposite endpoint of the stream, in world coordinates
     * @param outgoing {@code true} when the stream leaves this block, {@code false} when it arrives
     * @return the port geometry for this stream, never {@code null}
     */
    StreamPort essentiaStreamPort(BlockGetter level, BlockPos pos, BlockState state, Vec3 farEnd, boolean outgoing);

    /**
     * Port geometry for one end of an essentia stream.
     *
     * @param anchor the point the stream visually attaches to, in world coordinates
     * @param clearance the point the stream passes through straight off the port before curving away
     */
    record StreamPort(Vec3 anchor, Vec3 clearance) {
    }
}
