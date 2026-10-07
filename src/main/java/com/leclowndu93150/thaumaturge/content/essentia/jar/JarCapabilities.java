package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.content.essentia.item.SingleAspectItemStorage;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class JarCapabilities {
    private JarCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.JAR.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.JAR_VOID.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.STORAGE,
                TTBlockEntities.JAR.get(),
                (be, side) -> side != null && be.isConnectable(side) ? be.storage(side) : null);
        event.registerBlockEntity(
                EssentiaCapabilities.STORAGE,
                TTBlockEntities.JAR_VOID.get(),
                (be, side) -> side != null && be.isConnectable(side) ? be.storage(side) : null);
        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(),
                TTItems.PHIAL.get());
        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(),
                TTItems.ESSENTIA_CRYSTAL.get());
        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(),
                TTItems.MANA_BEAN.get());

        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(),
                TTItems.JAR_NORMAL.get(),
                TTItems.JAR_VOID.get());

        event.registerItem(
                EssentiaCapabilities.ITEM_STORAGE,
                (stack, ctx) -> new SingleAspectItemStorage(
                        stack, (IEssentiaContainerItem) stack.getItem(), BlockEntityJar.CAPACITY),
                TTItems.JAR_NORMAL.get(),
                TTItems.JAR_VOID.get());
        event.registerItem(
                EssentiaCapabilities.ITEM_STORAGE,
                (stack, ctx) -> new SingleAspectItemStorage(
                        stack, (IEssentiaContainerItem) stack.getItem(), PhialItem.BASE_AMOUNT),
                TTItems.PHIAL.get());

        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.JAR.get(), (be, side) -> be);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.JAR_VOID.get(), (be, side) -> be);
    }
}
