package com.leclowndu93150.thaumaturge.content.entity;

import java.util.Comparator;
import java.util.List;
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

public final class FocusTargeting {
    private FocusTargeting() {}

    public static boolean isFriendly(@Nullable Entity source, @Nullable Entity target) {
        if (source == null || target == null) {
            return false;
        }
        if (source.getId() == target.getId()) {
            return true;
        }
        if (source.hasIndirectPassenger(target) || target.hasIndirectPassenger(source)) {
            return true;
        }
        if (source.isAlliedTo(target)) {
            return true;
        }
        if (target instanceof OwnableEntity ownable && ownable.getOwner() != null && ownable.getOwner().equals(source)) {
            return true;
        }
        return target instanceof Player && target.level() instanceof ServerLevel serverLevel && !serverLevel.isPvpAllowed();
    }

    public static boolean canEntityBeSeen(Entity looking, Entity target) {
        Vec3 from = new Vec3(looking.getX(), looking.getY() + looking.getBbHeight() / 2.0F, looking.getZ());
        Vec3 to = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2.0F, target.getZ());
        return looking.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, looking)).getType() == HitResult.Type.MISS;
    }

    public static boolean isVisibleTo(float fov, Entity viewer, Vec3 look, Entity target, float range) {
        Vec3 point = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2.0F, target.getZ());
        Vec3 apex = new Vec3(viewer.getX(), viewer.getEyeY(), viewer.getZ());
        Vec3 base = apex.add(look.scale(range));
        return isLyingInCone(point, apex, base, fov);
    }

    public static List<LivingEntity> livingInRangeSorted(Level level, Entity around, double range) {
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(around.getX(), around.getY(), around.getZ(), around.getX(), around.getY(), around.getZ()).inflate(range),
                candidate -> candidate.getId() != around.getId());
        list.sort(Comparator.comparingDouble(around::distanceToSqr));
        return list;
    }

    public static List<LivingEntity> livingInRange(Level level, double x, double y, double z, @Nullable Entity exclude, double range) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(x, y, z, x, y, z).inflate(range), candidate -> exclude == null || candidate.getId() != exclude.getId());
    }

    private static boolean isLyingInCone(Vec3 x, Vec3 t, Vec3 b, float aperture) {
        double halfAperture = aperture / 2.0F;
        Vec3 apexToX = t.subtract(x);
        Vec3 axis = t.subtract(b);
        boolean inInfiniteCone = apexToX.dot(axis) / apexToX.length() / axis.length() > Math.cos(halfAperture);
        if (!inInfiniteCone) {
            return false;
        }
        return apexToX.dot(axis) / axis.length() < axis.length();
    }
}
