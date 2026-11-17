package com.leclowndu93150.thaumaturge.content.world.space;

import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

enum WorldOnlySpaces implements LevelSpaceProvider {
    INSTANCE;

    private static final List<SpaceFrame> WORLD = List.of(WorldFrame.INSTANCE);

    @Override
    public SpaceFrame frameOf(Level level, Position pos) {
        return WorldFrame.INSTANCE;
    }

    @Override
    public SpaceFrame frameOf(Level level, Vec3i pos) {
        return WorldFrame.INSTANCE;
    }

    @Override
    public SpaceFrame trackedFrame(Entity entity) {
        return WorldFrame.INSTANCE;
    }

    @Override
    public boolean isPlotChunk(Level level, ChunkPos pos) {
        return false;
    }

    @Override
    public List<SpaceFrame> framesTouching(Level level, AABB worldBox) {
        return WORLD;
    }

    @Override
    public @Nullable LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        return LoadedChunks.contains(level, pos) ? level.getChunkAt(pos) : null;
    }
}
