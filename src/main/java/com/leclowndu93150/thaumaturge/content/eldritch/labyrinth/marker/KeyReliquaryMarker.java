package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryRole;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public record KeyReliquaryMarker(Direction facing) implements LabyrinthMarker {
    public static final MapCodec<KeyReliquaryMarker> CODEC = RoomSocket.HORIZONTAL
            .optionalFieldOf("facing", Direction.NORTH)
            .xmap(KeyReliquaryMarker::new, KeyReliquaryMarker::facing);

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.KEY_RELIQUARY.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.STAMP;
    }

    @Override
    public Optional<ResourceLocation> landmark() {
        return Optional.of(LabyrinthLandmarks.KEY);
    }

    @Override
    public void stamp(MarkerStampContext context, BlockPos pos) {
        ReliquaryPlacement.stamp(context, pos, facing, ReliquaryRole.KEY_ROOM, Optional.empty());
    }
}
