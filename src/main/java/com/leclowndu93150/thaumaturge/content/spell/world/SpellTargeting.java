package com.leclowndu93150.thaumaturge.content.spell.world;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class SpellTargeting {
    private SpellTargeting() {}

    public static boolean isAlly(@Nullable Entity caster, Entity other) {
        if (caster == null) {
            return false;
        }
        if (caster == other
                || caster.isAlliedTo(other)
                || caster.hasIndirectPassenger(other)
                || other.hasIndirectPassenger(caster)) {
            return true;
        }
        if (other instanceof OwnableEntity pet && pet.getOwner() == caster) {
            return true;
        }
        return caster instanceof Player
                && other instanceof Player
                && other.level() instanceof ServerLevel server
                && !server.getServer().isPvpAllowed();
    }

    public static List<LivingEntity> livingWithin(
            Level level, Vec3 centre, double radius, Predicate<LivingEntity> filter) {
        double radiusSq = radius * radius;
        return level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(centre, centre).inflate(radius),
                living -> living.isAlive()
                        && !living.isSpectator()
                        && living.getBoundingBox().getCenter().distanceToSqr(centre) <= radiusSq
                        && filter.test(living));
    }

    public static Optional<LivingEntity> nearest(
            Level level, Vec3 centre, double radius, Predicate<LivingEntity> filter) {
        return livingWithin(level, centre, radius, filter).stream()
                .min(Comparator.comparingDouble(
                        living -> living.getBoundingBox().getCenter().distanceToSqr(centre)));
    }

    public static boolean canSee(Level level, Vec3 from, Entity target, @Nullable Entity source) {
        Vec3 to = target.getEyePosition();
        return level.clip(new ClipContext(
                                from,
                                to,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                source == null ? target : source))
                        .getType()
                == HitResult.Type.MISS;
    }

    public static boolean withinCone(Vec3 apex, Vec3 axis, Entity target, double range, double halfAngleCos) {
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(apex);
        double distance = toTarget.length();
        if (distance > range || distance == 0.0) {
            return distance == 0.0;
        }
        return toTarget.scale(1.0 / distance).dot(axis.normalize()) >= halfAngleCos;
    }
}
