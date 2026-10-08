package com.leclowndu93150.thaumaturge.content.entity.boss.hierophant;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class EntityHierophantCrescent extends AbstractHierophantSpell {
    public static final float RADIUS = 2.2F;
    public static final float HALF_ANGLE = 1.22F;
    public static final float SPEED = 0.52F;
    public static final float THICKNESS = 0.38F;
    public static final float HEIGHT = 0.7F;
    private static final int LIFETIME = 32;
    private static final EntityDataAccessor<Boolean> LEFT =
            SynchedEntityData.defineId(EntityHierophantCrescent.class, EntityDataSerializers.BOOLEAN);

    public EntityHierophantCrescent(EntityType<? extends EntityHierophantCrescent> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LEFT, false);
    }

    public void setLeft(boolean left) {
        entityData.set(LEFT, left);
    }

    public boolean isLeft() {
        return entityData.get(LEFT);
    }

    public static float radius(float age) {
        return RADIUS * Math.clamp(age / 4.0F, 0.05F, 1.0F);
    }

    public Vec3 center(float age) {
        return position().add(forward().scale(age * SPEED));
    }

    @Override
    public int lifetime() {
        return LIFETIME;
    }

    @Override
    protected void tickSpell(ServerLevel level, int age) {
        final Vec3 center = center(age);
        final float radius = radius(age);
        final Vec3 tip = center.add(forward().scale(radius));
        final Vec3 previous = center(Math.max(0, age - 1)).add(forward().scale(radius(Math.max(0, age - 1))));
        if (level.clip(new ClipContext(previous, tip, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
                        .getType()
                != HitResult.Type.MISS) {
            discard();
            return;
        }
        resolveTargets(
                level,
                new AABB(center, center)
                        .inflate(radius + THICKNESS, HEIGHT + Math.abs(forward().y) * radius, radius + THICKNESS),
                center);
    }

    @Override
    protected boolean intersects(LivingEntity target, int age) {
        final Vec3 delta = target.position().subtract(center(age)).multiply(1, 0, 1);
        final double width = target.getBbWidth() / 2.0;
        final double distance = delta.length();
        final double along = delta.dot(forward());
        return Math.abs(distance - radius(age)) <= THICKNESS + width + SPEED
                && along >= Math.cos(HALF_ANGLE) * radius(age) - width;
    }
}
