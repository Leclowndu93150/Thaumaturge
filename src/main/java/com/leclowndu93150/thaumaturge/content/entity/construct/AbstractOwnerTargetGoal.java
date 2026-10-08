package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jspecify.annotations.Nullable;

public abstract class AbstractOwnerTargetGoal extends TargetGoal {
    private static final TargetingConditions UNSEEN_COMBAT =
            TargetingConditions.forCombat().ignoreLineOfSight();

    private final EntityOwnedConstruct construct;
    private @Nullable LivingEntity candidate;
    private int respondedTo;

    protected AbstractOwnerTargetGoal(EntityOwnedConstruct construct) {
        super(construct, false, true);
        this.construct = construct;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    protected abstract @Nullable LivingEntity candidate(LivingEntity owner);

    protected abstract int stamp(LivingEntity owner);

    @Override
    public boolean canUse() {
        LivingEntity owner = construct.getOwner();
        if (owner == null || stamp(owner) == respondedTo) {
            return false;
        }
        candidate = candidate(owner);
        return canAttack(candidate, UNSEEN_COMBAT);
    }

    @Override
    public void start() {
        mob.setTarget(candidate);
        LivingEntity owner = construct.getOwner();
        if (owner != null) {
            respondedTo = stamp(owner);
        }
        super.start();
    }
}
