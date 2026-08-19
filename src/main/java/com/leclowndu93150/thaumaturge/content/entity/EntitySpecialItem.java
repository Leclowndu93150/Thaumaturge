package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class EntitySpecialItem extends ItemEntity {

    public EntitySpecialItem(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntitySpecialItem(Level level, double x, double y, double z, ItemStack itemStack) {
        super(TCEntities.SPECIAL_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setItem(itemStack);
        this.setDeltaMovement(this.random.nextDouble() * 0.2 - 0.1, 0.2, this.random.nextDouble() * 0.2 - 0.1);
        this.lifespan = itemStack.getItem() == null ? 6000 : itemStack.getEntityLifespan(level);
    }

    public EntitySpecialItem(Level level, double x, double y, double z, ItemStack itemStack, double deltaX, double deltaY, double deltaZ) {
        this(TCEntities.SPECIAL_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setItem(itemStack);
        this.setDeltaMovement(deltaX, deltaY, deltaZ);
        this.lifespan = itemStack.getItem() == null ? 6000 : itemStack.getEntityLifespan(level);
    }

    @Override
    public void tick() {
        if (tickCount > 1) {
            if (this.getDeltaMovement().y > 0.0)
                setDeltaMovement(getDeltaMovement().multiply(1, 0.9, 1));

            setDeltaMovement(getDeltaMovement().add(0, 0.04, 0));
            super.tick();
        }
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }
}
