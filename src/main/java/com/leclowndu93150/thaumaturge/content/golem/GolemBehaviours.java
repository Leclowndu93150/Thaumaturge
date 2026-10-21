package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemArmAbility;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructFollowOwnerGoal;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructOwnerHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructOwnerHurtTargetGoal;
import com.leclowndu93150.thaumaturge.content.golem.ai.BlockTaskGoal;
import com.leclowndu93150.thaumaturge.content.golem.ai.EntityTaskGoal;
import com.leclowndu93150.thaumaturge.content.golem.ai.ReturnHomeGoal;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import java.util.ArrayList;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;

final class GolemBehaviours {
    private static final double MELEE_SPEED = 1.15D;
    private static final double FOLLOW_SPEED = 1.0D;
    private static final float FOLLOW_START = 10.0F;
    private static final float FOLLOW_STOP = 2.0F;
    private static final float LOOK_RANGE = 8.0F;
    private static final int PRIORITY_FLOAT = 0;
    private static final int PRIORITY_RANGED = 10;
    private static final int PRIORITY_MELEE = 20;
    private static final int PRIORITY_FOLLOW = 30;
    private static final int PRIORITY_ENTITY_TASK = 30;
    private static final int PRIORITY_BLOCK_TASK = 31;
    private static final int PRIORITY_RETURN_HOME = 40;
    private static final int PRIORITY_LOOK_PLAYER = 50;
    private static final int PRIORITY_LOOK_AROUND = 60;
    private static final int TARGET_OWNER_HURT_BY = 1;
    private static final int TARGET_OWNER_HURT = 2;
    private static final int TARGET_SELF_FOLLOWING = 3;
    private static final int TARGET_SELF_ALONE = 1;

    private GolemBehaviours() {}

    static PathNavigation navigationFor(EntityThaumaturgeGolem golem) {
        if (golem.hasTrait(TTGolemTraits.FLYER)) {
            FlyingPathNavigation flying = new FlyingPathNavigation(golem, golem.level());
            flying.setCanFloat(true);
            return flying;
        }
        return golem.hasTrait(TTGolemTraits.CLIMBER) ? new WallClimberNavigation(golem, golem.level()) : new GroundPathNavigation(golem, golem.level());
    }

    static MoveControl moveControlFor(EntityThaumaturgeGolem golem) {
        return golem.hasTrait(TTGolemTraits.FLYER) ? new EntityThaumaturgeGolem.GolemFlyingMoveControl(golem) : new MoveControl(golem);
    }

    static void assemble(EntityThaumaturgeGolem golem, GoalSelector goals, GoalSelector targets) {
        boolean flyer = golem.hasTrait(TTGolemTraits.FLYER);
        boolean fighter = golem.hasTrait(TTGolemTraits.FIGHTER);
        boolean following = golem.isTrailingOwner();
        if (fighter && !flyer) {
            goals.addGoal(PRIORITY_FLOAT, new FloatGoal(golem));
        }
        if (fighter && golem.hasTrait(TTGolemTraits.RANGED)) {
            IGolemArmAbility ability = golem.properties().arms().ability();
            Goal ranged = ability == null ? null : ability.createRangedGoal(golem);
            if (ranged != null) {
                goals.addGoal(PRIORITY_RANGED, ranged);
            }
        }
        if (fighter) {
            goals.addGoal(PRIORITY_MELEE, new MeleeAttackGoal(golem, MELEE_SPEED, false));
        }
        if (golem.duty().isPresent()) {
            return;
        }
        if (following) {
            goals.addGoal(PRIORITY_FOLLOW, new ConstructFollowOwnerGoal(golem, FOLLOW_SPEED, FOLLOW_START, FOLLOW_STOP));
        } else {
            goals.addGoal(PRIORITY_ENTITY_TASK, new EntityTaskGoal(golem));
            goals.addGoal(PRIORITY_BLOCK_TASK, new BlockTaskGoal(golem));
            goals.addGoal(PRIORITY_RETURN_HOME, new ReturnHomeGoal(golem));
        }
        goals.addGoal(PRIORITY_LOOK_PLAYER, new LookAtPlayerGoal(golem, Player.class, LOOK_RANGE));
        goals.addGoal(PRIORITY_LOOK_AROUND, new RandomLookAroundGoal(golem));
        if (fighter) {
            if (following) {
                targets.addGoal(TARGET_OWNER_HURT_BY, new ConstructOwnerHurtByTargetGoal(golem));
                targets.addGoal(TARGET_OWNER_HURT, new ConstructOwnerHurtTargetGoal(golem));
            }
            targets.addGoal(following ? TARGET_SELF_FOLLOWING : TARGET_SELF_ALONE, new HurtByTargetGoal(golem));
        }
    }

    static void clear(GoalSelector selector) {
        for (WrappedGoal wrapped : new ArrayList<>(selector.getAvailableGoals())) {
            if (wrapped.isRunning()) {
                wrapped.stop();
            }
        }
        selector.removeAllGoals(goal -> true);
    }
}
