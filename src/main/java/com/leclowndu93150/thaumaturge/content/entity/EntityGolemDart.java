package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class EntityGolemDart extends AbstractArrow {
    public EntityGolemDart(EntityType<? extends EntityGolemDart> type, Level level) {
        super(type, level);
    }

    private EntityGolemDart(LivingEntity shooter) {
        super(TCEntities.GOLEM_DART.get(), shooter, shooter.level(), new ItemStack(Items.ARROW), null);
        pickup = Pickup.DISALLOWED;
    }

    public static void loose(LivingEntity shooter, LivingEntity target, double damage, double loft, float velocity, float inaccuracy) {
        EntityGolemDart dart = new EntityGolemDart(shooter);
        dart.setBaseDamage(damage);
        dart.shoot(target.getX() - shooter.getX(), target.getEyeY() + loft - dart.getY(), target.getZ() - shooter.getZ(), velocity, inaccuracy);
        shooter.level().addFreshEntity(dart);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
