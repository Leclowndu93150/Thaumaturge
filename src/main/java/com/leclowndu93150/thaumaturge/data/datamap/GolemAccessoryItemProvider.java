package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryItem;
import com.leclowndu93150.thaumaturge.registry.TTGolemAccessories;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.DeferredItem;

public final class GolemAccessoryItemProvider extends DataMapProvider {
    public GolemAccessoryItemProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<GolemAccessoryItem, Item> builder = builder(GolemAccessoryItem.DATA_MAP);
        add(builder, TTItems.GOLEM_TOP_HAT, TTGolemAccessories.TOP_HAT);
        add(builder, TTItems.GOLEM_FEZ, TTGolemAccessories.FEZ);
        add(builder, TTItems.GOLEM_GLASSES, TTGolemAccessories.GLASSES);
        add(builder, TTItems.GOLEM_BOWTIE, TTGolemAccessories.BOWTIE);
        add(builder, TTItems.GOLEM_VISOR, TTGolemAccessories.VISOR);
    }

    @Override
    public String getName() {
        return "Golem Accessory Item Data Map";
    }

    private static void add(
            Builder<GolemAccessoryItem, Item> builder, DeferredItem<Item> item, GolemAccessory accessory) {
        builder.add(item, new GolemAccessoryItem(accessory.id()), false);
    }
}
