package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;

public final class TTLabyrinthRoomTagsProvider extends TagsProvider<RoomType> {
    public TTLabyrinthRoomTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, RoomType.REGISTRY_KEY, lookupProvider, TTIds.MODID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        for (RoomRecipe recipe : RoomCatalogData.recipes()) {
            for (TagKey<RoomType> key : recipe.tags()) {
                tag(key).add(recipe.key());
            }
        }
    }
}
