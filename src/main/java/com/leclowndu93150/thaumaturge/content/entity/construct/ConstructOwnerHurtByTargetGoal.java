package com.leclowndu93150.thaumaturge.content.entity.construct;

import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class ConstructOwnerHurtByTargetGoal extends AbstractOwnerTargetGoal {
    public ConstructOwnerHurtByTargetGoal(EntityOwnedConstruct construct) {
        super(construct);
    }

    @Override
    protected @Nullable LivingEntity candidate(LivingEntity owner) {
        return owner.getLastHurtByMob();
    }

    @Override
    protected int stamp(LivingEntity owner) {
        return owner.getLastHurtByMobTimestamp();
    }
}
