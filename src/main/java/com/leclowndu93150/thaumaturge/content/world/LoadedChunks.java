package com.leclowndu93150.thaumaturge.content.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;

public final class LoadedChunks {
    private LoadedChunks() {}

    public static boolean contains(LevelAccessor level, BlockPos pos) {
        return contains(level, pos.getX(), pos.getZ());
    }

    public static boolean contains(LevelAccessor level, int blockX, int blockZ) {
        return level.hasChunk(SectionPos.blockToSectionCoord(blockX), SectionPos.blockToSectionCoord(blockZ));
    }

    public static boolean containsArea(LevelAccessor level, int minX, int minZ, int maxX, int maxZ) {
        int firstX = SectionPos.blockToSectionCoord(minX);
        int firstZ = SectionPos.blockToSectionCoord(minZ);
        int lastX = SectionPos.blockToSectionCoord(maxX);
        int lastZ = SectionPos.blockToSectionCoord(maxZ);
        for (int x = firstX; x <= lastX; x++) {
            for (int z = firstZ; z <= lastZ; z++) {
                if (!level.hasChunk(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }
}
