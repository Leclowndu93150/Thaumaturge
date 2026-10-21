package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazePlan;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MazeStamp(int maze, int generation) {
    public static final MazeStamp NONE = new MazeStamp(-1, 0);
    public static final MapCodec<MazeStamp> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("maze").forGetter(MazeStamp::maze),
                    Codec.INT.optionalFieldOf("generation", 0).forGetter(MazeStamp::generation))
            .apply(instance, MazeStamp::new));

    public static MazeStamp of(MazePlan plan) {
        return new MazeStamp(plan.id().value(), plan.generation());
    }

    public boolean present() {
        return maze >= 0;
    }

    public boolean matches(MazePlan plan) {
        return maze == plan.id().value() && generation == plan.generation();
    }

    public boolean staleFor(MazePlan plan) {
        return maze == plan.id().value() && generation != plan.generation();
    }
}
