package com.leclowndu93150.thaumaturge.content.pech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;

public record PechTradeTable(List<PechTrade> trades) {
    public static final ResourceKey<Registry<PechTradeTable>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "pech_trade"));

    public static final Codec<PechTradeTable> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(PechTrade.CODEC.listOf().fieldOf("trades").forGetter(PechTradeTable::trades)).apply(instance, PechTradeTable::new));

    public record PechTrade(int tier, ItemStackTemplate result) {
        public static final Codec<PechTrade> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codec.intRange(1, 5).fieldOf("tier").forGetter(PechTrade::tier), ItemStackTemplate.CODEC.fieldOf("result").forGetter(PechTrade::result)).apply(instance, PechTrade::new));
    }
}
