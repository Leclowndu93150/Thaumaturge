package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

public class EntityBrainyZombie extends Zombie {
    public EntityBrainyZombie(EntityType<? extends EntityBrainyZombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 3.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
