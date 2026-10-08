package com.leclowndu93150.thaumaturge.content.entity.construct;

import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class ConstructOwnerHurtTargetGoal extends AbstractOwnerTargetGoal {
    public ConstructOwnerHurtTargetGoal(EntityOwnedConstruct construct) {
        super(construct);
    }

    @Override
    protected @Nullable LivingEntity candidate(LivingEntity owner) {
        return owner.getLastHurtMob();
    }

    @Override
    protected int stamp(LivingEntity owner) {
        return owner.getLastHurtMobTimestamp();
    }
}
