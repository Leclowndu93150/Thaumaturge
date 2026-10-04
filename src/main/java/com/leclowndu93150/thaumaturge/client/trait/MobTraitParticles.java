package com.leclowndu93150.thaumaturge.client.trait;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface MobTraitParticles {
    void spawn(LivingEntity mob, Level level, RandomSource random, double x, double y, double z);
}
