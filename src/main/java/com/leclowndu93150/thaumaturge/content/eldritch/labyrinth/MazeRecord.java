package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MazeRecord(MazePlan plan, MazeState state) {
    public static final Codec<MazeRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    MazePlan.CODEC.fieldOf("plan").forGetter(MazeRecord::plan),
                    MazeState.CODEC.fieldOf("state").forGetter(MazeRecord::state))
            .apply(instance, MazeRecord::new));
}
