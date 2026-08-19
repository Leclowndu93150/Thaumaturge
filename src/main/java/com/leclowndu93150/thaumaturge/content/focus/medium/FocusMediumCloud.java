package com.leclowndu93150.thaumaturge.content.focus.medium;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusMedium;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.entity.EntityFocusCloud;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class FocusMediumCloud implements FocusMedium {
    private static final Identifier KEY = TCIds.rl("cloud");

    private static final int BASE_COMPLEXITY = 4;
    private static final int RADIUS_COMPLEXITY_FACTOR = 2;
    private static final int DURATION_COMPLEXITY_DIVISOR = 5;
    private static final float POWER_MULTIPLIER = 0.5F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_cloud"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.ALKIMIA;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return BASE_COMPLEXITY + settings.value("radius") * RADIUS_COMPLEXITY_FACTOR + settings.value("duration") / DURATION_COMPLEXITY_DIVISOR;
    }

    @Override
    public @Nullable CastStreams cast(CastContext ctx, FocusSettings settings, CastStreams incoming) {
        Trajectory[] supplied = incoming.trajectories();
        FocusPackage remaining = ctx.continuation();
        LivingEntity caster = ctx.caster();
        if (supplied != null && remaining != null && caster != null) {
            int radius = settings.value("radius");
            int duration = settings.value("duration");
            for (Trajectory trajectory : supplied) {
                caster.level().addFreshEntity(new EntityFocusCloud(remaining, caster, trajectory, radius, duration));
            }
        }
        return null;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("radius", "focus.common.radius", new SettingDefinition.IntRange(1, 3)),
                new SettingDefinition("duration", "focus.common.duration", new SettingDefinition.IntRange(5, 30)));
    }

    @Override
    public float powerMultiplier(FocusSettings settings) {
        return POWER_MULTIPLIER;
    }
}
