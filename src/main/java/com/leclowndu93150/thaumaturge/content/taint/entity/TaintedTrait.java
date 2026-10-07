package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitGoals;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public final class TaintedTrait implements MobTrait {
    private static final double TAINTED_HEALTH = 25.0;
    private static final double TAINTED_DAMAGE = 0.5;
    private static final float ASSIGN_HEAL = 25.0F;
    private static final int VITIUM = 3;
    private static final int FLOAT_PRIORITY = 0;
    private static final int MELEE_PRIORITY = 2;
    private static final int STROLL_PRIORITY = 5;
    private static final int LOOK_PRIORITY = 6;
    private static final int LOOK_AROUND_PRIORITY = 7;
    private static final int HURT_BY_PRIORITY = 0;
    private static final int PLAYER_PRIORITY = 2;
    private static final int VILLAGER_PRIORITY = 3;
    private static final int ANIMAL_PRIORITY = 8;
    private static final double SPEED = 1.0;
    private static final float LOOK_RANGE = 6.0F;
    private static final int TARGET_INTERVAL = 10;

    @Override
    public boolean isTaint() {
        return true;
    }

    @Override
    public boolean replacesNativeAi(LivingEntity mob) {
        return mob instanceof PathfinderMob && !(mob instanceof Enemy);
    }

    @Override
    public void modifiers(LivingEntity mob, MobTraitModifiers modifiers) {
        TaintedProfile profile = TaintedProfile.of(mob.getType());
        if (profile == null || profile.attributes().isEmpty()) {
            modifiers.add(Attributes.MAX_HEALTH, TAINTED_HEALTH, AttributeModifier.Operation.ADD_VALUE);
            modifiers.add(Attributes.ATTACK_DAMAGE, TAINTED_DAMAGE, AttributeModifier.Operation.ADD_VALUE);
            return;
        }
        for (Map.Entry<Holder<Attribute>, Double> entry : profile.attributes().entrySet()) {
            modifiers.setBase(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void goals(PathfinderMob mob, MobTraitGoals goals) {
        if (!replacesNativeAi(mob)) {
            return;
        }
        goals.add(FLOAT_PRIORITY, new FloatGoal(mob));
        goals.add(MELEE_PRIORITY, new MeleeAttackGoal(mob, SPEED, false));
        goals.add(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(mob, SPEED));
        goals.add(LOOK_PRIORITY, new LookAtPlayerGoal(mob, Player.class, LOOK_RANGE));
        goals.add(LOOK_AROUND_PRIORITY, new RandomLookAroundGoal(mob));
        goals.addTarget(HURT_BY_PRIORITY, new HurtByTargetGoal(mob));
        goals.addTarget(PLAYER_PRIORITY, new NearestAttackableTargetGoal<>(mob, Player.class, true));
        TaintedProfile profile = TaintedProfile.of(mob.getType());
        if (profile != null && profile.huntsVillagers()) {
            goals.addTarget(
                    VILLAGER_PRIORITY,
                    new NearestAttackableTargetGoal<>(
                            mob, Villager.class, TARGET_INTERVAL, true, false, target -> !MobTraits.isTainted(target)));
        }
        if (profile != null && profile.huntsAnimals()) {
            goals.addTarget(
                    ANIMAL_PRIORITY,
                    new NearestAttackableTargetGoal<>(
                            mob, Animal.class, TARGET_INTERVAL, true, false, target -> !MobTraits.isTainted(target)));
        }
    }

    @Override
    public AspectList aspects(LivingEntity mob) {
        Holder<IAspect> vitium =
                mob.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TTAspects.VITIUM);
        return AspectList.EMPTY.add(vitium, VITIUM);
    }

    @Override
    public void onAdded(LivingEntity mob) {
        TaintedProfile profile = TaintedProfile.of(mob.getType());
        if (profile != null) {
            for (Holder<MobTrait> trait : profile.traits()) {
                MobTraits.add(mob, trait);
            }
        }
        mob.heal(ASSIGN_HEAL);
    }

    @Override
    public void onRemoved(LivingEntity mob) {
        TaintedProfile profile = TaintedProfile.of(mob.getType());
        if (profile != null) {
            for (Holder<MobTrait> trait : profile.traits()) {
                MobTraits.remove(mob, trait);
            }
        }
    }
}
