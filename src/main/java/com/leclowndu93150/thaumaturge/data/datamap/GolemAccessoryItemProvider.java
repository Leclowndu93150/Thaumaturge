package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryItem;
import com.leclowndu93150.thaumaturge.registry.TCGolemAccessories;
import com.leclowndu93150.thaumaturge.registry.TCItems;
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
        add(builder, TCItems.GOLEM_TOP_HAT, TCGolemAccessories.TOP_HAT);
        add(builder, TCItems.GOLEM_FEZ, TCGolemAccessories.FEZ);
        add(builder, TCItems.GOLEM_GLASSES, TCGolemAccessories.GLASSES);
        add(builder, TCItems.GOLEM_BOWTIE, TCGolemAccessories.BOWTIE);
        add(builder, TCItems.GOLEM_VISOR, TCGolemAccessories.VISOR);
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
