package com.leclowndu93150.thaumaturge.content.wands;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/** Exact centivis calculations for the int-backed wand storage format. */
final class WandVisNumbers {
    private WandVisNumbers() {}

    static int capacity(int rodVis, int centivisPerVis) {
        return clampToStorage((long) rodVis * centivisPerVis);
    }

    static int clampToStorage(long centivis) {
        return (int) Math.clamp(centivis, 0L, Integer.MAX_VALUE);
    }

    /** Accept whole multiples of {@code step}, never more than the remaining room. */
    static long accepted(int stored, int capacity, long offered, int step) {
        if (offered <= 0) {
            return 0;
        }
        long room = Math.max(0L, (long) capacity - stored);
        return Math.min(offered, (room / step) * step);
    }

    /** An unrepresentable positive cost must be unaffordable, even for a full int-backed pool. */
    static long scaledCost(int base, float modifier) {
        double result = (double) base * modifier;
        if (Double.isNaN(result) || result > Integer.MAX_VALUE) {
            return (long) Integer.MAX_VALUE + 1L;
        }
        // Negative costs historically grant vis. Cap that credit to the storage range.
        if (result < -Integer.MAX_VALUE) {
            return -(long) Integer.MAX_VALUE;
        }
        return (long) result;
    }

    /** Preserve both fractional vis and large integer values without float rounding. */
    static String format(int centivis) {
        return new DecimalFormat("0.##").format(BigDecimal.valueOf(centivis, 2));
    }
}
