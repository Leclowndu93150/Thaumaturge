package com.leclowndu93150.thaumaturge.content.entity.trait;

import net.minecraft.world.entity.ai.goal.Goal;

public record TraitGoal(boolean target, int priority, Goal goal) {}
