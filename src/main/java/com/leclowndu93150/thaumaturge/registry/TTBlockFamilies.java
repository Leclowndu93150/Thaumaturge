package com.leclowndu93150.thaumaturge.registry;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public class TTBlockFamilies {
    private static final Map<Block, BlockFamily> MAP = Maps.newHashMap();

    public static final BlockFamily ARCANE_STONE = familyBuilder(TTBlocks.STONE_ARCANE.get())
            .stairs(TTBlocks.STAIRS_ARCANE.get())
            .slab(TTBlocks.SLAB_ARCANE_STONE.get())
            .wall(TTBlocks.WALL_ARCANE_STONE.get())
            .polished(TTBlocks.STONE_ARCANE_BRICK.get())
            .getFamily();
    public static final BlockFamily ARCANE_STONE_BRICKS = familyBuilder(TTBlocks.STONE_ARCANE_BRICK.get())
            .stairs(TTBlocks.STAIRS_ARCANE_BRICK.get())
            .slab(TTBlocks.SLAB_ARCANE_BRICK.get())
            .wall(TTBlocks.WALL_ARCANE_BRICK.get())
            .getFamily();
    public static final BlockFamily ANCIENT_STONE = familyBuilder(TTBlocks.STONE_ANCIENT.get())
            .stairs(TTBlocks.STAIRS_ANCIENT.get())
            .slab(TTBlocks.SLAB_ANCIENT.get())
            .wall(TTBlocks.WALL_ANCIENT.get())
            .getFamily();
    public static final BlockFamily ANCIENT_STONE_TILE = familyBuilder(TTBlocks.STONE_ANCIENT_TILE.get())
            .stairs(TTBlocks.STAIRS_ANCIENT_TILE.get())
            .slab(TTBlocks.SLAB_ANCIENT_TILE.get())
            .wall(TTBlocks.WALL_ANCIENT_TILE.get())
            .getFamily();
    public static final BlockFamily ANCIENT_ROCK = familyBuilder(TTBlocks.STONE_ANCIENT_ROCK.get())
            .stairs(TTBlocks.STAIRS_ANCIENT_ROCK.get())
            .slab(TTBlocks.SLAB_ANCIENT_ROCK.get())
            .wall(TTBlocks.WALL_ANCIENT_ROCK.get())
            .getFamily();
    public static final BlockFamily ELDRITCH_STONE = familyBuilder(TTBlocks.ELDRITCH_STONE.get())
            .stairs(TTBlocks.STAIRS_ELDRITCH.get())
            .slab(TTBlocks.SLAB_ELDRITCH_STONE.get())
            .wall(TTBlocks.WALL_ELDRITCH_STONE.get())
            .getFamily();
    public static final BlockFamily ELDRITCH_STONE_TILE = familyBuilder(TTBlocks.STONE_ELDRITCH_TILE.get())
            .stairs(TTBlocks.STAIRS_ELDRITCH_TILE.get())
            .slab(TTBlocks.SLAB_ELDRITCH.get())
            .wall(TTBlocks.WALL_ELDRITCH_TILE.get())
            .getFamily();
    public static final BlockFamily ELDRITCH_ROCK = familyBuilder(TTBlocks.ELDRITCH_ROCK.get())
            .stairs(TTBlocks.STAIRS_ELDRITCH_ROCK.get())
            .slab(TTBlocks.SLAB_ELDRITCH_ROCK.get())
            .wall(TTBlocks.WALL_ELDRITCH_ROCK.get())
            .getFamily();

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
