package com.leclowndu93150.thaumaturge.api.spell.behavior;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * A kind of node behaviour: the role it plays and the codec of its JSON configuration.
 *
 * <p>Register instances in the {@link #REGISTRY_KEY} registry. A {@code spell_part} JSON selects a
 * type through its {@code behavior.type} field; the remaining fields of {@code behavior} decode
 * through {@link #codec()}.
 *
 * @param kind  the role every part of this type plays
 * @param codec the configuration codec
 * @param <B>   the behaviour class
 * @since 1.0.0
 */
public record SpellBehaviorType<B extends SpellBehavior>(SpellPartKind kind, MapCodec<B> codec) {
    /** The registry key of behaviour types. */
    public static final ResourceKey<Registry<SpellBehaviorType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("spell_behavior"));
}
