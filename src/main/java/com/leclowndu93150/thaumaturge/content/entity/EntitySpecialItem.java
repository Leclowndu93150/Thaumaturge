package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public final class EntitySpecialItem extends ItemEntity {
    private static final double TOSS_SPREAD = 0.1;
    private static final double TOSS_LIFT = 0.2;
    private static final double CLIMB_BRAKE = 0.1;

    public EntitySpecialItem(EntityType<? extends EntitySpecialItem> type, Level level) {
        super(type, level);
    }

    public EntitySpecialItem(Level level, double x, double y, double z, ItemStack stack) {
        this(TTEntities.SPECIAL_ITEM.get(), level);
        setPos(x, y, z);
        setItem(stack);
        lifespan = stack.getEntityLifespan(level);
        setDeltaMovement(random.triangle(0.0, TOSS_SPREAD), TOSS_LIFT, random.triangle(0.0, TOSS_SPREAD));
    }

    @Override
    protected double getDefaultGravity() {
        return Math.max(0.0, getDeltaMovement().y) * CLIMB_BRAKE;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }
}
