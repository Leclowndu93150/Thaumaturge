package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.content.infusion.InfusionDataMaps;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionModifier;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class InfusionModifierProvider extends DataMapProvider {
    private static final InfusionModifier ANCIENT_PILLARS = new InfusionModifier(-1, -0.1F, -0.1F);
    private static final InfusionModifier ELDRITCH_PILLARS = new InfusionModifier(-3, 0.05F, 0.2F);
    private static final InfusionModifier SPEED_UPGRADE = new InfusionModifier(-1, 0.01F, 0.0F);
    private static final InfusionModifier COST_UPGRADE = new InfusionModifier(1, -0.02F, 0.0F);
    private static final InfusionModifier ELDRITCH_PEDESTAL = new InfusionModifier(0, 0.0025F, 0.0F);
    private static final InfusionModifier ANCIENT_PEDESTAL = new InfusionModifier(0, -0.01F, 0.0F);

    public InfusionModifierProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public String getName() {
        return "Infusion Modifier Data Maps";
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(InfusionDataMaps.PILLAR_SET)
                .add(TTBlocks.PILLAR_ANCIENT, ANCIENT_PILLARS, false)
                .add(TTBlocks.PILLAR_ELDRITCH, ELDRITCH_PILLARS, false);
        builder(InfusionDataMaps.MATRIX_UPGRADE)
                .add(TTBlocks.MATRIX_SPEED, SPEED_UPGRADE, false)
                .add(TTBlocks.MATRIX_COST, COST_UPGRADE, false);
        builder(InfusionDataMaps.PEDESTAL)
                .add(TTBlocks.PEDESTAL_ELDRITCH, ELDRITCH_PEDESTAL, false)
                .add(TTBlocks.PEDESTAL_ANCIENT, ANCIENT_PEDESTAL, false);
    }
}
