package com.leclowndu93150.thaumaturge.api.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static facade producing constructed {@link DamageSource} instances for the Thaumaturge damage
 * types. Behavior (armor bypass, magic flag, etc.) follows the tag wiring under
 * {@code data/thaumaturge/tags/damage_type/} which is consumed by vanilla at attack resolution
 * time.
 *
 * <p>All factories are safe to call on either side; they read the damage-type registry off the
 * level's registry access and never touch chunk state.
 *
 * @since 1.0.0
 */
public final class TTDamageSources {
    private TTDamageSources() {}

    /**
     * Produces a {@link DamageSource} for raw taint damage. Bypasses armor and shields; ignored
     * by witches.
     */
    public static DamageSource taint(Level level) {
        return source(level, TTDamageTypes.TAINT);
    }

    /**
     * Produces a {@link DamageSource} for tentacle damage with an optional attributed attacker.
     */
    public static DamageSource tentacle(Level level, @Nullable Entity attacker) {
        return new DamageSource(holder(level, TTDamageTypes.TENTACLE), attacker);
    }

    /**
     * Produces a {@link DamageSource} for swarm damage with an optional attributed attacker.
     */
    public static DamageSource swarm(Level level, @Nullable Entity attacker) {
        return new DamageSource(holder(level, TTDamageTypes.SWARM), attacker);
    }

    /**
     * Produces a {@link DamageSource} for dissolve damage. Bypasses armor.
     */
    public static DamageSource dissolve(Level level) {
        return source(level, TTDamageTypes.DISSOLVE);
    }

    /**
     * Produces a {@link DamageSource} for the fire focus effect: fire-typed indirect projectile
     * damage attributed to the caster.
     */
    public static DamageSource focusFire(Level level, @Nullable Entity direct, @Nullable Entity caster) {
        return new DamageSource(holder(level, TTDamageTypes.FOCUS_FIRE), direct, caster);
    }

    private static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(holder(level, key));
    }

    private static Holder<DamageType> holder(Level level, ResourceKey<DamageType> key) {
        return level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
    }
}
