package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Legacy-specialized ecological creeper; its explosion consequences are handled centrally. */
public final class EntityTaintCreeper extends Creeper implements ITaintedMob {
    public EntityTaintCreeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }
}
