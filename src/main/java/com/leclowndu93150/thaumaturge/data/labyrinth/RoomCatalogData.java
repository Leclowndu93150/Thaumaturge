package com.leclowndu93150.thaumaturge.data.labyrinth;

import java.util.ArrayList;
import java.util.List;

final class RoomCatalogData {
    private static final List<RoomRecipe> RECIPES = build();

    private RoomCatalogData() {}

    static List<RoomRecipe> recipes() {
        return RECIPES;
    }

    private static List<RoomRecipe> build() {
        List<RoomRecipe> recipes = new ArrayList<>();
        recipes.addAll(CorridorRooms.all());
        recipes.addAll(PassageRooms.all());
        recipes.addAll(HubRooms.all());
        recipes.addAll(FeatureRooms.all());
        recipes.addAll(SanctumRooms.all());
        recipes.addAll(HallRooms.all());
        return List.copyOf(recipes);
    }
}
