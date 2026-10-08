package com.leclowndu93150.thaumaturge.content.spell.world;

import com.leclowndu93150.thaumaturge.compat.sable.SableCompat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public final class SpellRayTrace {
    private static final double ENTITY_PADDING = 0.3;

    private SpellRayTrace() {}

    public static BlockHitResult clipBlocks(Level level, @Nullable Entity source, Vec3 from, Vec3 to) {
        CollisionContext context = source != null ? CollisionContext.of(source) : CollisionContext.empty();
        return level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, context));
    }

    public static List<EntityHitResult> entitiesAlong(Level level, @Nullable Entity source, Vec3 from, Vec3 to) {
        from = SableCompat.worldPosition(level, from);
        to = SableCompat.worldPosition(level, to);
        Vec3 start = from;
        AABB sweep = new AABB(from, to).inflate(ENTITY_PADDING + 1.0);
        List<EntityHitResult> hits = new ArrayList<>();
        for (Entity candidate : level.getEntities(
                source, sweep, entity -> entity.isPickable() && entity.isAlive() && !entity.isSpectator())) {
            AABB box = candidate.getBoundingBox().inflate(ENTITY_PADDING);
            Optional<Vec3> entry = box.contains(from) ? Optional.of(from) : box.clip(from, to);
            entry.ifPresent(point -> hits.add(new EntityHitResult(candidate, point)));
        }
        hits.sort(Comparator.comparingDouble(hit -> hit.getLocation().distanceToSqr(start)));
        return hits;
    }

    public static HitResult trace(Level level, @Nullable Entity source, Vec3 from, Vec3 direction, double range) {
        Vec3 to = from.add(direction.normalize().scale(range));
        BlockHitResult block = clipBlocks(level, source, from, to);
        Vec3 reach = block.getType() == HitResult.Type.MISS ? to : block.getLocation();
        List<EntityHitResult> entities = entitiesAlong(level, source, from, reach);
        if (!entities.isEmpty()) {
            return entities.getFirst();
        }
        return block;
    }

    public static List<HitResult> pierce(
            Level level, @Nullable Entity source, Vec3 from, Vec3 direction, double range, int entityCount) {
        Vec3 to = from.add(direction.normalize().scale(range));
        BlockHitResult block = clipBlocks(level, source, from, to);
        Vec3 reach = block.getType() == HitResult.Type.MISS ? to : block.getLocation();
        List<HitResult> out = new ArrayList<>();
        for (EntityHitResult hit : entitiesAlong(level, source, from, reach)) {
            if (out.size() >= entityCount) {
                return out;
            }
            out.add(hit);
        }
        if (block.getType() != HitResult.Type.MISS) {
            out.add(block);
        }
        return out;
    }

    public static Vec3 endOf(HitResult hit, Vec3 from, Vec3 direction, double range) {
        return hit.getType() == HitResult.Type.MISS
                ? from.add(direction.normalize().scale(range))
                : hit.getLocation();
    }
}
