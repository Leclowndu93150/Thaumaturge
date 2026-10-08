package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record PendingTrigger(int index, BlockPos pos, int radius, int transform, LabyrinthMarker marker) {
    public static final Codec<PendingTrigger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("index").forGetter(PendingTrigger::index),
                    BlockPos.CODEC.fieldOf("pos").forGetter(PendingTrigger::pos),
                    Codec.INT.fieldOf("radius").forGetter(PendingTrigger::radius),
                    Codec.INT.fieldOf("transform").forGetter(PendingTrigger::transform),
                    LabyrinthMarker.CODEC.fieldOf("marker").forGetter(PendingTrigger::marker))
            .apply(instance, PendingTrigger::new));
}
