package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

@EventBusSubscriber(modid = TCIds.MODID)
public class SmelterCapabilities {

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.SMELTER.get(), (be, side) -> {
            if (side == null) return be.getInventory();
            else if (side == Direction.UP
                    || be.getBlockState()
                            .getValue(BlockSmelter.FACING)
                            .getAxis()
                            .equals(side.getAxis())) return new RangedWrapper(be.getInventory(), 0, 1);
            return new RangedWrapper(be.getInventory(), 1, 2);
        });

        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.ALEMBIC.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.STORAGE,
                TCBlockEntities.ALEMBIC.get(),
                (be, side) -> side != null && be.isConnectable(side) ? be.storage(side) : null);

        event.registerBlockEntity(AspectCapabilities.CONTAINER, TCBlockEntities.ALEMBIC.get(), (be, side) -> be);
    }
}
