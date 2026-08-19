package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCCapabilities {
    private TCCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        crucible(event);
        devices(event);
        essentiaTransport(event);
        essentiaItems(event);
        smeltery(event);
        golemBuilder(event);
        infernalFurnace(event);
        researchTable(event);
        spa(event);
        workbench(event);
    }

    private static void crucible(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TCBlockEntities.CRUCIBLE.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, TCBlockEntities.CRUCIBLE.get(), (be, side) -> be.getTank());
    }

    private static void devices(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.LAMP_GROWTH.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.LAMP_FERTILITY.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.CENTRIFUGE.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.POTION_SPRAYER.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.HUNGRY_CHEST.get(), (be, side) -> VanillaContainerWrapper.of(be));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, TCBlockEntities.EVERFULL_URN.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, TCBlockEntities.VIS_GENERATOR.get(), (be, side) -> side == be.outputFace() ? be : null);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.ESSENTIA_PORT.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.CONDENSER.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.VOID_SIPHON.get(), (be, side) -> be.output());
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.THAUMATORIUM.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.THAUMATORIUM_TOP.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.THAUMATORIUM.get(), (be, side) -> be.catalyst());
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TCBlockEntities.MIRROR_ESSENTIA.get(), (be, side) -> be);
    }

    private static void essentiaTransport(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_VALVE.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_RESTRICT.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_FILTER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_ONEWAY.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_BUFFER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.ASPECT_QUERY, TCBlockEntities.TUBE_FILTER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.JAR.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.JAR_VOID.get(), (be, side) -> be);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TCBlockEntities.JAR.get(), (be, side) -> be);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TCBlockEntities.JAR_VOID.get(), (be, side) -> be);
    }

    private static void essentiaItems(RegisterCapabilitiesEvent event) {
        event.registerItem(EssentiaCapabilities.CONTAINER, (stack, ctx) -> (IEssentiaContainerItem) stack.getItem(), TCItems.PHIAL.get(), TCItems.ESSENTIA_CRYSTAL.get(), TCItems.MANA_BEAN.get(),
                TCItems.JAR_NORMAL.get(), TCItems.JAR_VOID.get());
    }

    private static void smeltery(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.SMELTER.get(), (be, side) -> {
            if (side == null) {
                return be.getInventory();
            }
            if (side == Direction.UP || be.getBlockState().getValue(BlockSmelter.FACING).getAxis().equals(side.getAxis())) {
                return RangedResourceHandler.ofSingleIndex(be.getInventory(), 0);
            }
            return RangedResourceHandler.ofSingleIndex(be.getInventory(), 1);
        });
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.ALEMBIC.get(), (be, side) -> be);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TCBlockEntities.ALEMBIC.get(), (be, side) -> be);
    }

    private static void golemBuilder(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.GOLEM_BUILDER.get(), (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.GOLEM_BUILDER.get(), (be, side) -> be.output());
    }

    private static void infernalFurnace(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.INFERNAL_FURNACE.get(), (be, side) -> side == null || side == Direction.UP ? be.inventory() : null);
    }

    private static void researchTable(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.RESEARCH_TABLE.get(), (be, side) -> be.items());
    }

    private static void spa(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, TCBlockEntities.SPA.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.SPA.get(), (be, side) -> side == Direction.UP ? null : be.getItems());
    }

    private static void workbench(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, TCBlockEntities.ARCANE_WORKBENCH.get(), (be, side) -> VanillaContainerWrapper.of(be.getInventory()));
    }
}
