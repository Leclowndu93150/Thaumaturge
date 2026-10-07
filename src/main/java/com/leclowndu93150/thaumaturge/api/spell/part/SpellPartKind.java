package com.leclowndu93150.thaumaturge.api.spell.part;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The role a part plays in a spell tree. A part's kind comes from its behaviour type.
 *
 * @since 1.0.0
 */
public enum SpellPartKind implements StringRepresentable {
    /** Carries the spell to its targets: touch, bolt, projectile, cloud. */
    DELIVERY("delivery"),
    /** Does something to the targets, then hands them on: burst, break, heal. */
    EFFECT("effect"),
    /** Changes how the following nodes behave: amplify, widen, homing. */
    MODIFIER("modifier"),
    /** Shapes the tree itself: branch, delay, relay, sieve. */
    FLOW("flow");

    /** Serializes by {@link #getSerializedName()}. */
    public static final Codec<SpellPartKind> CODEC = StringRepresentable.fromEnum(SpellPartKind::values);

    private final String name;

    SpellPartKind(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * The translation key of this kind's display name.
     *
     * @return {@code spell.thaumaturge.kind.<name>}
     */
    public String nameKey() {
        return "spell.thaumaturge.kind." + name;
    }
}
