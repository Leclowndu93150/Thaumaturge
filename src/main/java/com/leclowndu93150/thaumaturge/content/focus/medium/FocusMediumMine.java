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
import com.leclowndu93150.thaumaturge.content.entity.EntityFocusMine;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class FocusMediumMine implements FocusMedium {
    private static final Identifier KEY = TCIds.rl("mine");

    private static final int COMPLEXITY = 4;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_mine"), Optional.empty(), false);
    }

    @Override
    public int complexity(FocusSettings settings) {
        return COMPLEXITY;
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.VINCULUM;
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
            boolean friendly = settings.value("target") == 1;
            for (Trajectory trajectory : supplied) {
                caster.level().addFreshEntity(new EntityFocusMine(remaining, caster, trajectory, friendly));
            }
        }
        return null;
    }

    @Override
    public List<SettingDefinition> settings() {
        int[] friend = new int[]{0, 1};
        String[] friendDesc = new String[]{"focus.common.enemy", "focus.common.friend"};
        return List.of(new SettingDefinition("target", "focus.common.target", new SettingDefinition.IntList(friend, friendDesc)));
    }
}
