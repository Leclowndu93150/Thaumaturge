package com.leclowndu93150.thaumaturge.content.world.space;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
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

    public static boolean isWorldOnly() {
        return provider == WorldOnlySpaces.INSTANCE;
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

    public static List<BlockPos> foreignCentersNear(Level level, BlockPos origin, double reach) {
        if (isWorldOnly()) {
            return List.of();
        }
        SpaceFrame own = frameOf(level, origin);
        Vec3 world = own.toWorld(Vec3.atCenterOf(origin));
        List<SpaceFrame> frames = framesTouching(level, new AABB(world, world).inflate(reach));
        List<BlockPos> centers = new ArrayList<>(frames.size());
        for (SpaceFrame frame : frames) {
            if (!frame.equals(own)) {
                centers.add(BlockPos.containing(frame.toLocal(world)));
            }
        }
        return centers;
    }

    public static boolean withinCube(Level level, BlockPos a, BlockPos b, double range) {
        Vec3 delta = worldCenter(level, a).subtract(worldCenter(level, b));
        return Math.abs(delta.x) <= range && Math.abs(delta.y) <= range && Math.abs(delta.z) <= range;
    }

    public static boolean isPlotChunk(Level level, ChunkPos pos) {
        return provider.isPlotChunk(level, pos);
    }

    public static boolean isDetached(Level level, BlockPos pos) {
        return !frameOf(level, pos).isWorld();
    }

    public static BlockPos localBlock(Entity entity) {
        SpaceFrame frame = trackedFrame(entity);
        return frame.isWorld() ? entity.blockPosition() : BlockPos.containing(frame.toLocal(entity.position()));
    }

    public static boolean isPlot(Level level, BlockPos pos) {
        return isPlotChunk(level, new ChunkPos(pos));
    }

    public static Vec3 toWorld(Level level, Vec3 pos) {
        return frameOf(level, pos).toWorld(pos);
    }

    public static Vec3 worldCenter(Level level, BlockPos pos) {
        return frameOf(level, pos).toWorld(Vec3.atCenterOf(pos));
    }

    public static BlockPos worldBlock(Level level, BlockPos pos) {
        SpaceFrame frame = frameOf(level, pos);
        return frame.isWorld() ? pos : BlockPos.containing(frame.toWorld(Vec3.atCenterOf(pos)));
    }

    public static Vec3 faceNormal(Level level, BlockPos pos, Direction face) {
        return frameOf(level, pos).directionToWorld(Vec3.atLowerCornerOf(face.getNormal()));
    }

    public static double distanceSqr(Level level, BlockPos a, BlockPos b) {
        if (isWorldOnly()) {
            return a.distSqr(b);
        }
        return distanceSqr(level, Vec3.atCenterOf(a), Vec3.atCenterOf(b));
    }

    public static double distanceSqr(Level level, Vec3 a, Vec3 b) {
        return toWorld(level, a).distanceToSqr(toWorld(level, b));
    }

    public static <T extends Entity> List<T> entitiesIn(
            Level level, Class<T> type, BlockPos anchor, AABB local, Predicate<? super T> filter) {
        SpaceFrame frame = frameOf(level, anchor);
        if (frame.isWorld()) {
            return level.getEntitiesOfClass(type, local, filter);
        }
        return level.getEntitiesOfClass(
                type,
                worldBox(level, anchor, local),
                entity -> filter.test(entity) && insideLocal(frame, local, entity));
    }

    private static boolean insideLocal(SpaceFrame frame, AABB local, Entity entity) {
        double halfWidth = entity.getBbWidth() / 2.0;
        double halfHeight = entity.getBbHeight() / 2.0;
        return local.inflate(halfWidth, halfHeight, halfWidth)
                .contains(frame.toLocal(entity.getBoundingBox().getCenter()));
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
