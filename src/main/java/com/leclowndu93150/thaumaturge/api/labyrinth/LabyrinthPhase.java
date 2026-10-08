package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Progress of a labyrinth from creation to retirement.
 *
 * <ul>
 * <li>{@link #SEALED}: the boss door is closed and waits for a bound tablet.</li>
 * <li>{@link #CHARGING}: a tablet sits in the lock and the arena is being prepared.</li>
 * <li>{@link #ACTIVE}: the door is open and the encounter is running.</li>
 * <li>{@link #CONQUERED}: the encounter is beaten; rewards and the exit rift are available.</li>
 * <li>{@link #RETIRED}: the maze is no longer linked to any altar and is about to be dropped.</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum LabyrinthPhase implements StringRepresentable {
    SEALED("sealed"),
    CHARGING("charging"),
    ACTIVE("active"),
    CONQUERED("conquered"),
    RETIRED("retired");

    /**
     * Codec that reads and writes the phase by its lowercase serialized name.
     */
    public static final Codec<LabyrinthPhase> CODEC = StringRepresentable.fromEnum(LabyrinthPhase::values);

    private final String name;

    LabyrinthPhase(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * @return true while the lock is charging or the encounter is running, the span in which the boss hall is protected
     * @since 1.0.0
     */
    public boolean isContested() {
        return this == CHARGING || this == ACTIVE;
    }

    /**
     * @return true once the boss door has opened
     */
    public boolean isPastLock() {
        return this == ACTIVE || this == CONQUERED || this == RETIRED;
    }
}
