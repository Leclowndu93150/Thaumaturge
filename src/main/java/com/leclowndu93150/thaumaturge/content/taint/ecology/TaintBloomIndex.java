package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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

    public boolean covers(Level level, BlockPos pos, double radiusSq) {
        for (BlockPos bloom : blooms) {
            if (LevelSpaces.distanceSqr(level, bloom, pos) <= radiusSq) {
                return true;
            }
        }
        return false;
    }
}
