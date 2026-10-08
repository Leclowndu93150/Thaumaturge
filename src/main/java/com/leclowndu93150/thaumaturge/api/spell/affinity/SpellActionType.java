package com.leclowndu93150.thaumaturge.api.spell.affinity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * A kind of affinity action and the codec of its JSON configuration. Register instances in
 * {@link #REGISTRY_KEY}.
 *
 * @param codec the configuration codec
 * @param <A>   the action class
 * @since 1.0.0
 */
public record SpellActionType<A extends SpellAction>(MapCodec<A> codec) {
    /** The registry key of action types. */
    public static final ResourceKey<Registry<SpellActionType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("spell_action"));
}
