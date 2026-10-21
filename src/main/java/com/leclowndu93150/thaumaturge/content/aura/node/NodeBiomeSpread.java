package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

final class NodeBiomeSpread {
    private static final int SPREAD_INTERVAL = 50;
    private static final int DARK_RANGE = 12;
    private static final int PURE_RANGE = 8;
    private static final int SILVERWOOD_RADIUS = 1;

    private NodeBiomeSpread() {}

    static void tick(ServerLevel level, BlockEntityNode node, BlockPos pos, int tickCounter) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (tickCounter % SPREAD_INTERVAL != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        switch (node.getNodeType()) {
            case DARK -> eerie(level, sample(pos, DARK_RANGE, random));
            case PURE -> purify(level, pos, sample(pos, PURE_RANGE, random));
            case NORMAL, UNSTABLE, HUNGRY, TAINTED -> {}
        }
    }

    static void revertPainted(ServerLevel level, BlockPos pos, NodeType type) {
        switch (type) {
            case DARK -> revertArea(level, pos, DARK_RANGE, TTBiomes.EERIE);
            case PURE -> revertArea(level, pos, PURE_RANGE, TTBiomes.MAGICAL_FOREST);
            case NORMAL, UNSTABLE, HUNGRY, TAINTED -> {}
        }
    }

    private static void revertArea(ServerLevel level, BlockPos origin, int range, ResourceKey<Biome> painted) {
        int reach = range - 1;
        BlockPos.MutableBlockPos column = new BlockPos.MutableBlockPos();
        for (int quartX = QuartPos.fromBlock(origin.getX() - reach);
                quartX <= QuartPos.fromBlock(origin.getX() + reach);
                quartX++) {
            for (int quartZ = QuartPos.fromBlock(origin.getZ() - reach);
                    quartZ <= QuartPos.fromBlock(origin.getZ() + reach);
                    quartZ++) {
                column.set(QuartPos.toBlock(quartX), origin.getY(), QuartPos.toBlock(quartZ));
                if (level.hasChunkAt(column)) {
                    TaintBiomeManager.revertColumn(level, column, painted);
                }
            }
        }
    }

    private static BlockPos sample(BlockPos origin, int range, RandomSource random) {
        return origin.offset(
                random.nextInt(range) - random.nextInt(range), 0, random.nextInt(range) - random.nextInt(range));
    }

    private static void eerie(ServerLevel level, BlockPos sample) {
        if (level.hasChunkAt(sample) && !level.getBiome(sample).is(TTBiomes.EERIE)) {
            TaintBiomeManager.replaceColumn(level, sample, TTBiomes.EERIE);
        }
    }

    private static void purify(ServerLevel level, BlockPos origin, BlockPos sample) {
        if (!level.hasChunkAt(sample)) {
            return;
        }
        if (TaintBiomeManager.isTainted(level, sample) || silverwoodNearby(level, origin)) {
            TaintBiomeManager.replaceColumn(level, sample, TTBiomes.MAGICAL_FOREST);
        }
    }

    private static boolean silverwoodNearby(ServerLevel level, BlockPos origin) {
        for (BlockPos cell : BlockPos.betweenClosed(
                origin.offset(-SILVERWOOD_RADIUS, -SILVERWOOD_RADIUS, -SILVERWOOD_RADIUS),
                origin.offset(SILVERWOOD_RADIUS, SILVERWOOD_RADIUS, SILVERWOOD_RADIUS))) {
            if (level.hasChunkAt(cell) && level.getBlockState(cell).is(TTBlockTags.SILVERWOOD_LOGS)) {
                return true;
            }
        }
        return false;
    }
}
