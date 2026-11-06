package com.leclowndu93150.thaumaturge.content.world.space;

import java.util.List;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface LevelSpaceProvider {
    SpaceFrame frameOf(Level level, Position pos);

    SpaceFrame frameOf(Level level, Vec3i pos);

    SpaceFrame trackedFrame(Entity entity);

    boolean isPlotChunk(Level level, ChunkPos pos);

    List<SpaceFrame> framesTouching(Level level, AABB worldBox);
}
