package com.leclowndu93150.thaumaturge.content.taint.ecology;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;

public final class TaintBloomIndex {
    private final Set<BlockPos> blooms = new HashSet<>();

    public void add(BlockPos pos) {
        blooms.add(pos.immutable());
    }

    public void remove(BlockPos pos) {
        blooms.remove(pos);
    }

    public boolean isEmpty() {
        return blooms.isEmpty();
    }

    public boolean covers(BlockPos pos, double radiusSq) {
        for (BlockPos bloom : blooms) {
            if (bloom.distSqr(pos) <= radiusSq) {
                return true;
            }
        }
        return false;
    }
}
