package com.leclowndu93150.thaumaturge.api.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * A block that stabilises a nearby infusion matrix. The matrix surveys a 17x17 footprint,
 * from three blocks above itself to seven below, and evaluates stabilisers in point-mirrored
 * pairs across its own column: a stabiliser only adds stability when the mirrored position
 * holds the same block with the same stabilisation amount, otherwise the pair subtracts the
 * larger amount instead. Repeated blocks of the same type yield diminishing returns.
 *
 * <p>Implement this on the {@code Block}. Blocks that cannot implement the interface can be
 * added to the {@code thaumaturge:infusion_stabilisers} block tag instead, which grants the
 * default stabilisation amount.
 *
 * @since 1.0.0
 */
public interface IInfusionStabiliser {
    /**
     * The stabilisation amount granted by blocks registered through the tag rather than the
     * interface, and the assumed amount for interface implementors that do not override
     * {@link #getStabilizationAmount}.
     */
    float DEFAULT_STABILIZATION = 0.1F;

    /**
     * Whether this block currently acts as a stabiliser. State-dependent stabilisers, such as
     * blocks that only count while lit, decide here.
     *
     * @param level the level containing the block
     * @param pos the position of this block
     * @return {@code true} when the block should be included in the stability survey
     */
    boolean canStabiliseInfusion(Level level, BlockPos pos);

    /**
     * The stability restored per replenish cycle by a matched, symmetric pair containing this
     * block, before diminishing returns.
     *
     * @param level the level containing the block
     * @param pos the position of this block
     * @return the stabilisation amount
     */
    default float getStabilizationAmount(Level level, BlockPos pos) {
        return DEFAULT_STABILIZATION;
    }

    /**
     * Whether this block pair, despite matching by block and amount, still counts as
     * asymmetric. Used by stabilisers whose orientation or contents must also mirror.
     *
     * @param level the level containing the blocks
     * @param pos the position of this block
     * @param mirrored the point-mirrored position across the matrix
     * @return {@code true} when the pair should be penalised instead of counted
     */
    default boolean hasSymmetryPenalty(Level level, BlockPos pos, BlockPos mirrored) {
        return false;
    }

    /**
     * The stability drained per replenish cycle when {@link #hasSymmetryPenalty} reports a
     * penalty for this pair.
     *
     * @param level the level containing the block
     * @param pos the position of this block
     * @return the penalty amount
     */
    default float getSymmetryPenalty(Level level, BlockPos pos) {
        return 0.0F;
    }
}
