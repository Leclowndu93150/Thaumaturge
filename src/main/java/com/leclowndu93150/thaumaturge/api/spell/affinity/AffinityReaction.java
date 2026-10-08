package com.leclowndu93150.thaumaturge.api.spell.affinity;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Which half of an {@link AspectAffinity} an aspect-driven part uses.
 *
 * @since 1.0.0
 */
public enum AffinityReaction implements StringRepresentable {
    /** The entity and block reactions: what the aspect's energy does on impact. */
    STRIKE("strike"),
    /** The boon reaction: what the aspect grants when imbued into a creature. */
    IMBUE("imbue");

    /** Serializes by {@link #getSerializedName()}. */
    public static final Codec<AffinityReaction> CODEC = StringRepresentable.fromEnum(AffinityReaction::values);

    private final String name;

    AffinityReaction(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * Whether an affinity defines this reaction.
     *
     * @param affinity the affinity
     * @return true when the affinity has at least one action for this reaction
     */
    public boolean definedBy(AspectAffinity affinity) {
        return switch (this) {
            case STRIKE -> !affinity.entity().isEmpty() || !affinity.block().isEmpty();
            case IMBUE -> !affinity.imbue().isEmpty();
        };
    }
}
