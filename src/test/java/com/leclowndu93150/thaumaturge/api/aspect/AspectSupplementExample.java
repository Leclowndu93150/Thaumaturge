package com.leclowndu93150.thaumaturge.api.aspect;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;

final class AspectSupplementExample extends DataMapProvider {
    private final Item augmentedItem;
    private final Item emptyItem;

    AspectSupplementExample(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, Item augmentedItem, Item emptyItem) {
        super(output, lookup);
        this.augmentedItem = augmentedItem;
        this.emptyItem = emptyItem;
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(AspectDataMaps.ASPECT_SUPPLEMENTS).add(augmentedItem.builtInRegistryHolder(), new AspectSupplement(Map.of(TTAspects.PRAECANTATIO.identifier(), 3)), false);
        builder(AspectDataMaps.BASE_ASPECTS).add(emptyItem.builtInRegistryHolder(), AspectList.EMPTY, false);
    }

    @Override
    public String getName() {
        return "Aspect supplement example";
    }
}
