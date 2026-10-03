package com.leclowndu93150.thaumaturge.content.equipment;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class EnchantMining {
    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final int LOG_UPDATE_RADIUS = 3;
    private static final int LOG_UPDATE_DELAY_BASE = 50;
    private static final int LOG_UPDATE_DELAY_RANGE = 75;
    private static final int SEARCH_LIMIT_HORIZONTAL = 24;
    private static final int SEARCH_LIMIT_VERTICAL = 48;
    private static final int SEARCH_NODE_LIMIT = 1024;

    private EnchantMining() {}

    public static boolean harvestBlock(ServerLevel level, Player player, BlockPos pos, boolean skipEvent) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!skipEvent) {
            BlockEvent.BreakEvent event = CommonHooks.fireBlockBreak(
                    level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos, state);
            if (event.isCanceled()) {
                return false;
            }
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        level.levelEvent(null, LEVEL_EVENT_BLOCK_BREAK, pos, Block.getId(state));
        boolean removed = level.removeBlock(pos, false);
        if (removed
                && !player.hasInfiniteMaterials()
                && player.getMainHandItem().isCorrectToolForDrops(state)) {
            Block.dropResources(state, level, pos, blockEntity, player, player.getMainHandItem());
        }
        return removed;
    }

    public static boolean breakFurthest(ServerLevel level, BlockPos origin, BlockState block, Player player) {
        BlockPos furthest = findFurthest(level, origin, block);
        boolean worked = harvestBlock(level, player, furthest, true);
        if (worked && isLog(level, origin)) {
            for (int xx = -LOG_UPDATE_RADIUS; xx <= LOG_UPDATE_RADIUS; xx++) {
                for (int yy = -LOG_UPDATE_RADIUS; yy <= LOG_UPDATE_RADIUS; yy++) {
                    for (int zz = -LOG_UPDATE_RADIUS; zz <= LOG_UPDATE_RADIUS; zz++) {
                        BlockPos p = furthest.offset(xx, yy, zz);
                        level.scheduleTick(
                                p,
                                level.getBlockState(p).getBlock(),
                                LOG_UPDATE_DELAY_BASE + level.getRandom().nextInt(LOG_UPDATE_DELAY_RANGE));
                    }
                }
            }
        }
        return worked;
    }

    private static BlockPos findFurthest(ServerLevel level, BlockPos origin, BlockState block) {
        LongOpenHashSet visited = new LongOpenHashSet();
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        ArrayDeque<BlockPos> next = new ArrayDeque<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        visited.add(origin.asLong());
        frontier.add(origin);
        BlockPos best = origin;
        while (!frontier.isEmpty()) {
            double bestDistance = -1.0;
            for (BlockPos pos : frontier) {
                double distance = pos.distSqr(origin);
                if (distance > bestDistance) {
                    bestDistance = distance;
                    best = pos;
                }
                if (visited.size() >= SEARCH_NODE_LIMIT) {
                    continue;
                }
                for (int xx = -1; xx <= 1; xx++) {
                    for (int yy = -1; yy <= 1; yy++) {
                        for (int zz = -1; zz <= 1; zz++) {
                            cursor.setWithOffset(pos, xx, yy, zz);
                            if (Math.abs(cursor.getX() - origin.getX()) > SEARCH_LIMIT_HORIZONTAL
                                    || Math.abs(cursor.getY() - origin.getY()) > SEARCH_LIMIT_VERTICAL
                                    || Math.abs(cursor.getZ() - origin.getZ()) > SEARCH_LIMIT_HORIZONTAL
                                    || visited.contains(cursor.asLong())
                                    || !level.isLoaded(cursor)) {
                                continue;
                            }
                            BlockState state = level.getBlockState(cursor);
                            if (state.is(block.getBlock()) && state.getDestroySpeed(level, cursor) >= 0.0F) {
                                visited.add(cursor.asLong());
                                next.add(cursor.immutable());
                            }
                        }
                    }
                }
            }
            ArrayDeque<BlockPos> done = frontier;
            frontier = next;
            next = done;
            next.clear();
        }
        return best;
    }

    public static boolean isLog(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.LOGS);
    }

    public static boolean isOre(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Tags.Blocks.ORES);
    }

    public static ItemStack fortuneTool(ServerLevel level, Player player, boolean silk, int fortune) {
        ItemStack tool = player.getMainHandItem().copy();
        if (tool.isEmpty() && (silk || fortune > 0)) {
            tool = new ItemStack(Items.NETHERITE_PICKAXE);
        }
        if (!tool.isEmpty() && (silk || fortune > 0)) {
            var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            if (silk) {
                tool.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
            }
            if (fortune > 0) {
                tool.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), fortune);
            }
        }
        return tool;
    }
}
