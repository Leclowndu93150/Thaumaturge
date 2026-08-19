package com.leclowndu93150.thaumaturge.content.equipment;

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
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public final class EnchantMining {
    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final int LOG_UPDATE_RADIUS = 3;
    private static final int LOG_UPDATE_DELAY_BASE = 50;
    private static final int LOG_UPDATE_DELAY_RANGE = 75;
    private static final int SEARCH_LIMIT_HORIZONTAL = 24;
    private static final int SEARCH_LIMIT_VERTICAL = 48;

    private EnchantMining() {}

    public static boolean harvestBlock(ServerLevel level, Player player, BlockPos pos, boolean skipEvent) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!skipEvent) {
            BreakBlockEvent event = CommonHooks.fireBlockBreak(level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos, state);
            if (event.isCanceled()) {
                return false;
            }
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        level.levelEvent(null, LEVEL_EVENT_BLOCK_BREAK, pos, Block.getId(state));
        boolean removed = level.removeBlock(pos, false);
        if (removed && !player.hasInfiniteMaterials() && player.hasCorrectToolForDrops(state)) {
            Block.dropResources(state, level, pos, blockEntity, player, player.getMainHandItem());
        }
        return removed;
    }

    public static boolean breakFurthest(ServerLevel level, BlockPos origin, BlockState block, Player player) {
        int reach = isLog(level, origin) ? 2 : 1;
        BlockPos furthest = findFurthest(level, origin, block, reach);
        boolean worked = harvestBlock(level, player, furthest, true);
        if (worked && isLog(level, origin)) {
            for (int xx = -LOG_UPDATE_RADIUS; xx <= LOG_UPDATE_RADIUS; xx++) {
                for (int yy = -LOG_UPDATE_RADIUS; yy <= LOG_UPDATE_RADIUS; yy++) {
                    for (int zz = -LOG_UPDATE_RADIUS; zz <= LOG_UPDATE_RADIUS; zz++) {
                        BlockPos p = furthest.offset(xx, yy, zz);
                        level.scheduleTick(p, level.getBlockState(p).getBlock(), LOG_UPDATE_DELAY_BASE + level.getRandom().nextInt(LOG_UPDATE_DELAY_RANGE));
                    }
                }
            }
        }
        return worked;
    }

    private static BlockPos findFurthest(ServerLevel level, BlockPos origin, BlockState block, int reach) {
        BlockPos best = origin;
        double bestDistance = 0.0;
        boolean advanced;
        do {
            advanced = false;
            for (int xx = -reach; xx <= reach && !advanced; xx++) {
                for (int yy = reach; yy >= -reach && !advanced; yy--) {
                    for (int zz = -reach; zz <= reach && !advanced; zz++) {
                        BlockPos candidate = best.offset(xx, yy, zz);
                        if (Math.abs(candidate.getX() - origin.getX()) > SEARCH_LIMIT_HORIZONTAL || Math.abs(candidate.getY() - origin.getY()) > SEARCH_LIMIT_VERTICAL
                                || Math.abs(candidate.getZ() - origin.getZ()) > SEARCH_LIMIT_HORIZONTAL) {
                            return best;
                        }
                        BlockState state = level.getBlockState(candidate);
                        boolean same = state.is(block.getBlock());
                        if (same && state.getDestroySpeed(level, candidate) >= 0.0F) {
                            double dx = candidate.getX() - origin.getX();
                            double dy = candidate.getY() - origin.getY();
                            double dz = candidate.getZ() - origin.getZ();
                            double distance = dx * dx + dy * dy + dz * dz;
                            if (distance > bestDistance) {
                                bestDistance = distance;
                                best = candidate;
                                advanced = true;
                            }
                        }
                    }
                }
            }
        } while (advanced);
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
