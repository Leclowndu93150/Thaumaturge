package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class OreScanColors {
    public static final int DEFAULT = 12632256;

    private static final int C_IRON = 14200723;
    private static final int C_COAL = 1052688;
    private static final int C_REDSTONE = 16711680;
    private static final int C_GOLD = 16576075;
    private static final int C_LAPIS = 1328572;
    private static final int C_DIAMOND = 6155509;
    private static final int C_EMERALD = 1564002;
    private static final int C_QUARTZ = 15064789;
    private static final int C_COPPER = 16620629;
    private static final int C_AMBER = 16626469;
    private static final int C_CINNABAR = 10159368;

    private record Entry(TagKey<Block> ore, int color) {
    }

    private static final List<Entry> ENTRIES = List.of(new Entry(Tags.Blocks.ORES_IRON, C_IRON), new Entry(Tags.Blocks.ORES_COAL, C_COAL), new Entry(Tags.Blocks.ORES_REDSTONE, C_REDSTONE),
            new Entry(Tags.Blocks.ORES_GOLD, C_GOLD), new Entry(Tags.Blocks.ORES_LAPIS, C_LAPIS), new Entry(Tags.Blocks.ORES_DIAMOND, C_DIAMOND), new Entry(Tags.Blocks.ORES_EMERALD, C_EMERALD),
            new Entry(Tags.Blocks.ORES_QUARTZ, C_QUARTZ), new Entry(Tags.Blocks.ORES_COPPER, C_COPPER), new Entry(TCBlockTags.ORES_AMBER, C_AMBER), new Entry(TCBlockTags.ORES_CINNABAR, C_CINNABAR));

    private OreScanColors() {}

    public static int of(BlockState state) {
        for (Entry entry : ENTRIES) {
            if (state.is(entry.ore())) {
                return entry.color();
            }
        }
        return DEFAULT;
    }
}
