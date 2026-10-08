package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.StateOrientation;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;

public record BlockMarker(
        SimpleWeightedRandomList<BlockState> states,
        float chance,
        boolean scaled,
        Optional<Direction> facing,
        Optional<BlockState> otherwise)
        implements LabyrinthMarker {
    public static final MapCodec<BlockMarker> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC)
                            .fieldOf("states")
                            .forGetter(BlockMarker::states),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(BlockMarker::chance),
                    Codec.BOOL.optionalFieldOf("scaled", true).forGetter(BlockMarker::scaled),
                    Direction.CODEC.optionalFieldOf("facing").forGetter(BlockMarker::facing),
                    BlockState.CODEC.optionalFieldOf("otherwise").forGetter(BlockMarker::otherwise))
            .apply(instance, BlockMarker::new));

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.BLOCK.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.STAMP;
    }

    @Override
    public void stamp(MarkerStampContext context, BlockPos pos) {
        float roll = chance * (scaled ? context.decorationScale() : 1.0F);
        if (context.random().nextFloat() < roll) {
            states.getRandomValue(context.random())
                    .ifPresent(state -> context.place(
                            pos,
                            facing.map(local -> StateOrientation.face(state, context.orient(local)))
                                    .orElse(state)));
        } else {
            otherwise.ifPresent(state -> context.place(pos, state));
        }
    }
}
