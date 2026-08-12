package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class JarCapabilities {
    private JarCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.JAR.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.JAR_VOID.get(), (be, side) -> be);
        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(),
                TCItems.PHIAL.get());
        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(),
                TCItems.ESSENTIA_CRYSTAL.get());
        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(),
                TCItems.MANA_BEAN.get());

        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(),
                TCItems.JAR_NORMAL.get(),
                TCItems.JAR_VOID.get());

        event.registerBlockEntity(AspectCapabilities.CONTAINER, TCBlockEntities.JAR.get(), (be, side) -> be);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TCBlockEntities.JAR_VOID.get(), (be, side) -> be);
    }
}
