package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The part an entity plays in an encounter.
 *
 * <ul>
 * <li>{@link #PRIMARY}: must die for the encounter to be beaten, and counts toward the shared boss bar.</li>
 * <li>{@link #MINION}: supporting entity; it is bound to the maze but does not block victory.</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum EncounterRole implements StringRepresentable {
    PRIMARY("primary"),
    MINION("minion");

    /**
     * Codec that reads and writes the role by its lowercase serialized name.
     */
    public static final Codec<EncounterRole> CODEC = StringRepresentable.fromEnum(EncounterRole::values);

    private final String name;

    EncounterRole(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
