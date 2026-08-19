package com.leclowndu93150.thaumaturge.registry;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public class TCBlockFamilies {

    private static final Map<Block, BlockFamily> MAP = Maps.newHashMap();

    public static final BlockFamily ARCANE_STONE = familyBuilder(TCBlocks.STONE_ARCANE.get())
            // .wall(Blocks.ANDESITE_WALL)
            .stairs(TCBlocks.STAIRS_ARCANE.get())
            // .slab(TCBlocks.SLAB_ARCANE.get())
            .polished(TCBlocks.STONE_ARCANE_BRICK.get()).generateStonecutterRecipe().getFamily();
    public static final BlockFamily ARCANE_STONE_BRICKS = familyBuilder(TCBlocks.STONE_ARCANE_BRICK.get()).stairs(TCBlocks.STAIRS_ARCANE_BRICK.get())
            // .slab(Blocks.POLISHED_ANDESITE_SLAB)
            .generateStonecutterRecipe().getFamily();

    private static BlockFamily.Builder familyBuilder(Block base) {
        BlockFamily.Builder builder = new BlockFamily.Builder(base);
        BlockFamily blockFamily = MAP.put(base, builder.getFamily());
        if (blockFamily != null) {
            throw new IllegalStateException("Duplicate family definition for " + BuiltInRegistries.BLOCK.getKey(base));
        } else {
            return builder;
        }
    }

    public static Stream<BlockFamily> getAllFamilies() {
        return MAP.values().stream();
    }

    public static @Nullable BlockFamily getFamily(Block base) {
        return MAP.get(base);
    }
}
