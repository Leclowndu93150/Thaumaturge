package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitGoals;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import com.leclowndu93150.thaumaturge.mixin.world.entity.MobAccessor;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

public final class MobTraitEngine {
    private static final String MODIFIER_ROOT = "trait/";
    private static final String SEPARATOR = "/";

    private MobTraitEngine() {}

    public static List<Holder<MobTrait>> traits(LivingEntity mob) {
        MobTraitState state = mob.getExistingDataOrNull(TTAttachments.MOB_TRAITS);
        return state == null ? List.of() : state.traits();
    }

    public static boolean add(LivingEntity mob, Holder<MobTrait> trait) {
        if (mob.level().isClientSide()) {
            return false;
        }
        MobTraitState state = mob.getData(TTAttachments.MOB_TRAITS);
        if (state.contains(trait)) {
            return false;
        }
        mob.setData(TTAttachments.MOB_TRAITS, state.with(trait));
        refresh(mob);
        trait.value().onAdded(mob);
        return true;
    }

    public static boolean remove(LivingEntity mob, Holder<MobTrait> trait) {
        if (mob.level().isClientSide()) {
            return false;
        }
        MobTraitState state = mob.getExistingDataOrNull(TTAttachments.MOB_TRAITS);
        if (state == null || !state.contains(trait)) {
            return false;
        }
        mob.setData(TTAttachments.MOB_TRAITS, state.without(trait));
        refresh(mob);
        trait.value().onRemoved(mob);
        return true;
    }

    public static void refresh(LivingEntity mob) {
        refreshModifiers(mob);
        if (mob instanceof PathfinderMob pathfinder) {
            refreshGoals(pathfinder);
        }
    }

    public static boolean suppressesNativeAi(LivingEntity mob) {
        MobTraitRuntime runtime = mob.getExistingDataOrNull(TTAttachments.MOB_TRAIT_RUNTIME);
        return runtime != null && runtime.nativeAiSuppressed();
    }

    private static void refreshModifiers(LivingEntity mob) {
        Map<ResourceLocation, Expected> expected = new LinkedHashMap<>();
        for (Holder<MobTrait> trait : traits(mob)) {
            trait.value()
                    .modifiers(
                            mob,
                            new ModifierCollector(
                                    mob, trait.unwrapKey().orElseThrow().location(), expected));
        }
        BuiltInRegistries.ATTRIBUTE.holders().forEach(attribute -> {
            AttributeInstance instance = mob.getAttribute(attribute);
            if (instance != null) {
                for (AttributeModifier modifier : List.copyOf(instance.getModifiers())) {
                    if (isTraitModifier(modifier.id()) && !expected.containsKey(modifier.id())) {
                        instance.removeModifier(modifier.id());
                    }
                }
            }
        });
        for (Expected entry : expected.values()) {
            AttributeInstance instance = mob.getAttribute(entry.attribute());
            if (instance != null
                    && !entry.modifier()
                            .equals(instance.getModifier(entry.modifier().id()))) {
                instance.removeModifier(entry.modifier().id());
                instance.addPermanentModifier(entry.modifier());
            }
        }
    }

    private static boolean isTraitModifier(ResourceLocation id) {
        return id.getNamespace().equals(TTIds.MODID) && id.getPath().startsWith(MODIFIER_ROOT);
    }

    private static void refreshGoals(PathfinderMob mob) {
        MobAccessor accessor = (MobAccessor) mob;
        GoalSelector goals = accessor.thaumaturge$getGoalSelector();
        GoalSelector targets = accessor.thaumaturge$getTargetSelector();
        MobTraitRuntime runtime = mob.getData(TTAttachments.MOB_TRAIT_RUNTIME);
        for (TraitGoal injected : runtime.injected()) {
            (injected.target() ? targets : goals).removeGoal(injected.goal());
        }
        runtime.injected().clear();
        List<Holder<MobTrait>> traits = traits(mob);
        boolean suppress = false;
        for (Holder<MobTrait> trait : traits) {
            suppress |= trait.value().replacesNativeAi(mob);
        }
        if (suppress && !runtime.nativeAiSuppressed()) {
            stash(goals, false, runtime);
            stash(targets, true, runtime);
            stopBrain(mob);
            runtime.setNativeAiSuppressed(true);
        } else if (!suppress && runtime.nativeAiSuppressed()) {
            for (TraitGoal stashed : runtime.stashed()) {
                (stashed.target() ? targets : goals).addGoal(stashed.priority(), stashed.goal());
            }
            runtime.stashed().clear();
            runtime.setNativeAiSuppressed(false);
        }
        GoalSink sink = new GoalSink(goals, targets, runtime);
        for (Holder<MobTrait> trait : traits) {
            trait.value().goals(mob, sink);
        }
    }

    private static void stash(GoalSelector selector, boolean target, MobTraitRuntime runtime) {
        for (WrappedGoal wrapped : List.copyOf(selector.getAvailableGoals())) {
            runtime.stashed().add(new TraitGoal(target, wrapped.getPriority(), wrapped.getGoal()));
        }
        selector.removeAllGoals(goal -> true);
    }

    @SuppressWarnings("unchecked")
    private static void stopBrain(LivingEntity mob) {
        if (mob.level() instanceof ServerLevel level) {
            ((Brain<LivingEntity>) mob.getBrain()).stopAll(level, mob);
        }
    }

    private record Expected(Holder<Attribute> attribute, AttributeModifier modifier) {}

    private static final class ModifierCollector implements MobTraitModifiers {
        private final LivingEntity mob;
        private final ResourceLocation trait;
        private final Map<ResourceLocation, Expected> expected;
        private int index;

        private ModifierCollector(LivingEntity mob, ResourceLocation trait, Map<ResourceLocation, Expected> expected) {
            this.mob = mob;
            this.trait = trait;
            this.expected = expected;
        }

        @Override
        public void add(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
            ResourceLocation id =
                    TTIds.rl(MODIFIER_ROOT + trait.getNamespace() + SEPARATOR + trait.getPath() + SEPARATOR + index++);
            expected.put(id, new Expected(attribute, new AttributeModifier(id, amount, operation)));
        }

        @Override
        public void setBase(Holder<Attribute> attribute, double value) {
            AttributeInstance instance = mob.getAttribute(attribute);
            if (instance != null) {
                add(attribute, value - instance.getBaseValue(), AttributeModifier.Operation.ADD_VALUE);
            }
        }
    }

    private static final class GoalSink implements MobTraitGoals {
        private final GoalSelector goals;
        private final GoalSelector targets;
        private final MobTraitRuntime runtime;

        private GoalSink(GoalSelector goals, GoalSelector targets, MobTraitRuntime runtime) {
            this.goals = goals;
            this.targets = targets;
            this.runtime = runtime;
        }

        @Override
        public void add(int priority, Goal goal) {
            goals.addGoal(priority, goal);
            runtime.injected().add(new TraitGoal(false, priority, goal));
        }

        @Override
        public void addTarget(int priority, Goal goal) {
            targets.addGoal(priority, goal);
            runtime.injected().add(new TraitGoal(true, priority, goal));
        }
    }
}
