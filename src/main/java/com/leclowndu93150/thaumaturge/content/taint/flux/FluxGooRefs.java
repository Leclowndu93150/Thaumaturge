package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.registry.TTFluids;
import net.minecraft.world.level.material.FlowingFluid;

public final class FluxGooRefs {
    private FluxGooRefs() {}

    public static FlowingFluid sourceFluid() {
        return TTFluids.FLUX_GOO_SOURCE.get();
    }
}
