package com.leclowndu93150.thaumaturge.api.spell.fx;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * How one kind of spell impact looks. Styles are registered in {@link #REGISTRY_KEY} and run on the
 * client when a spell effects payload arrives; implementations only add particles.
 *
 * @since 1.0.0
 */
public interface SpellFxStyle {
    /** The registry key of effect styles. */
    ResourceKey<Registry<SpellFxStyle>> REGISTRY_KEY = ResourceKey.createRegistryKey(TTIds.rl("spell_fx"));

    /**
     * Spawns one particle of this style. Called on the client thread, several times per event.
     *
     * @param level  the client level
     * @param at     the particle position
     * @param motion the particle motion
     * @param color  the packed {@code 0xRRGGBB} tint of the event
     * @param random the client random
     */
    void spawn(Level level, Vec3 at, Vec3 motion, int color, RandomSource random);
}
