package com.leclowndu93150.thaumaturge.compat.apothicenchanting.data;

import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.apothicenchanting.BlockEnchantingStats;
import com.leclowndu93150.thaumaturge.compat.apothicenchanting.EnchantingStats;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class EnchantingStatsProvider implements DataProvider {
    private static final String REGISTRY_PATH = "apothic_enchanting/enchanting_stats";

    private final PackOutput.PathProvider path;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final List<Entry> entries = new ArrayList<>();
    private HolderGetter<Block> blocks;

    public EnchantingStatsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.path = output.createPathProvider(PackOutput.Target.DATA_PACK, REGISTRY_PATH);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registries.thenCompose(lookup -> {
            this.blocks = lookup.lookupOrThrow(Registries.BLOCK);
            this.build();
            RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
            CompletableFuture<?>[] writes = this.entries.stream()
                    .map(entry -> DataProvider.saveStable(
                            output,
                            BlockEnchantingStats.CODEC
                                    .encodeStart(ops, entry.stats())
                                    .getOrThrow(),
                            this.path.json(TTIds.rl(entry.name()))))
                    .toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(writes);
        });
    }

    private void build() {
        add(
                "arcane_stone",
                new EnchantingStats(40, 1, 0, 0, 0),
                TTBlocks.STONE_ARCANE,
                TTBlocks.STONE_ARCANE_BRICK,
                TTBlocks.SLAB_ARCANE_STONE,
                TTBlocks.SLAB_ARCANE_BRICK,
                TTBlocks.STAIRS_ARCANE,
                TTBlocks.STAIRS_ARCANE_BRICK,
                TTBlocks.PILLAR_ARCANE,
                TTBlocks.PEDESTAL_ARCANE);
        add(
                "silverwood",
                new EnchantingStats(45, 2, 0, 3, 0),
                TTBlocks.PLANK_SILVERWOOD,
                TTBlocks.SLAB_SILVERWOOD,
                TTBlocks.STAIRS_SILVERWOOD,
                TTBlocks.LOG_SILVERWOOD,
                TTBlocks.WOOD_SILVERWOOD,
                TTBlocks.STRIPPED_LOG_SILVERWOOD,
                TTBlocks.STRIPPED_WOOD_SILVERWOOD);
        add(
                "elemental_crystals",
                new EnchantingStats(50, 3, 3, 0, 0),
                TTBlocks.CRYSTAL_AER,
                TTBlocks.CRYSTAL_IGNIS,
                TTBlocks.CRYSTAL_AQUA,
                TTBlocks.CRYSTAL_TERRA);
        add("crystal_ordo", new EnchantingStats(55, 3, -5, 5, 1), TTBlocks.CRYSTAL_ORDO);
        add("crystal_perditio", new EnchantingStats(50, 3, 12, -5, 0), TTBlocks.CRYSTAL_PERDITIO);
        add(
                "taint",
                new EnchantingStats(0, -5, 20, -10, 0),
                TTBlocks.CRYSTAL_VITIUM,
                TTBlocks.TAINT_ROCK,
                TTBlocks.TAINT_SOIL,
                TTBlocks.TAINT_CRUST,
                TTBlocks.TAINT_GEYSER,
                TTBlocks.TAINT_LOG,
                TTBlocks.TAINT_FIBRE);
        add("metal_thaumium", new EnchantingStats(60, 4, 0, 3, 0), TTBlocks.METAL_THAUMIUM_BLOCK);
        add("metal_void", new EnchantingStats(65, 4, 5, 8, 0), TTBlocks.METAL_VOID_BLOCK);
        add(
                "ancient_stone",
                new EnchantingStats(75, 6, 3, 5, 0),
                TTBlocks.STONE_ANCIENT,
                TTBlocks.STONE_ANCIENT_TILE,
                TTBlocks.STONE_ANCIENT_ROCK,
                TTBlocks.STONE_ANCIENT_GLYPHED,
                TTBlocks.STONE_ANCIENT_DOORWAY,
                TTBlocks.SLAB_ANCIENT,
                TTBlocks.STAIRS_ANCIENT,
                TTBlocks.PILLAR_ANCIENT,
                TTBlocks.PEDESTAL_ANCIENT);
        add(
                "eldritch_stone",
                new EnchantingStats(90, 10, 5, 10, 0),
                TTBlocks.ELDRITCH_STONE,
                TTBlocks.ELDRITCH_ROCK,
                TTBlocks.ELDRITCH_CRUST,
                TTBlocks.STONE_ELDRITCH_TILE,
                TTBlocks.SLAB_ELDRITCH,
                TTBlocks.STAIRS_ELDRITCH,
                TTBlocks.ELDRITCH_DOOR,
                TTBlocks.PILLAR_ELDRITCH,
                TTBlocks.PEDESTAL_ELDRITCH);
        add("eldritch_crust_glowing", new EnchantingStats(95, 12, 5, 12, 1), TTBlocks.ELDRITCH_CRUST_GLOWING);
        add("eldritch_stone_inert", new EnchantingStats(40, 1, 0, 0, 0), TTBlocks.ELDRITCH_STONE_INERT);
        add("vis_battery", new EnchantingStats(85, 8, 0, 10, 0), TTBlocks.VIS_BATTERY);
        add("jar_node", new EnchantingStats(100, 15, 10, 20, 1), TTBlocks.JAR_NODE);
    }

    @SafeVarargs
    private void add(String name, EnchantingStats stats, DeferredBlock<? extends Block>... members) {
        HolderSet<Block> set = HolderSet.direct(Arrays.stream(members)
                .map(member -> this.blocks.getOrThrow(member.getKey()))
                .toList());
        this.entries.add(new Entry(name, new BlockEnchantingStats(set, stats)));
    }

    @Override
    public String getName() {
        return "Apothic Enchanting Stats";
    }

    private record Entry(String name, BlockEnchantingStats stats) {}
}
