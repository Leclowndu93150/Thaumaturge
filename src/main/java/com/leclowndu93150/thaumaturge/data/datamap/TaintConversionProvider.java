package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.content.taint.entity.TaintConversion;
import com.leclowndu93150.thaumaturge.registry.TCDataMaps;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class TaintConversionProvider extends DataMapProvider {
    public TaintConversionProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public String getName() {
        return "Taint Conversion Data Maps";
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<TaintConversion, EntityType<?>> b = builder(TCDataMaps.TAINT_CONVERSION);
        add(b, EntityType.CREEPER, TCEntities.TAINT_CREEPER.get(), false);
        add(b, EntityType.COW, TCEntities.TAINT_COW.get(), true);
        add(b, EntityType.PIG, TCEntities.TAINT_PIG.get(), true);
        add(b, EntityType.CHICKEN, TCEntities.TAINT_CHICKEN.get(), true);
        add(b, EntityType.SHEEP, TCEntities.TAINT_SHEEP.get(), true);
        add(b, EntityType.VILLAGER, TCEntities.TAINT_VILLAGER.get(), true);
    }

    private static void add(
            Builder<TaintConversion, EntityType<?>> builder,
            EntityType<?> from,
            EntityType<?> into,
            boolean naturalSpawns) {
        builder.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(from), new TaintConversion(into, naturalSpawns), false);
    }
}
