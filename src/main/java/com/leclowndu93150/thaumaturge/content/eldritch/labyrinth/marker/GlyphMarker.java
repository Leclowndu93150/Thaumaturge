package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.StateOrientation;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public record GlyphMarker(BlockState state, Direction facing) implements LabyrinthMarker {
    public static final MapCodec<GlyphMarker> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BlockState.CODEC.fieldOf("state").forGetter(GlyphMarker::state),
                    Direction.CODEC.fieldOf("facing").forGetter(GlyphMarker::facing))
            .apply(instance, GlyphMarker::new));

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.GLYPH.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.STAMP;
    }

    @Override
    public boolean wayfinding() {
        return true;
    }

    @Override
    public void stamp(MarkerStampContext context, BlockPos pos) {
        Direction face = context.orient(facing);
        context.place(
                pos, StateOrientation.exposeFaces(StateOrientation.face(state, face), StateOrientation.faceBit(face)));
    }
}
