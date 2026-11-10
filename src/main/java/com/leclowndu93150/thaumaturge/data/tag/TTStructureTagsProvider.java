package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TTStructureBootstrap;
import com.leclowndu93150.thaumaturge.registry.TTStructureTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class TTStructureTagsProvider extends StructureTagsProvider {
    public TTStructureTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TTIds.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(TTStructureTags.ON_CULT_MAPS).add(TTStructureBootstrap.ELDRITCH_OBELISK);
    }
}
