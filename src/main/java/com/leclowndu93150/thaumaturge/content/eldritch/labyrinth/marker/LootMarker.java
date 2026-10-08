package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;

public record LootMarker(SimpleWeightedRandomList<BlockState> containers, float chance) implements LabyrinthMarker {
    public static final MapCodec<LootMarker> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC)
                            .fieldOf("containers")
                            .forGetter(LootMarker::containers),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(LootMarker::chance))
            .apply(instance, LootMarker::new));

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.LOOT.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.STAMP;
    }

    @Override
    public void stamp(MarkerStampContext context, BlockPos pos) {
        if (context.random().nextFloat() < chance * context.lootScale()) {
            containers.getRandomValue(context.random()).ifPresent(state -> context.place(pos, state));
        }
    }
}
