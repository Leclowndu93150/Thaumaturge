package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class RefiningResults {
    private record Entry(TagKey<Block> ore, Item cluster) {
    }

    private static final List<Entry> ENTRIES = List.of(new Entry(Tags.Blocks.ORES_IRON, TTItems.CLUSTER_IRON.get()), new Entry(Tags.Blocks.ORES_GOLD, TTItems.CLUSTER_GOLD.get()),
            new Entry(Tags.Blocks.ORES_COPPER, TTItems.CLUSTER_COPPER.get()), new Entry(Tags.Blocks.ORES_QUARTZ, TTItems.CLUSTER_QUARTZ.get()),
            new Entry(TTBlockTags.ORES_CINNABAR, TTItems.CLUSTER_CINNABAR.get()));

    private RefiningResults() {}

    public static Item clusterFor(BlockState state) {
        for (Entry entry : ENTRIES) {
            if (state.is(entry.ore())) {
                return entry.cluster();
            }
        }
        return null;
    }
}
