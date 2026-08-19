package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.mixin.world.entity.MobAccessor;
import com.leclowndu93150.thaumaturge.registry.TCAttributes;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public final class ChampionHelper {
    public static final int NOT_PROCESSED = -2;
    public static final int NOT_CHAMPION = -1;

    private static final Identifier CHAMPION_HEALTH_ID = TCIds.rl("champion/health");
    private static final AttributeModifier CHAMPION_HEALTH = new AttributeModifier(CHAMPION_HEALTH_ID, 100.0, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier CHAMPION_DAMAGE_ID = TCIds.rl("champion/damage");
    private static final AttributeModifier CHAMPION_DAMAGE = new AttributeModifier(CHAMPION_DAMAGE_ID, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final Identifier BOLD_SPEED_ID = TCIds.rl("champion/bold_speed");
    private static final AttributeModifier BOLD_SPEED = new AttributeModifier(BOLD_SPEED_ID, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final Identifier MIGHTY_DAMAGE_ID = TCIds.rl("champion/mighty_damage");
    private static final AttributeModifier MIGHTY_DAMAGE = new AttributeModifier(MIGHTY_DAMAGE_ID, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final Identifier TAINTED_HEALTH_ID = TCIds.rl("champion/tainted_health");
    private static final AttributeModifier TAINTED_HEALTH = new AttributeModifier(TAINTED_HEALTH_ID, 25.0, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier TAINTED_DAMAGE_ID = TCIds.rl("champion/tainted_damage");
    private static final AttributeModifier TAINTED_DAMAGE = new AttributeModifier(TAINTED_DAMAGE_ID, 0.5, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier TAINTED_AI_MARKER_ID = TCIds.rl("champion/tainted_ai");
    private static final AttributeModifier TAINTED_AI_MARKER = new AttributeModifier(TAINTED_AI_MARKER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE);

    private static final float ASSIGN_HEAL = 25.0F;

    private ChampionHelper() {}

    public static int championType(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(TCAttributes.CHAMPION_MOD);
        return instance == null ? NOT_PROCESSED : (int) instance.getValue();
    }

    public static boolean isChampion(LivingEntity entity) {
        int type = championType(entity);
        return type >= 0 && type < ChampionModifier.MODS.size();
    }

    public static void markNotChampion(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(TCAttributes.CHAMPION_MOD);
        if (instance != null) {
            instance.removeModifier(ChampionModifier.NONE_MARKER_ID);
            instance.addPermanentModifier(ChampionModifier.NONE_MARKER);
        }
    }

    public static void makeChampion(Monster entity, boolean persist) {
        int type = entity.getRandom().nextInt(ChampionModifier.MODS.size());
        if (entity instanceof Creeper) {
            type = ChampionModifier.BOLD;
        }
        makeChampion(entity, persist, type);
    }

    public static void makeChampion(Monster entity, boolean persist, int type) {
        AttributeInstance instance = entity.getAttribute(TCAttributes.CHAMPION_MOD);
        if (instance == null || instance.getValue() > NOT_PROCESSED) {
            return;
        }
        ChampionModifier mod = ChampionModifier.MODS.get(type);
        instance.removeModifier(mod.attributeMod().id());
        instance.addPermanentModifier(mod.attributeMod());
        applyPermanent(entity, Attributes.MAX_HEALTH, CHAMPION_HEALTH);
        applyPermanent(entity, Attributes.ATTACK_DAMAGE, CHAMPION_DAMAGE);
        entity.heal(ASSIGN_HEAL);
        entity.setCustomName(Component.translatable("champion.thaumaturge.name", Component.translatable("champion.mod." + mod.name()), entity.getName()));
        if (persist) {
            entity.setPersistenceRequired();
        }
        switch (type) {
            case ChampionModifier.BOLD -> applyPermanent(entity, Attributes.MOVEMENT_SPEED, BOLD_SPEED);
            case ChampionModifier.MIGHTY -> applyPermanent(entity, Attributes.ATTACK_DAMAGE, MIGHTY_DAMAGE);
            case ChampionModifier.WARDED -> {
                int ward = (int) entity.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
                AttributeInstance maxAbsorption = entity.getAttribute(Attributes.MAX_ABSORPTION);
                if (maxAbsorption != null && maxAbsorption.getBaseValue() < ward) {
                    maxAbsorption.setBaseValue(ward);
                }
                entity.setAbsorptionAmount(entity.getAbsorptionAmount() + ward);
            }
            default -> {
            }
        }
    }

    public static void makeTainted(LivingEntity target) {
        AttributeInstance instance = target.getAttribute(TCAttributes.CHAMPION_MOD);
        if (instance == null || instance.getValue() > NOT_CHAMPION) {
            return;
        }
        if ((int) instance.getValue() == NOT_CHAMPION) {
            instance.addPermanentModifier(ChampionModifier.MINUS_ONE);
        }
        ChampionModifier mod = ChampionModifier.MODS.get(ChampionModifier.TAINTED);
        instance.removeModifier(mod.attributeMod().id());
        instance.addPermanentModifier(mod.attributeMod());
        applyPermanent(target, Attributes.MAX_HEALTH, TAINTED_HEALTH);
        AttributeInstance attack = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            if (attack.getBaseValue() <= 0.0) {
                attack.setBaseValue(Math.max(2.0F, (target.getBbHeight() + target.getBbWidth()) * 2.0F));
            } else {
                attack.removeModifier(TAINTED_DAMAGE_ID);
                attack.addPermanentModifier(TAINTED_DAMAGE);
            }
        }
        target.heal(ASSIGN_HEAL);
    }

    static void resetTaintedAI(LivingEntity entity) {
        if (!(entity instanceof PathfinderMob critter) || critter instanceof Monster) {
            return;
        }
        AttributeInstance marker = critter.getAttribute(TCAttributes.TAINTED_MOD);
        if (marker == null || marker.getValue() != 0.0) {
            return;
        }
        MobAccessor accessor = (MobAccessor) critter;
        accessor.thaumaturge$getGoalSelector().removeAllGoals(goal -> true);
        accessor.thaumaturge$getTargetSelector().removeAllGoals(goal -> true);
        accessor.thaumaturge$getGoalSelector().addGoal(0, new FloatGoal(critter));
        accessor.thaumaturge$getGoalSelector().addGoal(2, new MeleeAttackGoal(critter, 1.2, false));
        accessor.thaumaturge$getGoalSelector().addGoal(5, new MoveTowardsRestrictionGoal(critter, 1.0));
        accessor.thaumaturge$getGoalSelector().addGoal(7, new RandomStrollGoal(critter, 1.0));
        accessor.thaumaturge$getGoalSelector().addGoal(8, new LookAtPlayerGoal(critter, Player.class, 8.0F));
        accessor.thaumaturge$getGoalSelector().addGoal(8, new RandomLookAroundGoal(critter));
        accessor.thaumaturge$getTargetSelector().addGoal(1, new HurtByTargetGoal(critter));
        accessor.thaumaturge$getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(critter, Player.class, true));
        marker.removeModifier(TAINTED_AI_MARKER_ID);
        marker.addPermanentModifier(TAINTED_AI_MARKER);
    }

    public static void resetTaintedAIMarker(LivingEntity entity) {
        AttributeInstance marker = entity.getAttribute(TCAttributes.TAINTED_MOD);
        if (marker != null) {
            marker.removeModifier(TAINTED_AI_MARKER_ID);
        }
    }

    private static void applyPermanent(LivingEntity entity, Holder<Attribute> attribute, AttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifier.id());
            instance.addPermanentModifier(modifier);
        }
    }
}
