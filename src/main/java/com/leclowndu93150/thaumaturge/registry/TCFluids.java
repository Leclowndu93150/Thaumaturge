package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.alchemy.LiquidDeathFluid;
import com.leclowndu93150.thaumaturge.content.spa.PurifyingFluid;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TCIds.MODID);

    public static final DeferredHolder<Fluid, FluxGooFluid.Source> FLUX_GOO_SOURCE =
            FLUIDS.register("flux_goo", () -> new FluxGooFluid.Source(props()));

    public static final DeferredHolder<Fluid, FluxGooFluid.Flowing> FLUX_GOO_FLOWING =
            FLUIDS.register("flowing_flux_goo", () -> new FluxGooFluid.Flowing(props()));

    public static final DeferredHolder<Fluid, PurifyingFluid.Source> PURIFYING_SOURCE =
            FLUIDS.register("purifying", () -> new PurifyingFluid.Source(purifyingProps()));

    public static final DeferredHolder<Fluid, PurifyingFluid.Flowing> PURIFYING_FLOWING =
            FLUIDS.register("flowing_purifying", () -> new PurifyingFluid.Flowing(purifyingProps()));

    public static final DeferredHolder<Fluid, LiquidDeathFluid.Source> LIQUID_DEATH_SOURCE =
            FLUIDS.register("liquid_death", () -> new LiquidDeathFluid.Source(liquidDeathProps()));

    public static final DeferredHolder<Fluid, LiquidDeathFluid.Flowing> LIQUID_DEATH_FLOWING =
            FLUIDS.register("flowing_liquid_death", () -> new LiquidDeathFluid.Flowing(liquidDeathProps()));

    private static BaseFlowingFluid.Properties liquidDeathProps() {
        return new BaseFlowingFluid.Properties(TCFluidTypes.LIQUID_DEATH, LIQUID_DEATH_SOURCE, LIQUID_DEATH_FLOWING)
                .block(() -> (LiquidBlock) TCBlocks.LIQUID_DEATH.get())
                .bucket(TCItems.BUCKET_LIQUID_DEATH)
                .levelDecreasePerBlock(2)
                .tickRate(15);
    }

    private static BaseFlowingFluid.Properties purifyingProps() {
        return new BaseFlowingFluid.Properties(TCFluidTypes.PURIFYING, PURIFYING_SOURCE, PURIFYING_FLOWING)
                .block(() -> (LiquidBlock) TCBlocks.PURIFYING_FLUID.get());
    }

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(TCFluidTypes.FLUX_GOO, FLUX_GOO_SOURCE, FLUX_GOO_FLOWING)
                .block(() -> (LiquidBlock) TCBlocks.FLUX_GOO.get())
                .tickRate(30)
                .levelDecreasePerBlock(2)
                .slopeFindDistance(2)
                .explosionResistance(100.0F);
    }

    private TCFluids() {}

    public static void register(IEventBus modBus) {
        FLUIDS.register(modBus);
    }
}
