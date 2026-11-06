package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectDefinition;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class CrystalTrimMaterialProvider extends DataMapProvider {
    public CrystalTrimMaterialProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        HolderGetter<TrimMaterial> materials = provider.lookupOrThrow(Registries.TRIM_MATERIAL);
        Builder<Holder<TrimMaterial>, IAspect> builder = builder(TTTrimMaterials.CRYSTAL_MATERIAL);
        for (AspectDefinition aspect : AspectBootstrap.DEFINITIONS) {
            builder.add(aspect.key(), materials.getOrThrow(TTTrimMaterials.essentiaCrystal(aspect.key())), false);
        }
    }

    @Override
    public String getName() {
        return "Crystal Trim Material Data Map";
    }
}
