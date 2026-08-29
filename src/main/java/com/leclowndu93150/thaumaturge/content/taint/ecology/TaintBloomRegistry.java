package com.leclowndu93150.thaumaturge.content.taint.ecology;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Tracks loaded Ethereal Blooms without persisting positions or loading chunks. */
public final class TaintBloomRegistry {
    public static final int PROTECTION_RADIUS = 8;
    private static final double PROTECTION_RADIUS_SQ = PROTECTION_RADIUS * PROTECTION_RADIUS;
    private static final Map<ServerLevel, Set<BlockPos>> BLOOMS = new WeakHashMap<>();

    private TaintBloomRegistry() {}

    public static void add(ServerLevel level, BlockPos pos) {
        BLOOMS.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.immutable());
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        Set<BlockPos> blooms = BLOOMS.get(level);
        if (blooms == null) {
            return;
        }
        blooms.remove(pos);
        if (blooms.isEmpty()) {
            BLOOMS.remove(level);
        }
    }

    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        Set<BlockPos> blooms = BLOOMS.get(level);
        if (blooms == null) {
            return false;
        }
        for (BlockPos bloom : blooms) {
            if (bloom.distSqr(pos) <= PROTECTION_RADIUS_SQ) {
                return true;
            }
        }
        return false;
    }
}
