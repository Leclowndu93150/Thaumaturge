package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionDataMaps;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class ChampionWhitelistProvider extends DataMapProvider {
    public ChampionWhitelistProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public String getName() {
        return "Champion Whitelist Data Maps";
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<Integer, EntityType<?>> b = builder(ChampionDataMaps.CHAMPION_WHITELIST);
        add(b, EntityType.ZOMBIE, 0);
        add(b, EntityType.HUSK, 0);
        add(b, EntityType.DROWNED, 0);
        add(b, EntityType.ZOMBIE_VILLAGER, 0);
        add(b, EntityType.ZOMBIFIED_PIGLIN, 0);
        add(b, EntityType.SPIDER, 0);
        add(b, EntityType.CAVE_SPIDER, 0);
        add(b, EntityType.BLAZE, 0);
        add(b, EntityType.ENDERMAN, 0);
        add(b, EntityType.SKELETON, 0);
        add(b, EntityType.WITCH, 1);
        add(b, TCEntities.BRAINY_ZOMBIE.get(), 0);
        add(b, TCEntities.BRAINY_DROWNED.get(), 0);
        add(b, TCEntities.BRAINY_HUSK.get(), 0);
        add(b, TCEntities.GIANT_BRAINY_ZOMBIE.get(), 0);
        add(b, TCEntities.ELDRITCH_CRAB.get(), 0);
        add(b, TCEntities.TAINTACLE.get(), 2);
        add(b, TCEntities.TAINTACLE_SMALL.get(), 2);
        add(b, TCEntities.INHABITED_ZOMBIE.get(), 3);
    }

    private void add(Builder<Integer, EntityType<?>> b, EntityType<?> type, int weight) {
        b.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type), weight, false);
    }
}
