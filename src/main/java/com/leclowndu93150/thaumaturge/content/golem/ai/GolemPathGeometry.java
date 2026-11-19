package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public final class GolemPathGeometry {
    private static final double EPSILON = 1.0E-7;
    private static final Vec3[] AXES = {new Vec3(1, 0, 0), new Vec3(0, 1, 0), new Vec3(0, 0, 1)};

    private GolemPathGeometry() {}

    public record Surface(SpaceFrame frame, Vec3 localFeet) {
        public Vec3 worldFeet() {
            return frame.toWorld(localFeet);
        }
    }

    public static AABB body(Mob mob, Vec3 feet) {
        double halfWidth = mob.getBbWidth() / 2.0;
        return new AABB(
                feet.x - halfWidth,
                feet.y,
                feet.z - halfWidth,
                feet.x + halfWidth,
                feet.y + mob.getBbHeight(),
                feet.z + halfWidth);
    }

    public static boolean clear(Level level, Mob mob, AABB worldBody) {
        return clear(level, mob, worldBody, 0.0);
    }

    private static boolean clear(Level level, Mob mob, AABB worldBody, double inset) {
        AABB collisionBody = worldBody.deflate(inset, 0, inset);
        CollisionContext context = CollisionContext.of(mob);
        for (SpaceFrame frame : LevelSpaces.framesTouching(level, worldBody)) {
            AABB local = localBounds(frame, worldBody);
            for (BlockPos pos : cells(local)) {
                if (!level.isLoaded(pos)) {
                    if (frame.isWorld()) {
                        return false;
                    }
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (hazard(state) && intersects(worldBody, new AABB(pos), frame)) {
                    return false;
                }
                for (AABB box : state.getCollisionShape(level, pos, context).toAabbs()) {
                    if (intersects(collisionBody, box.move(pos), frame)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static @Nullable Surface surface(
            Level level, Mob mob, double worldX, double worldZ, double minY, double maxY) {
        return surface(level, mob, worldX, worldZ, minY, maxY, 0.0);
    }

    static @Nullable Surface startingSurface(
            Level level, Mob mob, double worldX, double worldZ, double minY, double maxY) {
        // Allow tiny contact overlaps at the start; all subsequent movement uses full clearance.
        return surface(level, mob, worldX, worldZ, minY, maxY, 0.002);
    }

    private static @Nullable Surface surface(
            Level level, Mob mob, double worldX, double worldZ, double minY, double maxY, double inset) {
        Vec3 top = new Vec3(worldX, maxY + EPSILON, worldZ);
        Vec3 bottom = new Vec3(worldX, minY - EPSILON, worldZ);
        List<Surface> candidates = new ArrayList<>();
        CollisionContext context = CollisionContext.of(mob);
        double halfWidth = mob.getBbWidth() / 2.0;
        AABB column = new AABB(top, bottom).inflate(halfWidth, EPSILON, halfWidth);
        for (SpaceFrame frame : LevelSpaces.framesTouching(level, column)) {
            // The uphill corner determines an upright body's footing on a tilted deck.
            for (int sample = 0; sample < 5; sample++) {
                double dx = sample == 0 ? 0 : ((sample & 1) == 0 ? -halfWidth : halfWidth);
                double dz = sample == 0 ? 0 : ((sample & 2) == 0 ? -halfWidth : halfWidth);
                Vec3 from = frame.toLocal(top.add(dx, 0, dz));
                Vec3 to = frame.toLocal(bottom.add(dx, 0, dz));
                for (BlockPos pos : cells(new AABB(from, to))) {
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (hazard(state)) {
                        continue;
                    }
                    var hit = state.getCollisionShape(level, pos, context).clip(from, to, pos);
                    if (hit == null
                            || frame.directionToWorld(Vec3.atLowerCornerOf(
                                                    hit.getDirection().getNormal()))
                                            .y
                                    <= 0.5) {
                        continue;
                    }
                    double height = frame.toWorld(hit.getLocation()).y;
                    if (height >= minY - EPSILON && height <= maxY + EPSILON) {
                        candidates.add(new Surface(frame, frame.toLocal(new Vec3(worldX, height, worldZ))));
                    }
                }
            }
        }
        candidates.sort(Comparator.comparingDouble((Surface point) -> point.worldFeet().y)
                .reversed());
        for (Surface candidate : candidates) {
            // Ignore tiny transform-rounding overlaps with the supporting surface.
            if (clear(level, mob, body(mob, candidate.worldFeet().add(0, 0.001, 0)), inset)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean hazard(BlockState state) {
        return WalkNodeEvaluator.isBurningBlock(state)
                || state.getFluidState().is(FluidTags.LAVA)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.WITHER_ROSE)
                || state.is(Blocks.POWDER_SNOW);
    }

    private static Iterable<BlockPos> cells(AABB box) {
        // Fences and walls can extend above their own block cell.
        return BlockPos.betweenClosed(
                BlockPos.containing(box.minX, box.minY - 1, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ));
    }

    private static AABB localBounds(SpaceFrame frame, AABB world) {
        Vec3 first = frame.toLocal(new Vec3(world.minX, world.minY, world.minZ));
        AABB bounds = new AABB(first, first);
        for (int corner = 1; corner < 8; corner++) {
            Vec3 point = frame.toLocal(new Vec3(
                    (corner & 1) == 0 ? world.minX : world.maxX,
                    (corner & 2) == 0 ? world.minY : world.maxY,
                    (corner & 4) == 0 ? world.minZ : world.maxZ));
            bounds = bounds.minmax(new AABB(point, point));
        }
        return bounds;
    }

    private static boolean intersects(AABB world, AABB local, SpaceFrame frame) {
        Vec3[] axes = {frame.directionToWorld(AXES[0]), frame.directionToWorld(AXES[1]), frame.directionToWorld(AXES[2])
        };
        Vec3 offset = frame.toWorld(local.getCenter()).subtract(world.getCenter());
        for (Vec3 axis : AXES) {
            if (separated(axis, offset, world, local, axes)) {
                return false;
            }
        }
        for (Vec3 axis : axes) {
            if (separated(axis, offset, world, local, axes)) {
                return false;
            }
            for (Vec3 worldAxis : AXES) {
                Vec3 cross = axis.cross(worldAxis);
                if (cross.lengthSqr() > EPSILON * EPSILON && separated(cross, offset, world, local, axes)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean separated(Vec3 axis, Vec3 offset, AABB world, AABB local, Vec3[] axes) {
        double worldRadius = (world.getXsize() * Math.abs(axis.x)
                        + world.getYsize() * Math.abs(axis.y)
                        + world.getZsize() * Math.abs(axis.z))
                / 2.0;
        double localRadius = (local.getXsize() * Math.abs(axis.dot(axes[0]))
                        + local.getYsize() * Math.abs(axis.dot(axes[1]))
                        + local.getZsize() * Math.abs(axis.dot(axes[2])))
                / 2.0;
        return Math.abs(axis.dot(offset)) >= worldRadius + localRadius - EPSILON * axis.length();
    }
}
