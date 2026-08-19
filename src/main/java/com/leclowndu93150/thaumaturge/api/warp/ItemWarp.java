package com.leclowndu93150.thaumaturge.api.warp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data-map value attached to items under {@code thaumaturge:warp}. Crafting an
 * item carrying this value inflicts that much {@link WarpType#NORMAL} warp on
 * the crafter, and holding or wearing it contributes to gear warp.
 *
 * @param amount the warp inflicted; must be positive
 * @since 1.0
 */
public record ItemWarp(int amount) {
    public static final Codec<ItemWarp> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(Codec.intRange(1, 500).fieldOf("amount").forGetter(ItemWarp::amount)).apply(instance, ItemWarp::new));
}
