package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public final class FuelValuesProvider extends DataMapProvider {
    public FuelValuesProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<FurnaceFuel, Item> b = builder(NeoForgeDataMaps.FURNACE_FUELS);

        b.add(TTItems.ALUMENTUM, new FurnaceFuel(4800), false);
    }

    @Override
    public String getName() {
        return "TC Fuel Values Data Map";
    }
}
