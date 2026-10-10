package com.leclowndu93150.thaumaturge.content.equipment.whirlwind;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RepelField implements WhirlwindStrategy {
    private static final double FIELD_REACH = 2.5;
    private static final double EDGE_SHOVE = 0.38;
    private static final double CORE_SHOVE_BONUS = 0.04;
    private static final double MIN_SEPARATION = 1.0E-4;
    private static final double HALF = 0.5;

    @Override
    public void apply(Level level, LivingEntity user) {
        if (level.isClientSide()) {
            return;
        }
        AABB field = user.getBoundingBox().inflate(FIELD_REACH);
        Vec3 centre = middleOf(user);
        Entity mount = user.getVehicle();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, field, candidate -> isRepelled(user, mount, candidate))) {
            shove(centre, target);
        }
    }

    private static boolean isRepelled(LivingEntity user, Entity mount, LivingEntity candidate) {
        return candidate != user && candidate != mount && !(candidate instanceof Player) && candidate.isAlive();
    }

    private static void shove(Vec3 centre, LivingEntity target) {
        Vec3 away = middleOf(target).subtract(centre);
        double distance = away.length();
        if (distance < MIN_SEPARATION) {
            return;
        }
        double closeness = Math.max(0.0, 1.0 - distance / FIELD_REACH);
        double strength = EDGE_SHOVE + CORE_SHOVE_BONUS * closeness;
        target.push(away.scale(strength / distance));
    }

    private static Vec3 middleOf(Entity entity) {
        return entity.position().add(0.0, entity.getBbHeight() * HALF, 0.0);
    }
}
