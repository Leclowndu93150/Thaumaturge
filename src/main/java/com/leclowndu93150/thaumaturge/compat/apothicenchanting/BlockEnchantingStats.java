package com.leclowndu93150.thaumaturge.compat.apothicenchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public record BlockEnchantingStats(HolderSet<Block> blocks, EnchantingStats stats) {
    public static final Codec<BlockEnchantingStats> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(BlockEnchantingStats::blocks),
                    EnchantingStats.CODEC.fieldOf("stats").forGetter(BlockEnchantingStats::stats)).apply(instance, BlockEnchantingStats::new));
}
