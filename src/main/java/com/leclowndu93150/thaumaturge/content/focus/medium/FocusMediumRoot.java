package com.leclowndu93150.thaumaturge.content.focus.medium;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusMedium;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class FocusMediumRoot implements FocusMedium {
    public static final ResourceLocation KEY = TTIds.rl("root");

    @Override
    public ResourceLocation id() {
        return KEY;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return 0;
    }

    @Override
    public Set<SupplyType> requires() {
        return SUPPLIES_NOTHING;
    }

    @Override
    public Set<SupplyType> supplies() {
        return SUPPLIES_BOTH;
    }

    @Override
    public CastStreams cast(CastContext ctx, FocusSettings settings, CastStreams incoming) {
        return incoming;
    }
}
