package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Batched, simulation-aware essentia movement between two {@link IEssentiaTransport} endpoints.
 *
 * <p>The core flow in Thaumaturge moves a single unit per tick; addons that pipe essentia at scale
 * need to move many units at once and to size a move before committing it. These helpers do both,
 * driving the endpoints through the simulate-aware overloads on {@link IEssentiaTransport} so that
 * accuracy matches whatever those devices report.
 *
 * <p>A transfer respects sidedness: the source must {@linkplain IEssentiaTransport#canOutputTo
 * output} on its face and the destination must {@linkplain IEssentiaTransport#canInputFrom input}
 * on its face. The moved amount is the minimum of the requested cap, what the source will release,
 * and what the destination will accept. When {@code simulate} is true nothing is committed and the
 * return value reports how much would move.
 *
 * @since 1.0.0
 */
public final class EssentiaTransfer {
    private EssentiaTransfer() {}

    /**
     * Moves up to {@code max} of {@code aspect} from {@code src} through {@code srcFace} into
     * {@code dst} through {@code dstFace}.
     *
     * @param src      the source device
     * @param srcFace  the source side essentia leaves through
     * @param dst      the destination device
     * @param dstFace  the destination side essentia enters through
     * @param aspect   the aspect to move
     * @param max      the maximum amount to move; non-positive values move nothing
     * @param simulate when true, neither device is modified and the return value is the amount that
     *                 would move
     * @return the amount actually moved, or that would move when simulating
     */
    public static int transfer(IEssentiaTransport src, Direction srcFace, IEssentiaTransport dst, Direction dstFace, Holder<IAspect> aspect, int max, boolean simulate) {
        if (max <= 0) {
            return 0;
        }
        if (!src.canOutputTo(srcFace) || !dst.canInputFrom(dstFace)) {
            return 0;
        }
        int available = src.takeEssentia(aspect, max, srcFace, true);
        if (available <= 0) {
            return 0;
        }
        int acceptable = dst.addEssentia(aspect, available, dstFace, true);
        if (acceptable <= 0) {
            return 0;
        }
        if (simulate) {
            return acceptable;
        }
        int taken = src.takeEssentia(aspect, acceptable, srcFace, false);
        int accepted = dst.addEssentia(aspect, taken, dstFace, false);
        if (accepted < taken) {
            src.addEssentia(aspect, taken - accepted, srcFace, false);
        }
        return accepted;
    }

    /**
     * Resolves the transport capability at each position and moves up to {@code max} of
     * {@code aspect} between them. The faces are the sides of each block that face the transfer.
     *
     * @param level    the level
     * @param srcPos   the source block position
     * @param srcFace  the source side essentia leaves through
     * @param dstPos   the destination block position
     * @param dstFace  the destination side essentia enters through
     * @param aspect   the aspect to move
     * @param max      the maximum amount to move
     * @param simulate when true, nothing is committed
     * @return the amount moved, or that would move when simulating; zero when either endpoint
     *         exposes no transport
     */
    public static int transfer(Level level, BlockPos srcPos, Direction srcFace, BlockPos dstPos, Direction dstFace, Holder<IAspect> aspect, int max, boolean simulate) {
        @Nullable
        IEssentiaTransport src = EssentiaAccess.transport(level, srcPos, srcFace);
        @Nullable
        IEssentiaTransport dst = EssentiaAccess.transport(level, dstPos, dstFace);
        if (src == null || dst == null) {
            return 0;
        }
        return transfer(src, srcFace, dst, dstFace, aspect, max, simulate);
    }
}
