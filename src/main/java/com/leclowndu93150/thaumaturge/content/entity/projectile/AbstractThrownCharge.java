package com.leclowndu93150.thaumaturge.content.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public abstract class AbstractThrownCharge extends ThrowableItemProjectile {
    protected AbstractThrownCharge(EntityType<? extends AbstractThrownCharge> type, Level level) {
        super(type, level);
    }

    protected AbstractThrownCharge(EntityType<? extends AbstractThrownCharge> type, LivingEntity thrower, Level level, ItemStack stack) {
        super(type, thrower, level, stack);
    }

    protected abstract ChargeTrail trail();

    protected abstract void detonate(ServerLevel level);

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            trail().emit(this);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel server && !isRemoved()) {
            detonate(server);
            discard();
        }
    }
}
