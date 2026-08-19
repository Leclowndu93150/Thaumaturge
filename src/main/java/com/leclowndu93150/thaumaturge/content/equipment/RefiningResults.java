package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class RefiningResults {
    private record Entry(TagKey<Block> ore, Item cluster) {
    }

    private static final List<Entry> ENTRIES = List.of(new Entry(Tags.Blocks.ORES_IRON, TCItems.CLUSTER_IRON.get()), new Entry(Tags.Blocks.ORES_GOLD, TCItems.CLUSTER_GOLD.get()),
            new Entry(Tags.Blocks.ORES_COPPER, TCItems.CLUSTER_COPPER.get()), new Entry(Tags.Blocks.ORES_QUARTZ, TCItems.CLUSTER_QUARTZ.get()),
            new Entry(TCBlockTags.ORES_CINNABAR, TCItems.CLUSTER_CINNABAR.get()));

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
