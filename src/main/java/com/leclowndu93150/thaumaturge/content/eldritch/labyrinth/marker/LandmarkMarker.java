package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record LandmarkMarker(ResourceLocation id) implements LabyrinthMarker {
    public static final MapCodec<LandmarkMarker> CODEC =
            ResourceLocation.CODEC.fieldOf("id").xmap(LandmarkMarker::new, LandmarkMarker::id);

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.LANDMARK.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.LANDMARK;
    }

    @Override
    public Optional<ResourceLocation> landmark() {
        return Optional.of(id);
    }
}
