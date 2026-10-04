package com.leclowndu93150.thaumaturge.api.research.scan;

import net.minecraft.core.BlockPos;

/**
 * A scan aimed at a block.
 *
 * @param pos the block, in the scanning player's level
 * @since 1.0.0
 */
public record ScannedBlock(BlockPos pos) implements ScanTarget {
}
