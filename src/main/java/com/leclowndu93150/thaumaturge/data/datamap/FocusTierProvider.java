package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class FocusTierProvider extends DataMapProvider {
    private static final FocusTier LESSER = new FocusTier(20, 6, 0, 1);
    private static final FocusTier ADVANCED = new FocusTier(40, 10, 2, 2);
    private static final FocusTier GREATER = new FocusTier(75, 14, 4, 4);

    public FocusTierProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<FocusTier, Item> builder = builder(FocusTier.DATA_MAP);
        builder.add(TTItems.FOCUS_1, LESSER, false);
        builder.add(TTItems.FOCUS_2, ADVANCED, false);
        builder.add(TTItems.FOCUS_3, GREATER, false);
    }

    @Override
    public String getName() {
        return "Focus Tier Data Map";
    }
}
