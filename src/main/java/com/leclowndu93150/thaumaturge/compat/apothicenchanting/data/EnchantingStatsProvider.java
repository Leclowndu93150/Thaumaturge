package com.leclowndu93150.thaumaturge.compat.apothicenchanting.data;

import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.apothicenchanting.BlockEnchantingStats;
import com.leclowndu93150.thaumaturge.compat.apothicenchanting.EnchantingStats;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
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
                            this.path.json(TCIds.rl(entry.name()))))
                    .toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(writes);
        });
    }

    private void build() {
        add(
                "arcane_stone",
                new EnchantingStats(40, 1, 0, 0, 0),
                TCBlocks.STONE_ARCANE,
                TCBlocks.STONE_ARCANE_BRICK,
                TCBlocks.SLAB_ARCANE_STONE,
                TCBlocks.SLAB_ARCANE_BRICK,
                TCBlocks.STAIRS_ARCANE,
                TCBlocks.STAIRS_ARCANE_BRICK,
                TCBlocks.PILLAR_ARCANE,
                TCBlocks.PEDESTAL_ARCANE);
        add(
                "silverwood",
                new EnchantingStats(45, 2, 0, 3, 0),
                TCBlocks.PLANK_SILVERWOOD,
                TCBlocks.SLAB_SILVERWOOD,
                TCBlocks.STAIRS_SILVERWOOD,
                TCBlocks.LOG_SILVERWOOD,
                TCBlocks.WOOD_SILVERWOOD,
                TCBlocks.STRIPPED_LOG_SILVERWOOD,
                TCBlocks.STRIPPED_WOOD_SILVERWOOD);
        add(
                "elemental_crystals",
                new EnchantingStats(50, 3, 3, 0, 0),
                TCBlocks.CRYSTAL_AER,
                TCBlocks.CRYSTAL_IGNIS,
                TCBlocks.CRYSTAL_AQUA,
                TCBlocks.CRYSTAL_TERRA);
        add("crystal_ordo", new EnchantingStats(55, 3, -5, 5, 1), TCBlocks.CRYSTAL_ORDO);
        add("crystal_perditio", new EnchantingStats(50, 3, 12, -5, 0), TCBlocks.CRYSTAL_PERDITIO);
        add(
                "taint",
                new EnchantingStats(0, -5, 20, -10, 0),
                TCBlocks.CRYSTAL_VITIUM,
                TCBlocks.TAINT_ROCK,
                TCBlocks.TAINT_SOIL,
                TCBlocks.TAINT_CRUST,
                TCBlocks.TAINT_GEYSER,
                TCBlocks.TAINT_LOG,
                TCBlocks.TAINT_FIBRE);
        add("metal_thaumium", new EnchantingStats(60, 4, 0, 3, 0), TCBlocks.METAL_THAUMIUM_BLOCK);
        add("metal_void", new EnchantingStats(65, 4, 5, 8, 0), TCBlocks.METAL_VOID_BLOCK);
        add(
                "ancient_stone",
                new EnchantingStats(75, 6, 3, 5, 0),
                TCBlocks.STONE_ANCIENT,
                TCBlocks.STONE_ANCIENT_TILE,
                TCBlocks.STONE_ANCIENT_ROCK,
                TCBlocks.STONE_ANCIENT_GLYPHED,
                TCBlocks.STONE_ANCIENT_DOORWAY,
                TCBlocks.SLAB_ANCIENT,
                TCBlocks.STAIRS_ANCIENT,
                TCBlocks.PILLAR_ANCIENT,
                TCBlocks.PEDESTAL_ANCIENT);
        add(
                "eldritch_stone",
                new EnchantingStats(90, 10, 5, 10, 0),
                TCBlocks.ELDRITCH_STONE,
                TCBlocks.ELDRITCH_ROCK,
                TCBlocks.ELDRITCH_CRUST,
                TCBlocks.STONE_ELDRITCH_TILE,
                TCBlocks.SLAB_ELDRITCH,
                TCBlocks.STAIRS_ELDRITCH,
                TCBlocks.ELDRITCH_DOOR,
                TCBlocks.PILLAR_ELDRITCH,
                TCBlocks.PEDESTAL_ELDRITCH);
        add("eldritch_crust_glowing", new EnchantingStats(95, 12, 5, 12, 1), TCBlocks.ELDRITCH_CRUST_GLOWING);
        add("eldritch_stone_inert", new EnchantingStats(40, 1, 0, 0, 0), TCBlocks.ELDRITCH_STONE_INERT);
        add("vis_battery", new EnchantingStats(85, 8, 0, 10, 0), TCBlocks.VIS_BATTERY);
        add("jar_node", new EnchantingStats(100, 15, 10, 20, 1), TCBlocks.JAR_NODE);
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
