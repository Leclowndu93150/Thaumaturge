package com.leclowndu93150.thaumaturge.content.entity.boss.hierophant;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class EntityHierophantNova extends AbstractHierophantSpell {
    public static final float SPEED = 0.45F;
    public static final float START_RADIUS = 1.5F;
    public static final float THICKNESS = 0.45F;
    public static final float HEIGHT = 0.6F;
    private static final int LIFETIME = 28;

    public EntityHierophantNova(EntityType<? extends EntityHierophantNova> type, Level level) {
        super(type, level);
    }

    @Override
    public int lifetime() {
        return LIFETIME;
    }

    public static float radius(float age) {
        return START_RADIUS + age * SPEED;
    }

    @Override
    protected void tickSpell(ServerLevel level, int age) {
        final float reach = radius(age) + THICKNESS;
        resolveTargets(
                level,
                new AABB(getX() - reach, getY(), getZ() - reach, getX() + reach, getY() + HEIGHT, getZ() + reach),
                position().add(0, HEIGHT / 2, 0));
    }

    @Override
    protected boolean intersects(LivingEntity target, int age) {
        final double distance = target.position().subtract(position()).horizontalDistance();
        return Math.abs(distance - radius(age)) <= THICKNESS + SPEED + target.getBbWidth() / 2.0;
    }
}
