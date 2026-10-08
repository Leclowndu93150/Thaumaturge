package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.PortalLink;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record EntryPortalMarker() implements LabyrinthMarker {
    public static final EntryPortalMarker INSTANCE = new EntryPortalMarker();
    public static final MapCodec<EntryPortalMarker> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.ENTRY_PORTAL.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.STAMP;
    }

    @Override
    public Optional<ResourceLocation> landmark() {
        return Optional.of(LabyrinthLandmarks.ENTRY_PORTAL);
    }

    @Override
    public void stamp(MarkerStampContext context, BlockPos pos) {
        context.placeEntity(
                        pos, TTBlocks.ELDRITCH_PORTAL.get().defaultBlockState(), TTBlockEntities.ELDRITCH_PORTAL.get())
                .ifPresent(portal -> portal.setLink(PortalLink.toOrigin(context.maze())));
    }
}
