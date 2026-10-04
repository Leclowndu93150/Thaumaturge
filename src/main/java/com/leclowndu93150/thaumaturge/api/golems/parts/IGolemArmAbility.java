package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jspecify.annotations.Nullable;

/**
 * Behaviour golem arms add to combat.
 *
 * @since 1.0.0
 */
public interface IGolemArmAbility extends IGolemPartAbility {
    @Override
    default void tick(IGolemAPI golem) {}

    /**
     * Runs on the server after a melee hit lands.
     *
     * @param golem  the attacking golem
     * @param target the entity that was hit
     */
    default void onMeleeHit(IGolemAPI golem, Entity target) {}

    /**
     * Fires a ranged attack on the server.
     *
     * @param golem  the attacking golem
     * @param target the target
     * @param power  the attack strength, from 0.1 at point blank to 1 at the edge of range
     */
    default void onRangedAttack(IGolemAPI golem, LivingEntity target, float power) {}

    /**
     * Creates the goal that drives ranged attacks for golems with the ranged trait.
     *
     * @param mob the golem
     * @return the goal, or null when these arms cannot attack at range
     */
    default @Nullable Goal createRangedGoal(RangedAttackMob mob) {
        return null;
    }
}
