package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public final class TaintColumns {
    public static final MapCodec<TaintColumns> CODEC =
            Codec.INT.optionalFieldOf("changed", 0).xmap(TaintColumns::new, columns -> columns.changed);

    private static final int QUARTS_PER_SIDE = 4;
    private static final int ALL_COLUMNS = (1 << QUARTS_PER_SIDE * QUARTS_PER_SIDE) - 1;

    private int changed;

    public TaintColumns() {
        this(0);
    }

    private TaintColumns(int changed) {
        this.changed = changed & ALL_COLUMNS;
    }

    public boolean isChanged(int localQuartX, int localQuartZ) {
        return (changed & bit(localQuartX, localQuartZ)) != 0;
    }

    public void setChanged(int localQuartX, int localQuartZ, boolean value) {
        if (value) {
            changed |= bit(localQuartX, localQuartZ);
        } else {
            changed &= ~bit(localQuartX, localQuartZ);
        }
    }

    public boolean isEmpty() {
        return changed == 0;
    }

    private static int bit(int localQuartX, int localQuartZ) {
        return 1 << (localQuartZ & QUARTS_PER_SIDE - 1) * QUARTS_PER_SIDE + (localQuartX & QUARTS_PER_SIDE - 1);
    }
}
