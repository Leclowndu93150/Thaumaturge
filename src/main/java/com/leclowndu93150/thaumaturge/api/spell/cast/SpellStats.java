package com.leclowndu93150.thaumaturge.api.spell.cast;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/**
 * The stats shipped with Thaumaturge.
 *
 * @since 1.0.0
 */
public final class SpellStats {
    /** Multiplier on every magnitude: damage, healing, potency. */
    public static final SpellStat POWER = stat("power", 1.0F);
    /** Blocks added to every radius; above zero, single-target effects also hit around the target. */
    public static final SpellStat RADIUS = stat("radius", 0.0F);
    /** Multiplier on every duration. */
    public static final SpellStat DURATION = stat("duration", 1.0F);
    /** Multiplier on carrier speed. */
    public static final SpellStat SPEED = stat("speed", 1.0F);
    /** Extra entities a projectile, bolt or beam passes through. */
    public static final SpellStat PIERCE = stat("pierce", 0.0F);
    /** Times a projectile bounces off blocks. */
    public static final SpellStat BOUNCE = stat("bounce", 0.0F);
    /** Above zero, carriers steer toward targets. */
    public static final SpellStat HOMING = stat("homing", 0.0F);
    /** Fortune level for block breaking. */
    public static final SpellStat FORTUNE = stat("fortune", 0.0F);
    /** Above zero, block breaking uses silk touch. */
    public static final SpellStat SILK_TOUCH = stat("silk_touch", 0.0F);

    /** Every shipped stat, in declaration order. */
    public static final List<SpellStat> ALL = List.of(POWER, RADIUS, DURATION, SPEED, PIERCE, BOUNCE, HOMING, FORTUNE, SILK_TOUCH);

    private SpellStats() {}

    /**
     * Finds a shipped stat by id.
     *
     * @param id the stat id
     * @return the shipped stat, or empty for an id no shipped stat uses
     */
    public static Optional<SpellStat> byId(Identifier id) {
        for (SpellStat stat : ALL) {
            if (stat.id().equals(id)) {
                return Optional.of(stat);
            }
        }
        return Optional.empty();
    }

    private static SpellStat stat(String path, float defaultValue) {
        return new SpellStat(Identifier.fromNamespaceAndPath("thaumaturge", path), defaultValue);
    }
}
