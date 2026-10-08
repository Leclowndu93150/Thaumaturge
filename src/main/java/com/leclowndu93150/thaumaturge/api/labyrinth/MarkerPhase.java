package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * When a {@link LabyrinthMarker} takes effect.
 *
 * <ul>
 * <li>{@link #STAMP}: during chunk generation, on a worldgen thread, limited to the chunk being generated.</li>
 * <li>{@link #TRIGGER}: on the server thread, the first time a player comes within {@link LabyrinthMarker#triggerRadius()}.</li>
 * <li>{@link #LANDMARK}: when the maze is created; the position is recorded under {@link LabyrinthMarker#landmark()} and nothing is placed.</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum MarkerPhase implements StringRepresentable {
    STAMP("stamp"),
    TRIGGER("trigger"),
    LANDMARK("landmark");

    /**
     * Codec that reads and writes the phase by its lowercase serialized name.
     */
    public static final Codec<MarkerPhase> CODEC = StringRepresentable.fromEnum(MarkerPhase::values);

    private final String name;

    MarkerPhase(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
