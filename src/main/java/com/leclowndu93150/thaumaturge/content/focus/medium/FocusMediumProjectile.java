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
import com.leclowndu93150.thaumaturge.content.entity.EntityFocusProjectile;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class FocusMediumProjectile implements FocusMedium {
    private static final Identifier KEY = TCIds.rl("projectile");

    private static final int BASE_COMPLEXITY = 4;
    private static final int BOUNCY_COMPLEXITY = 3;
    private static final int SEEKING_COMPLEXITY = 5;
    private static final float SPEED_DIVISOR = 3.0F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_projectile"), Optional.of(1), false);
    }

    @Override
    public int complexity(FocusSettings settings) {
        int complexity = BASE_COMPLEXITY + (settings.value("speed") - 1) / 2;
        switch (settings.value("option")) {
            case EntityFocusProjectile.SPECIAL_BOUNCY -> complexity += BOUNCY_COMPLEXITY;
            case EntityFocusProjectile.SPECIAL_SEEK_ENEMY, EntityFocusProjectile.SPECIAL_SEEK_FRIENDLY -> complexity += SEEKING_COMPLEXITY;
        }
        return complexity;
    }

    @Override
    public Set<SupplyType> supplies() {
        return SUPPLIES_BOTH;
    }

    @Override
    public @Nullable CastStreams cast(CastContext ctx, FocusSettings settings, CastStreams incoming) {
        Trajectory[] supplied = incoming.trajectories();
        FocusPackage remaining = ctx.continuation();
        LivingEntity caster = ctx.caster();
        if (supplied != null && remaining != null && caster != null) {
            float speed = settings.value("speed") / SPEED_DIVISOR;
            int option = settings.value("option");
            for (Trajectory trajectory : supplied) {
                caster.level().addFreshEntity(new EntityFocusProjectile(remaining, caster, speed, trajectory, option));
            }
        }
        return null;
    }

    @Override
    public List<SettingDefinition> settings() {
        int[] option = new int[]{EntityFocusProjectile.SPECIAL_NONE, EntityFocusProjectile.SPECIAL_BOUNCY, EntityFocusProjectile.SPECIAL_SEEK_ENEMY, EntityFocusProjectile.SPECIAL_SEEK_FRIENDLY};
        String[] optionDesc = new String[]{"focus.common.none", "focus.projectile.bouncy", "focus.projectile.seeking.hostile", "focus.projectile.seeking.friendly"};
        return List.of(new SettingDefinition("option", "focus.common.options", new SettingDefinition.IntList(option, optionDesc)),
                new SettingDefinition("speed", "focus.projectile.speed", new SettingDefinition.IntRange(1, 5)));
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.MOTUS;
    }
}
