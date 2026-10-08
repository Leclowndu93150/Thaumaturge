package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import net.minecraft.world.level.ChunkPos;

public final class MazeRegions {
    public static final int STRIDE_CHUNKS = 64;
    public static final int OFFSET_CHUNKS = 4096;
    public static final int MARGIN_CHUNKS = 2;

    private MazeRegions() {}

    public static ChunkPos origin(int regionIndex) {
        int ring = (int) Math.floor((Math.sqrt(regionIndex) + 1.0) / 2.0);
        int regionX = 0;
        int regionZ = 0;
        if (ring > 0) {
            int side = ring * 2;
            int offset = regionIndex - (side - 1) * (side - 1);
            int edge = offset / side;
            int along = offset % side;
            switch (edge) {
                case 0 -> {
                    regionX = ring;
                    regionZ = -ring + 1 + along;
                }
                case 1 -> {
                    regionX = ring - 1 - along;
                    regionZ = ring;
                }
                case 2 -> {
                    regionX = -ring;
                    regionZ = ring - 1 - along;
                }
                default -> {
                    regionX = -ring + 1 + along;
                    regionZ = -ring;
                }
            }
        }
        return new ChunkPos(
                OFFSET_CHUNKS + regionX * STRIDE_CHUNKS + MARGIN_CHUNKS,
                OFFSET_CHUNKS + regionZ * STRIDE_CHUNKS + MARGIN_CHUNKS);
    }

    public static long regionKey(int chunkX, int chunkZ) {
        return ChunkPos.asLong(
                Math.floorDiv(chunkX - OFFSET_CHUNKS, STRIDE_CHUNKS),
                Math.floorDiv(chunkZ - OFFSET_CHUNKS, STRIDE_CHUNKS));
    }
}
