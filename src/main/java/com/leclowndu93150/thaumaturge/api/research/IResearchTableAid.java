package com.leclowndu93150.thaumaturge.api.research;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block that assists research when it stands directly on top of a research table. The table
 * checks the block above each of its two halves whenever a player writes an aspect onto a note or
 * combines two aspects, and the chances of both blocks are added together.
 *
 * <p>Implement this on the {@code Block}. The check runs server-side, only when an aspect would be
 * taken from the player's own pool; aspects drawn from the table's bonus pool are never saved.
 *
 * @since 1.0.0
 */
public interface IResearchTableAid {
    /**
     * The chance, between {@code 0} and {@code 1}, that an aspect written or combined at the table
     * below is not consumed.
     *
     * @param level the level containing this block
     * @param pos the position of this block, one above the research table
     * @param state the current state of this block
     * @return the save chance, or {@code 0} while this aid is inactive
     * @since 1.0.0
     */
    float aspectSaveChance(Level level, BlockPos pos, BlockState state);
}
