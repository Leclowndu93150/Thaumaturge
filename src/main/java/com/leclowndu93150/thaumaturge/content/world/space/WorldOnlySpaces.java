package com.leclowndu93150.thaumaturge.content.world.space;

import java.util.List;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

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
}
