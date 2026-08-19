package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ScanRaycastHelper {

    private ScanRaycastHelper() {}

    public static HitResult performRaycast(Player player) {
        return performRaycast(player, ClipContext.Fluid.ANY);
    }

    public static HitResult performRaycast(Player player, ClipContext.Fluid fluid) {
        return performRaycast(
                new ScanRaycastContext(player.level(), player.getEyePosition(), player.getViewVector(1.0F), player.blockInteractionRange(), player.entityInteractionRange(), fluid, player));
    }

    public static HitResult performRaycast(ScanRaycastContext ctx) {
        Level level = ctx.level();
        Vec3 start = ctx.start();
        Vec3 direction = ctx.direction();
        double blockReach = ctx.blockReach();
        double entityReach = ctx.entityReach();
        ClipContext.Fluid fluidFilter = ctx.fluidFilter();

        if (level == null)
            return null;

        BlockHitResult blockResult = level.clip(new ClipContext(start, start.add(direction.scale(blockReach)), ClipContext.Block.OUTLINE, fluidFilter, ctx.entity()));

        EntityHitResult entityResult = clipEntity(level, start, direction, entityReach, ctx.entity());

        if (blockResult.getType() != HitResult.Type.MISS && entityResult != null) {
            double blockDistance = blockResult.getLocation().distanceToSqr(start);
            double entityDistance = entityResult.getLocation().distanceToSqr(start);
            if (blockDistance < entityDistance) {
                return blockResult;
            } else {
                return entityResult;
            }
        } else if (blockResult.getType() != HitResult.Type.MISS) {
            return blockResult;
        }
        return entityResult != null ? entityResult : BlockHitResult.miss(ctx.entity().getEyePosition(), ctx.entity().getDirection(), ctx.entity().blockPosition());
    }

    private static EntityHitResult clipEntity(Level level, Vec3 start, Vec3 direction, double entityReach, Entity entity) {
        AABB box = entity.getBoundingBox().expandTowards(direction.scale(entityReach)).inflate(1.0F, 1.0F, 1.0F);
        return ProjectileUtil.getEntityHitResult(entity, start, start.add(direction.scale(entityReach)), box, (e) -> !e.isSpectator(), entityReach * entityReach);
    }

    public static record ScanRaycastContext(Level level, Vec3 start, Vec3 direction, double blockReach, double entityReach, ClipContext.Fluid fluidFilter, Entity entity) {
        public ScanRaycastContext {
            Objects.requireNonNull(level, "level cannot be null");
            Objects.requireNonNull(start, "start cannot be null");
            Objects.requireNonNull(direction, "direction cannot be null");
            Objects.requireNonNull(fluidFilter, "fluidFilter cannot be null");
            Objects.requireNonNull(entity, "entity cannot be null");
        }

        public ScanRaycastContext withLevel(Level level) {
            return new ScanRaycastContext(level, this.start, this.direction, this.blockReach, this.entityReach, this.fluidFilter, this.entity);
        }

        public ScanRaycastContext withStart(Vec3 start) {
            return new ScanRaycastContext(this.level, start, this.direction, this.blockReach, this.entityReach, this.fluidFilter, this.entity);
        }

        public ScanRaycastContext withDirection(Vec3 direction) {
            return new ScanRaycastContext(this.level, this.start, direction, this.blockReach, this.entityReach, this.fluidFilter, this.entity);
        }

        public ScanRaycastContext withBlockReach(double blockReach) {
            return new ScanRaycastContext(this.level, this.start, this.direction, blockReach, this.entityReach, this.fluidFilter, this.entity);
        }

        public ScanRaycastContext withEntityReach(double entityReach) {
            return new ScanRaycastContext(this.level, this.start, this.direction, this.blockReach, entityReach, this.fluidFilter, this.entity);
        }

        public ScanRaycastContext withFluidFilter(ClipContext.Fluid fluidFilter) {
            return new ScanRaycastContext(this.level, this.start, this.direction, this.blockReach, this.entityReach, fluidFilter, this.entity);
        }

        public ScanRaycastContext withEntity(Entity entity) {
            return new ScanRaycastContext(this.level, this.start, this.direction, this.blockReach, this.entityReach, this.fluidFilter, entity);
        }
    }
}
