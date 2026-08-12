package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCFluidTypes;
import com.leclowndu93150.thaumaturge.registry.TCFluids;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCClientFluidExtensions {
    private static final int PURIFYING_TINT = 0x77FFEEAA;

    private static final int LIQUID_DEATH_TINT = 0xFF4D0066;

    private TCClientFluidExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new FluxGooClientExtensions(), TCFluidTypes.FLUX_GOO.get());
    }

    @SubscribeEvent
    public static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        Material gooTexture = new Material(TCIds.rl("block/flux_goo"));
        event.register(
                new FluidModel.Unbaked(gooTexture, gooTexture, null, null),
                TCFluids.FLUX_GOO_SOURCE.get(),
                TCFluids.FLUX_GOO_FLOWING.get());
        event.register(
                new FluidModel.Unbaked(
                        new Material(Identifier.withDefaultNamespace("block/water_still")),
                        new Material(Identifier.withDefaultNamespace("block/water_flow")),
                        new Material(Identifier.withDefaultNamespace("block/water_overlay")),
                        BlockTintSources.constant(PURIFYING_TINT)),
                TCFluids.PURIFYING_SOURCE.get(),
                TCFluids.PURIFYING_FLOWING.get());
        Material deathTexture = new Material(TCIds.rl("block/animatedglow"));
        event.register(
                new FluidModel.Unbaked(deathTexture, deathTexture, null, BlockTintSources.constant(LIQUID_DEATH_TINT)),
                TCFluids.LIQUID_DEATH_SOURCE.get(),
                TCFluids.LIQUID_DEATH_FLOWING.get());
    }
}
