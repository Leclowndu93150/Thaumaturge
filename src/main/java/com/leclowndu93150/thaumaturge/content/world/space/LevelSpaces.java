package com.leclowndu93150.thaumaturge.content.world.space;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LevelSpaces {
    private static final int AABB_CORNERS = 8;
    private static volatile LevelSpaceProvider provider = WorldOnlySpaces.INSTANCE;

    private LevelSpaces() {}

    public static void install(LevelSpaceProvider spaces) {
        if (provider != WorldOnlySpaces.INSTANCE) {
            throw new IllegalStateException("Level spaces already installed");
        }
        provider = spaces;
    }

    public static SpaceFrame frameOf(Level level, Vec3 pos) {
        return provider.frameOf(level, pos);
    }

    public static SpaceFrame frameOf(Level level, BlockPos pos) {
        return provider.frameOf(level, pos);
    }

    public static SpaceFrame trackedFrame(Entity entity) {
        return provider.trackedFrame(entity);
    }

    public static List<SpaceFrame> framesTouching(Level level, AABB worldBox) {
        return provider.framesTouching(level, worldBox);
    }

    public static boolean isDetached(Level level, BlockPos pos) {
        return !frameOf(level, pos).isWorld();
    }

    public static Vec3 toWorld(Level level, Vec3 pos) {
        return frameOf(level, pos).toWorld(pos);
    }

    public static Vec3 worldCenter(Level level, BlockPos pos) {
        return frameOf(level, pos).toWorld(Vec3.atCenterOf(pos));
    }

    public static double distanceSqr(Level level, Vec3 a, Vec3 b) {
        return toWorld(level, a).distanceToSqr(toWorld(level, b));
    }

    public static AABB worldBox(Level level, BlockPos anchor, AABB local) {
        SpaceFrame frame = frameOf(level, anchor);
        if (frame.isWorld()) {
            return local;
        }
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int corner = 0; corner < AABB_CORNERS; corner++) {
            Vec3 point = frame.toWorld(new Vec3(
                    (corner & 1) == 0 ? local.minX : local.maxX,
                    (corner & 2) == 0 ? local.minY : local.maxY,
                    (corner & 4) == 0 ? local.minZ : local.maxZ));
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            minZ = Math.min(minZ, point.z);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
            maxZ = Math.max(maxZ, point.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
