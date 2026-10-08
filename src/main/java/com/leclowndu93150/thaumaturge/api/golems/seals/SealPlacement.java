package com.leclowndu93150.thaumaturge.api.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Where a seal type may sit. Checked when a seal is placed and once per second afterwards; a seal that no longer fits pops off as an
 * item.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface SealPlacement {
    /**
     * @param level the level
     * @param pos   the block the seal is stuck to
     * @param face  the face it is stuck on
     * @return whether the seal may stay there
     */
    boolean allows(Level level, BlockPos pos, Direction face);
}
