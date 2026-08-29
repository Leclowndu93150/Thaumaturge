package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

/** TC4 spore-burst spiderling. Kept deliberately small and fragile. */
public final class EntityTaintSpider extends Spider implements ITaintedMob {
    public EntityTaintSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        this.xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }
}
