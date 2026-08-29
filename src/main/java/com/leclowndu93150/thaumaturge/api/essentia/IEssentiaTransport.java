package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

/**
 * Sided essentia transport contract for blocks that can move essentia between neighbors.
 *
 * <p>Implementations expose the capability through {@link EssentiaCapabilities#TRANSPORT}.
 *
 * @since 1.0.0
 */
public interface IEssentiaTransport {
    /**
     * Whether this block can connect to a tube on the given side.
     *
     * @param face the side queried, never {@code null}
     * @return {@code true} when the side participates in essentia transport
     */
    boolean isConnectable(Direction face);

    /**
     * Whether the given side accepts incoming essentia.
     *
     * @param face the side queried, never {@code null}
     * @return {@code true} when the side is an inlet
     */
    boolean canInputFrom(Direction face);

    /**
     * Whether the given side emits essentia.
     *
     * @param face the side queried, never {@code null}
     * @return {@code true} when the side is an outlet
     */
    boolean canOutputTo(Direction face);

    /**
     * Sets the suction state for this device. Implementations may ignore the request when they
     * do not generate suction.
     *
     * @param aspect the aspect to draw, or {@code null} to clear
     * @param amount the suction strength
     */
    void setSuction(@Nullable Holder<IAspect> aspect, int amount);

    /**
     * The aspect this device tries to draw on the given side, or {@code null} for any.
     *
     * @param face the side queried, never {@code null}
     * @return the aspect holder, or {@code null} when undirected
     */
    @Nullable
    Holder<IAspect> getSuctionType(Direction face);

    /**
     * The suction strength on the given side.
     *
     * @param face the side queried, never {@code null}
     * @return the suction amount; zero or negative means no suction
     */
    int getSuctionAmount(Direction face);

    /**
     * Removes up to {@code amount} of {@code aspect} from this device through the given side.
     *
     * @param aspect the aspect requested
     * @param amount the maximum amount to extract
     * @param face   the side through which extraction happens
     * @return the amount actually removed
     */
    int takeEssentia(Holder<IAspect> aspect, int amount, Direction face);

    /**
     * Inserts up to {@code amount} of {@code aspect} into this device through the given side.
     *
     * @param aspect the aspect supplied
     * @param amount the maximum amount to insert
     * @param face   the side through which insertion happens
     * @return the amount accepted
     */
    int addEssentia(Holder<IAspect> aspect, int amount, Direction face);

    /**
     * The aspect currently stored or routed on the given side.
     *
     * @param face the side queried, never {@code null}
     * @return the aspect holder, or {@code null} when empty
     */
    @Nullable
    Holder<IAspect> getEssentiaType(Direction face);

    /**
     * The amount of essentia currently stored or available on the given side.
     *
     * @param face the side queried, never {@code null}
     * @return the amount, never negative
     */
    int getEssentiaAmount(Direction face);

    /**
     * The minimum suction required by external pumps to draw essentia from this device.
     *
     * @return the minimum suction threshold
     */
    int getMinimumSuction();

    /**
     * Inserts up to {@code amount} of {@code aspect} through the given side, optionally without
     * committing. When {@code simulate} is true the device state must not change and the return
     * value reports how much would be accepted.
     *
     * <p>The default implementation commits through {@link #addEssentia(Holder, int, Direction)}
     * when not simulating, and otherwise estimates the acceptance as
     * {@code min(amount, spaceFor(aspect, face))}. Devices whose acceptance is not a pure function
     * of free space (filters, one-way valves, buffers) should override this method so that
     * simulation matches the real transfer.
     *
     * @param aspect   the aspect supplied
     * @param amount   the maximum amount to insert
     * @param face     the side through which insertion happens
     * @param simulate when true, do not modify device state
     * @return the amount accepted, or that would be accepted when simulating
     */
    default int addEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        if (!simulate) {
            return addEssentia(aspect, amount, face);
        }
        return Math.min(amount, spaceFor(aspect, face));
    }

    /**
     * Removes up to {@code amount} of {@code aspect} through the given side, optionally without
     * committing. When {@code simulate} is true the device state must not change and the return
     * value reports how much would be extracted.
     *
     * <p>The default implementation commits through {@link #takeEssentia(Holder, int, Direction)}
     * when not simulating. Simulation first verifies that the requested aspect matches
     * {@link #getEssentiaType(Direction)}, then estimates extraction as
     * {@code min(amount, getEssentiaAmount(face))}. Mixed-aspect stores and devices whose extraction
     * is not a pure function of their exposed type and amount should override this method or expose
     * {@link IEssentiaStorage} so simulation matches the real transfer.
     *
     * @param aspect   the aspect requested
     * @param amount   the maximum amount to extract
     * @param face     the side through which extraction happens
     * @param simulate when true, do not modify device state
     * @return the amount removed, or that would be removed when simulating
     */
    default int takeEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        if (!simulate) {
            return takeEssentia(aspect, amount, face);
        }
        Holder<IAspect> stored = getEssentiaType(face);
        if (amount <= 0 || stored == null || !stored.equals(aspect)) return 0;
        return Math.min(amount, getEssentiaAmount(face));
    }

    /**
     * The amount of {@code aspect} this device could still accept through the given side. Used by
     * the simulating {@link #addEssentia(Holder, int, Direction, boolean)} default and by
     * transfer helpers to size batched moves.
     *
     * <p>The default returns {@link Integer#MAX_VALUE}, meaning unbounded acceptance. Devices with
     * a real capacity should override this to report their free space for the aspect.
     *
     * @param aspect the aspect queried
     * @param face   the side queried
     * @return the free space for the aspect on the side; {@link Integer#MAX_VALUE} when unbounded
     */
    default int spaceFor(Holder<IAspect> aspect, Direction face) {
        return Integer.MAX_VALUE;
    }
}
