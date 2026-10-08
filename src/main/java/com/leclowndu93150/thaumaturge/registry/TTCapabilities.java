package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.AdvancedAlchemicalFurnaceStructure;
import com.leclowndu93150.thaumaturge.content.essentia.item.ComponentEssentia;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumaturge.content.essentia.storage.SingleAspectItemStorage;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTCapabilities {
    private TTCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        crucible(event);
        nodes(event);
        devices(event);
        essentiaTransport(event);
        essentiaItems(event);
        smeltery(event);
        golemBuilder(event);
        infernalFurnace(event);
        researchTable(event);
        spa(event);
        workbench(event);
        infusion(event);
    }

    private static void infusion(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AspectCapabilities.CONTAINER, TTBlockEntities.INFUSION_MATRIX.get(), (be, side) -> be);
    }

    private static void crucible(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.CRUCIBLE.get(), (be, side) -> be);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK, TTBlockEntities.CRUCIBLE.get(), (be, side) -> be.getTank());
    }

    private static void nodes(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.NODE.get(), (be, side) -> be);
        event.registerBlockEntity(
                VisRelayCapabilities.SOURCE, TTBlockEntities.NODE.get(), (be, context) -> be.relaySource());
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.JAR_NODE.get(), (be, side) -> be);
    }

    private static void devices(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TTBlockEntities.LAMP_GROWTH.get(), (be, side) -> be.intake());
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TTBlockEntities.LAMP_FERTILITY.get(), (be, side) -> be.intake());
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.CENTRIFUGE.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.ESSENTIA_CRYSTALIZER.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.ESSENTIA_RESERVOIR.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.FLUX_SCRUBBER.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TTBlockEntities.POTION_SPRAYER.get(), (be, side) -> be);
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

    private static void essentiaTransport(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_VALVE.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_RESTRICT.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_FILTER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_ONEWAY.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_BUFFER.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.ASPECT_QUERY, TTBlockEntities.TUBE_FILTER.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.JAR.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.JAR_VOID.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.JAR.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.STORAGE,
                TTBlockEntities.JAR.get(),
                (be, side) -> side != null && be.isConnectable(side) ? be.storage(side) : null);
        event.registerBlockEntity(
                EssentiaCapabilities.STORAGE,
                TTBlockEntities.JAR_VOID.get(),
                (be, side) -> side != null && be.isConnectable(side) ? be.storage(side) : null);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.JAR_VOID.get(), (be, side) -> be);
    }

    private static void essentiaItems(RegisterCapabilitiesEvent event) {
        event.registerItem(
                EssentiaCapabilities.CONTAINER, (stack, ctx) -> ComponentEssentia.phial(stack), TTItems.PHIAL.get());
        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> ComponentEssentia.crystal(stack),
                TTItems.ESSENTIA_CRYSTAL.get(),
                TTItems.MANA_BEAN.get());
        event.registerItem(
                EssentiaCapabilities.CONTAINER,
                (stack, ctx) -> ComponentEssentia.jar(stack),
                TTItems.JAR_NORMAL.get(),
                TTItems.JAR_VOID.get());
        event.registerItem(
                EssentiaCapabilities.ITEM_STORAGE,
                (stack, ctx) -> new SingleAspectItemStorage(stack, ComponentEssentia::jar, jarCapacity(stack), false),
                TTItems.JAR_NORMAL.get(),
                TTItems.JAR_VOID.get());
        event.registerItem(
                EssentiaCapabilities.ITEM_STORAGE,
                (stack, ctx) ->
                        new SingleAspectItemStorage(stack, ComponentEssentia::phial, PhialItem.BASE_AMOUNT, true),
                TTItems.PHIAL.get());
    }

    private static int jarCapacity(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof IEssentiaJar jar
                ? jar.jarCapacity()
                : IEssentiaJar.DEFAULT_CAPACITY;
    }

    private static void smeltery(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TTBlockEntities.SMELTER.get(), (be, side) -> {
            if (side == null) {
                return be.getInventory();
            }
            if (side == Direction.UP
                    || be.getBlockState()
                            .getValue(BlockSmelter.FACING)
                            .getAxis()
                            .equals(side.getAxis())) {
                return new RangedWrapper(be.getInventory(), 0, 1);
            }
            return new RangedWrapper(be.getInventory(), 1, 2);
        });
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.ALEMBIC.get(), (be, side) -> be);
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.ALEMBIC.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.STORAGE,
                TTBlockEntities.ALEMBIC.get(),
                (be, side) -> side != null && be.isConnectable(side) ? be.storage(side) : null);
    }

    private static void golemBuilder(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.GOLEM_BUILDER.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlock(
                EssentiaCapabilities.TRANSPORT,
                (level, pos, state, be, side) -> AdvancedAlchemicalFurnaceStructure.nozzle(level, pos, side),
                TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get());
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, TTBlockEntities.GOLEM_BUILDER.get(), (be, side) -> be.output());
    }

    private static void infernalFurnace(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TTBlockEntities.INFERNAL_FURNACE.get(),
                (be, side) -> side == null || side == Direction.UP ? be.inventory() : null);
    }

    private static void researchTable(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, TTBlockEntities.RESEARCH_TABLE.get(), (be, side) -> be.items());
    }

    private static void spa(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK, TTBlockEntities.SPA.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TTBlockEntities.SPA.get(),
                (be, side) -> side == Direction.UP ? null : be.getItems());
    }

    private static void workbench(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TTBlockEntities.ARCANE_WORKBENCH.get(),
                (be, side) -> new InvWrapper(be.getInventory()));
    }
}
