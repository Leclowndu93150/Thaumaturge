package com.leclowndu93150.thaumaturge.content.world.space;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockSightline {
    private BlockSightline() {}

    public static boolean isClear(
            Level level,
            Vec3 from,
            Vec3 to,
            BlockPos ignoreFrom,
            BlockPos ignoreTo,
            double maxDistance,
            CollisionContext context) {
        Vec3 worldFrom = LevelSpaces.toWorld(level, from);
        Vec3 worldTo = LevelSpaces.toWorld(level, to);
        if (worldFrom.distanceToSqr(worldTo) > maxDistance * maxDistance) {
            return false;
        }
        for (SpaceFrame frame : LevelSpaces.framesTouching(level, new AABB(worldFrom, worldTo))) {
            Vec3 start = frame.toLocal(worldFrom);
            Vec3 end = frame.toLocal(worldTo);
            boolean detached = !frame.isWorld();
            Boolean blocked = BlockGetter.traverseBlocks(
                    start,
                    end,
                    level,
                    (getter, cell) -> blocks(getter, cell, start, end, ignoreFrom, ignoreTo, detached, context)
                            ? Boolean.TRUE
                            : null,
                    getter -> Boolean.FALSE);
            if (blocked) {
                return false;
            }
        }
        return true;
    }

    private static boolean blocks(
            Level level,
            BlockPos cell,
            Vec3 start,
            Vec3 end,
            BlockPos ignoreFrom,
            BlockPos ignoreTo,
            boolean detached,
            CollisionContext context) {
        if (cell.equals(ignoreFrom) || cell.equals(ignoreTo) || (detached && !level.isLoaded(cell))) {
            return false;
        }
        VoxelShape shape = level.getBlockState(cell).getCollisionShape(level, cell, context);
        return !shape.isEmpty() && shape.clip(start, end, cell) != null;
    }
}
