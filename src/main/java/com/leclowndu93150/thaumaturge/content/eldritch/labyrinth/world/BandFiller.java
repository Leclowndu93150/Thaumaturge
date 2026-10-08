package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

final class BandFiller {
    private static final int LIVE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    private BandFiller() {}

    static void ensure(ChunkAccess chunk, BandSettings band) {
        ChunkPos pos = chunk.getPos();
        if (!chunk.getBlockState(new BlockPos(pos.getMinBlockX(), band.baseY(), pos.getMinBlockZ()))
                .is(band.fillState().getBlock())) {
            fill(chunk, band);
        }
    }

    static void fill(ChunkAccess chunk, BandSettings band) {
        BlockState fill = band.fillState();
        for (int y = band.baseY(); y < band.topY(); y += SectionPos.SECTION_SIZE) {
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
            section.acquire();
            try {
                for (int ly = 0; ly < SectionPos.SECTION_SIZE; ly++) {
                    for (int lx = 0; lx < SectionPos.SECTION_SIZE; lx++) {
                        for (int lz = 0; lz < SectionPos.SECTION_SIZE; lz++) {
                            section.setBlockState(lx, ly, lz, fill, false);
                        }
                    }
                }
            } finally {
                section.release();
            }
        }
        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
    }

    static void fillLive(ServerLevel level, int chunkX, int chunkZ, BandSettings band) {
        BlockState fill = band.fillState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = chunkX * SectionPos.SECTION_SIZE;
        int minZ = chunkZ * SectionPos.SECTION_SIZE;
        for (int y = band.baseY(); y < band.topY(); y++) {
            for (int x = 0; x < SectionPos.SECTION_SIZE; x++) {
                for (int z = 0; z < SectionPos.SECTION_SIZE; z++) {
                    level.setBlock(cursor.set(minX + x, y, minZ + z), fill, LIVE_FLAGS);
                }
            }
        }
    }
}
