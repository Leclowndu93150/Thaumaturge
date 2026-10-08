package com.leclowndu93150.thaumaturge.api.spell.fx;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * The visual-effects sink of one cast. Calls are batched and sent to nearby players as one payload
 * when the cast step finishes; anything over the per-payload cap is dropped.
 *
 * @since 1.0.0
 */
public interface SpellFx {
    /**
     * A burst of particles where the spell struck.
     *
     * @param style the {@link SpellFxStyle} id; unknown ids fall back to a coloured sparkle
     * @param at    the position
     * @param color the packed {@code 0xRRGGBB} tint
     */
    void impact(ResourceLocation style, Vec3 at, int color);

    /**
     * A directed spray of particles, such as the flash leaving a caster's hand.
     *
     * @param style     the {@link SpellFxStyle} id
     * @param at        the position
     * @param direction the spray direction
     * @param color     the packed {@code 0xRRGGBB} tint
     */
    void burst(ResourceLocation style, Vec3 at, Vec3 direction, int color);

    /**
     * A crackling arc between two points.
     *
     * @param from  the start
     * @param to    the end
     * @param color the packed {@code 0xRRGGBB} tint
     * @param width the arc width
     */
    void arc(Vec3 from, Vec3 to, int color, float width);

    /**
     * A straight beam between two points.
     *
     * @param from  the start
     * @param to    the end
     * @param color the packed {@code 0xRRGGBB} tint
     * @param ticks how long the beam lingers
     */
    void beam(Vec3 from, Vec3 to, int color, int ticks);
}
