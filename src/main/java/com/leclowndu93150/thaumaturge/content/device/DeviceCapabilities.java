package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

@EventBusSubscriber(modid = TCIds.MODID)
public final class DeviceCapabilities {
    private DeviceCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.LAMP_GROWTH.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TCBlockEntities.LAMP_FERTILITY.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.CENTRIFUGE.get(), (be, side) -> be);
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                TCBlockEntities.HUNGRY_CHEST.get(),
                (be, side) -> VanillaContainerWrapper.of(be));
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK, TCBlockEntities.EVERFULL_URN.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                TCBlockEntities.VIS_GENERATOR.get(),
                (be, side) -> side == be.outputFace() ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TCBlockEntities.ESSENTIA_PORT.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.CONDENSER.get(), (be, side) -> be);
        event.registerBlockEntity(
                Capabilities.Item.BLOCK, TCBlockEntities.VOID_SIPHON.get(), (be, side) -> be.output());
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.THAUMATORIUM.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TCBlockEntities.THAUMATORIUM_TOP.get(), (be, side) -> be);
        event.registerBlockEntity(
                Capabilities.Item.BLOCK, TCBlockEntities.THAUMATORIUM.get(), (be, side) -> be.catalyst());
        event.registerBlockEntity(
                AspectCapabilities.CONTAINER, TCBlockEntities.MIRROR_ESSENTIA.get(), (be, side) -> be);
    }
}
