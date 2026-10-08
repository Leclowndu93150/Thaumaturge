package com.leclowndu93150.thaumaturge.api.damagesource;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

/**
 * Public addon API for Thaumaturge damage types. Constants are {@link ResourceKey}s into the
 * datapack damage type registry and can be resolved at runtime through a
 * {@link RegistryAccess RegistryAccess} obtained from the level.
 *
 * <p>Each damage type's behavior (armor bypass, witch resistance, magic flag, etc.) is
 * established by tag membership rather than by a {@link DamageSource
 * DamageSource} subclass; see {@link TTDamageSources} for the construction facade.
 *
 * @since 1.0.0
 */
public final class TTDamageTypes {
    public static final ResourceKey<DamageType> TAINT = key("taint");
    public static final ResourceKey<DamageType> TENTACLE = key("tentacle");
    public static final ResourceKey<DamageType> SWARM = key("swarm");
    public static final ResourceKey<DamageType> DISSOLVE = key("dissolve");
    public static final ResourceKey<DamageType> FOCUS_FIRE = key("focus_fire");

    public static final ResourceKey<DamageType> ELDRITCH_SPELL = key("eldritch_spell");

    private TTDamageTypes() {}

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, TTIds.rl(path));
    }
}
