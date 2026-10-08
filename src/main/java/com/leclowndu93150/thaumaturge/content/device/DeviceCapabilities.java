package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@EventBusSubscriber(modid = TTIds.MODID)
public final class DeviceCapabilities {
    private DeviceCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TTBlockEntities.LAMP_GROWTH.get(), (be, side) -> be.intake());
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TTBlockEntities.LAMP_FERTILITY.get(), (be, side) -> be.intake());
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.CENTRIFUGE.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.ESSENTIA_RESERVOIR.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.ESSENTIA_CRYSTALIZER.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.FLUX_SCRUBBER.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, TTBlockEntities.HUNGRY_CHEST.get(), (be, side) -> new InvWrapper(be));
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TTBlockEntities.ITEM_GRATE.get(),
                (be, side) -> side == Direction.UP ? be.inventory() : null);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK, TTBlockEntities.EVERFULL_URN.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                TTBlockEntities.VIS_GENERATOR.get(),
                (be, side) -> side == be.outputFace() ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.ESSENTIA_PORT.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.CONDENSER.get(), (be, side) -> be);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, TTBlockEntities.VOID_SIPHON.get(), (be, side) -> be.output());
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.THAUMATORIUM.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TTBlockEntities.THAUMATORIUM_TOP.get(), (be, side) -> be);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, TTBlockEntities.THAUMATORIUM.get(), (be, side) -> be.catalyst());
        event.registerBlockEntity(
                AspectCapabilities.CONTAINER, TTBlockEntities.MIRROR_ESSENTIA.get(), (be, side) -> be);
    }
}
