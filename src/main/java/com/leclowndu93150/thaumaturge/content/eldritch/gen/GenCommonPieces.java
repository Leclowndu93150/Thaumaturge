package com.leclowndu93150.thaumaturge.content.eldritch.gen;

import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCrabSpawner;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchInset;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeCell;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GenCommonPieces {
    static final int[][] PAT_CONNECT = {{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1}, {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1}, {1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1}, {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1}, {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1}, {1, 8, 2, 3, 9, 9, 9, 4, 2, 8, 1}, {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
            {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1}, {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}};

    static void generateConnections(GenContext ctx, int cx, int cz, int y, MazeCell cell, int depth, boolean justTheTip) {
        int x = cx * 16;
        int z = cz * 16;
        if (cell.north) {
            for (int d = 0; d <= depth; d++) {
                for (int w = start(d, depth, justTheTip); w < end(d, depth, justTheTip); w++) {
                    for (int h = start(d, depth, justTheTip); h < end(d, depth, justTheTip); h++) {
                        if (d != depth || !justTheTip || PAT_CONNECT[h][w] != 8) {
                            ctx.placeBlock(x + 3 + w, y + 10 - h, z + d, PAT_CONNECT[h][w], Direction.NORTH, cell);
                        }
                    }
                }
            }
        }
        if (cell.south) {
            for (int d = 0; d <= depth; d++) {
                for (int w = start(d, depth, justTheTip); w < end(d, depth, justTheTip); w++) {
                    for (int h = start(d, depth, justTheTip); h < end(d, depth, justTheTip); h++) {
                        if (d != depth || !justTheTip || PAT_CONNECT[h][w] != 8) {
                            ctx.placeBlock(x + 3 + w, y + 10 - h, z + 16 - d, PAT_CONNECT[h][w], Direction.SOUTH, cell);
                        }
                    }
                }
            }
        }
        if (cell.east) {
            for (int d = 0; d <= depth; d++) {
                for (int w = start(d, depth, justTheTip); w < end(d, depth, justTheTip); w++) {
                    for (int h = start(d, depth, justTheTip); h < end(d, depth, justTheTip); h++) {
                        if (d != depth || !justTheTip || PAT_CONNECT[h][w] != 8) {
                            ctx.placeBlock(x + 16 - d, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.EAST, cell);
                        }
                    }
                }
            }
        }
        if (cell.west) {
            for (int d = 0; d <= depth; d++) {
                for (int w = start(d, depth, justTheTip); w < end(d, depth, justTheTip); w++) {
                    for (int h = start(d, depth, justTheTip); h < end(d, depth, justTheTip); h++) {
                        if (d != depth || !justTheTip || PAT_CONNECT[h][w] != 8) {
                            ctx.placeBlock(x + d, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.WEST, cell);
                        }
                    }
                }
            }
        }
    }

    private static int start(int d, int depth, boolean justTheTip) {
        if (d == depth && justTheTip) {
            return 2;
        }
        return d == depth - 1 && justTheTip ? 1 : 0;
    }

    private static int end(int d, int depth, boolean justTheTip) {
        if (d == depth && justTheTip) {
            return 9;
        }
        return d == depth - 1 && justTheTip ? 10 : 11;
    }

    static void processDecorations(GenContext ctx) {
        for (BlockPos pos : ctx.decoUrn) {
            if (ctx.level.isEmptyBlock(pos.above())) {
                ctx.level.setBlock(pos, TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
                float roll = ctx.random.nextFloat();
                BlockState urn = roll < 0.025F
                        ? TCBlocks.LOOT_URN_RARE.get().defaultBlockState()
                        : roll < 0.1F ? TCBlocks.LOOT_URN_UNCOMMON.get().defaultBlockState() : TCBlocks.LOOT_URN_COMMON.get().defaultBlockState();
                ctx.level.setBlock(pos.above(), urn, 3);
            }
        }
        for (BlockPos pos : ctx.decoCommon) {
            int exposed = countExposedSides(ctx, pos);
            if (exposed > 0 && (exposed == 1 || !isBedrockShowing(ctx, pos)) && !isAdjacentToEldritchDeco(ctx, pos)) {
                BlockState state = ctx.random.nextInt(3) != 0
                        ? TCBlocks.ELDRITCH_CRUST_GLOWING.get().defaultBlockState()
                        : ctx.random.nextInt(8) != 0 ? TCBlocks.ELDRITCH_STONE_CRYSTAL.get().defaultBlockState() : TCBlocks.ELDRITCH_TRAP.get().defaultBlockState();
                ctx.level.setBlock(pos, state, 3);
                if (state.getBlock() instanceof BlockEldritchInset) {
                    ctx.level.getChunk(pos).markPosForPostprocessing(pos);
                }
                if (state.is(TCBlocks.ELDRITCH_CRUST_GLOWING.get()) && ctx.random.nextInt(12) == 0) {
                    for (Direction dir : Direction.values()) {
                        BlockPos side = pos.relative(dir);
                        if (ctx.level.isEmptyBlock(side)) {
                            ctx.level.setBlock(side, TCBlocks.CRYSTAL_VITIUM.get().defaultBlockState(), 3);
                            break;
                        }
                    }
                }
            }
        }
        for (BlockPos pos : ctx.crabSpawner) {
            int exposed = countExposedSides(ctx, pos);
            if (exposed == 1 && !isAdjacentToEldritchDeco(ctx, pos)) {
                for (Direction dir : Direction.values()) {
                    BlockPos front = pos.relative(dir);
                    if (ctx.level.getBlockState(front).isAir()) {
                        ctx.level.setBlock(front, TCBlocks.ELDRITCH_CRAB_SPAWNER.get().defaultBlockState().setValue(BlockEldritchCrabSpawner.FACING, dir), 3);
                        break;
                    }
                }
            }
        }
        ctx.decoCommon.clear();
        ctx.crabSpawner.clear();
        ctx.decoUrn.clear();
    }

    private static int countExposedSides(GenContext ctx, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (ctx.level.getBlockState(pos.relative(dir)).isAir()) {
                count++;
            }
        }
        return count;
    }

    private static boolean isBedrockShowing(GenContext ctx, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!ctx.level.getBlockState(pos.relative(dir)).isSolidRender()) {
                BlockState opposite = ctx.level.getBlockState(pos.relative(dir.getOpposite()));
                if (opposite.is(Blocks.BEDROCK) || opposite.is(TCBlocks.ELDRITCH_NOTHING.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAdjacentToEldritchDeco(GenContext ctx, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState neighbor = ctx.level.getBlockState(pos.relative(dir));
            if (neighbor.is(TCBlocks.ELDRITCH_CRUST_GLOWING.get()) || neighbor.is(TCBlocks.ELDRITCH_STONE_CRYSTAL.get()) || neighbor.is(TCBlocks.ELDRITCH_TRAP.get())
                    || neighbor.is(TCBlocks.ELDRITCH_CRAB_SPAWNER.get())) {
                return true;
            }
        }
        return false;
    }
}
