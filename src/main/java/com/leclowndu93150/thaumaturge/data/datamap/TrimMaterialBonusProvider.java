package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.api.items.trim.TrimMaterialBonus;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class TrimMaterialBonusProvider extends DataMapProvider {
    private static final TrimMaterialBonus THAUMIUM = new TrimMaterialBonus(1, 0);
    private static final TrimMaterialBonus VOID_METAL = new TrimMaterialBonus(2, 1);

    public TrimMaterialBonusProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<TrimMaterialBonus, TrimMaterial> builder = builder(TTDataMaps.TRIM_MATERIAL_BONUS);
        builder.add(TTTrimMaterials.THAUMIUM, THAUMIUM, false);
        builder.add(TTTrimMaterials.VOID_METAL, VOID_METAL, false);
    }

    @Override
    public String getName() {
        return "Trim Material Bonus Data Map";
    }
}
