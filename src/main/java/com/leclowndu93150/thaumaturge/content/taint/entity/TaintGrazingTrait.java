package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitGoals;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Sheep;

public final class TaintGrazingTrait implements MobTrait {
    private static final int PRIORITY = 4;

    @Override
    public void goals(PathfinderMob mob, MobTraitGoals goals) {
        if (mob instanceof Sheep sheep) {
            goals.add(PRIORITY, new TaintGrazeGoal(sheep));
        }
    }
}
