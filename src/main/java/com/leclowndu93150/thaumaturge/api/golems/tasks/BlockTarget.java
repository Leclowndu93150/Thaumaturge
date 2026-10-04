package com.leclowndu93150.thaumaturge.api.golems.tasks;

import net.minecraft.core.BlockPos;

/**
 * A task aimed at a fixed block.
 *
 * @param pos the block
 * @since 1.0.0
 */
public record BlockTarget(BlockPos pos) implements TaskTarget {
}
