package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record InfusionEnchantments(Map<InfusionEnchantment, Integer> levels) {
    public static final InfusionEnchantments EMPTY = new InfusionEnchantments(Map.of());

    public static final Codec<InfusionEnchantments> CODEC = Codec.unboundedMap(InfusionEnchantment.CODEC, Codec.intRange(1, Integer.MAX_VALUE)).xmap(InfusionEnchantments::new,
            InfusionEnchantments::levels);

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionEnchantments> STREAM_CODEC = ByteBufCodecs
            .<RegistryFriendlyByteBuf, InfusionEnchantment, Integer, Map<InfusionEnchantment, Integer>>map(LinkedHashMap::new, InfusionEnchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT)
            .map(InfusionEnchantments::new, InfusionEnchantments::levels);

    public InfusionEnchantments {
        EnumMap<InfusionEnchantment, Integer> copy = new EnumMap<>(InfusionEnchantment.class);
        copy.putAll(levels);
        levels = copy;
    }

    public int level(InfusionEnchantment enchantment) {
        return levels.getOrDefault(enchantment, 0);
    }

    public boolean has(InfusionEnchantment enchantment) {
        return levels.containsKey(enchantment);
    }

    public boolean isEmpty() {
        return levels.isEmpty();
    }

    public InfusionEnchantments with(InfusionEnchantment enchantment, int level) {
        Map<InfusionEnchantment, Integer> next = new EnumMap<>(levels);
        next.put(enchantment, level);
        return new InfusionEnchantments(next);
    }
}
