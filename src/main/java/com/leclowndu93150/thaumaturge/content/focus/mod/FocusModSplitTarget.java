package com.leclowndu93150.thaumaturge.content.focus.mod;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.FocusSplit;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class FocusModSplitTarget implements FocusSplit {
    private static final ResourceLocation KEY = TTIds.rl("split_target");

    private static final int COMPLEXITY = 4;

    @Override
    public ResourceLocation id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TTIds.rl("focus_split"), Optional.empty(), false);
    }

    @Override
    public int complexity(FocusSettings settings) {
        return COMPLEXITY;
    }

    @Override
    public Set<SupplyType> requires() {
        return SUPPLIES_TARGETS;
    }

    @Override
    public Set<SupplyType> supplies() {
        return SUPPLIES_TARGETS;
    }

    @Override
    public CastStreams branchStreams(CastContext ctx, FocusSettings settings, CastStreams incoming) {
        return new CastStreams(null, incoming.targets());
    }
}
