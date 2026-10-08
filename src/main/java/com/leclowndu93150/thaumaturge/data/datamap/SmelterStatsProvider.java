package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.content.essentia.smeltery.SmelterDataMaps;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.SmelterStats;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class SmelterStatsProvider extends DataMapProvider {
    private static final SmelterStats BASIC = SmelterStats.DEFAULT;
    private static final SmelterStats THAUMIUM = new SmelterStats(10, 0.9F);
    private static final SmelterStats VOID = new SmelterStats(15, 0.95F);

    public SmelterStatsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public String getName() {
        return "Smelter Stats Data Maps";
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(SmelterDataMaps.SMELTER_STATS)
                .add(TTBlocks.SMELTER_BASIC, BASIC, false)
                .add(TTBlocks.SMELTER_THAUMIUM, THAUMIUM, false)
                .add(TTBlocks.SMELTER_VOID, VOID, false);
    }
}
