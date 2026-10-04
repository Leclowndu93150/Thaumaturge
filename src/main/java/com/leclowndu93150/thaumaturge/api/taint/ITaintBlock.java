package com.leclowndu93150.thaumaturge.api.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block made of taint. Implement it on the {@code Block}.
 *
 * <p>Taint blocks count as taint for spreading, fibre growth and taint seeds, and an ethereal bloom cleanses them by calling
 * {@link #decay}.
 *
 * @since 1.0.0
 */
public interface ITaintBlock {
    /**
     * Reverts this block toward clean terrain. Called on the server by cleansing effects such as the ethereal bloom; implementations
     * replace or remove the block and may drop items.
     *
     * @param level the level
     * @param pos   the block position
     * @param state the current state of the block
     */
    void decay(Level level, BlockPos pos, BlockState state);
}
