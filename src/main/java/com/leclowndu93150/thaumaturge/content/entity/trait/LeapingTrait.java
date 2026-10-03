package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitGoals;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;

public final class LeapingTrait implements MobTrait {
    private static final int PRIORITY = 4;
    private static final float LEAP_HEIGHT = 0.3F;

    @Override
    public void goals(PathfinderMob mob, MobTraitGoals goals) {
        goals.add(PRIORITY, new LeapAtTargetGoal(mob, LEAP_HEIGHT));
    }
}
