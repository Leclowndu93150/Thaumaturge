package com.leclowndu93150.thaumaturge.content.eldritch.gen;

import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeCell;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public final class GenPassage extends GenCommonPieces {
    private GenPassage() {}

    static void generateDefaultPassage(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        generateConnections(ctx, cx, cz, y, cell, 4, false);
        int mod = 0;
        if (cell.north && cell.south && cell.west && cell.east && ctx.random.nextBoolean()) {
            mod = 1;
        }
        for (int w = 1; w < 8; w++) {
            for (int h = 1; h < 8; h++) {
                if (w == 4 && h == 4 && mod == 1) {
                    ctx.placeBlock(x + 4 + w, y + 2, z + 4 + h, GenContext.GLOW_TILE, cell);
                    ctx.placeBlock(x + 4 + w, y + 8, z + 4 + h, GenContext.GLOW_TILE, cell);
                } else {
                    ctx.placeBlock(x + 4 + w, y + 2, z + 4 + h, trappedOrStone(ctx, cell), cell);
                    ctx.placeBlock(x + 4 + w, y + 8, z + 4 + h, trappedOrStone(ctx, cell), cell);
                }
                ctx.placeBlock(x + 4 + w, y, z + 4 + h, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + 4 + w, y + 10, z + 4 + h, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + 4 + w, y + 1, z + 4 + h, GenContext.VOID, cell);
                ctx.placeBlock(x + 4 + w, y + 9, z + 4 + h, GenContext.VOID, cell);
            }
        }
        if (cell.north) {
            for (int w = 2 + mod; w < 9 - mod; w++) {
                for (int h = 2 + mod; h < 9 - mod; h++) {
                    ctx.placeBlock(x + 3 + w, y + 10 - h, z + 5, PAT_CONNECT[h][w], Direction.NORTH, cell);
                }
            }
            if (mod == 0) {
                if (cell.west) {
                    ctx.placeBlock(x + 6, y + 3, z + 6, GenContext.STAIR_A, Direction.EAST, cell);
                    ctx.placeBlock(x + 6, y + 7, z + 6, GenContext.STAIR_A_INV, Direction.EAST, cell);
                }
                if (cell.east) {
                    ctx.placeBlock(x + 10, y + 3, z + 6, GenContext.STAIR_A, Direction.EAST, cell);
                    ctx.placeBlock(x + 10, y + 7, z + 6, GenContext.STAIR_A_INV, Direction.EAST, cell);
                }
            }
        } else {
            for (int w = 1; w < 8; w++) {
                for (int h = 1; h < 8; h++) {
                    ctx.placeBlock(x + 4 + w, y + 9 - h, z + 5, trappedOrStone(ctx, cell), cell);
                    ctx.placeBlock(x + 4 + w, y + 9 - h, z + 4, GenContext.VOID, cell);
                    ctx.placeBlock(x + 4 + w, y + 9 - h, z + 3, GenContext.BEDROCK, cell);
                    if (h == 7) {
                        ctx.placeBlock(x + 4 + w, y + 1, z + 4, GenContext.BEDROCK, cell);
                        ctx.placeBlock(x + 4 + w, y + 9, z + 4, GenContext.BEDROCK, cell);
                    }
                    if (w == 7) {
                        ctx.placeBlock(x + 4, y + 9 - h, z + 4, GenContext.BEDROCK, cell);
                        ctx.placeBlock(x + 12, y + 9 - h, z + 4, GenContext.BEDROCK, cell);
                    }
                }
            }
            for (int w = 2; w < 7; w++) {
                ctx.placeBlock(x + 4 + w, y + 3, z + 6, GenContext.STAIR_A, Direction.EAST, cell);
                ctx.placeBlock(x + 4 + w, y + 7, z + 6, GenContext.STAIR_A_INV, Direction.EAST, cell);
            }
        }
        if (cell.south) {
            for (int w = 2 + mod; w < 9 - mod; w++) {
                for (int h = 2 + mod; h < 9 - mod; h++) {
                    ctx.placeBlock(x + 3 + w, y + 10 - h, z + 11, PAT_CONNECT[h][w], Direction.SOUTH, cell);
                }
            }
            if (mod == 0) {
                if (cell.west) {
                    ctx.placeBlock(x + 6, y + 3, z + 10, GenContext.STAIR_B, Direction.EAST, cell);
                    ctx.placeBlock(x + 6, y + 7, z + 10, GenContext.STAIR_B_INV, Direction.EAST, cell);
                }
                if (cell.east) {
                    ctx.placeBlock(x + 10, y + 3, z + 10, GenContext.STAIR_B, Direction.EAST, cell);
                    ctx.placeBlock(x + 10, y + 7, z + 10, GenContext.STAIR_B_INV, Direction.EAST, cell);
                }
            }
        } else {
            for (int w = 1; w < 8; w++) {
                for (int h = 1; h < 8; h++) {
                    ctx.placeBlock(x + 4 + w, y + 9 - h, z + 11, trappedOrStone(ctx, cell), cell);
                    ctx.placeBlock(x + 4 + w, y + 9 - h, z + 12, GenContext.VOID, cell);
                    ctx.placeBlock(x + 4 + w, y + 9 - h, z + 13, GenContext.BEDROCK, cell);
                    if (h == 7) {
                        ctx.placeBlock(x + 4 + w, y + 1, z + 12, GenContext.BEDROCK, cell);
                        ctx.placeBlock(x + 4 + w, y + 9, z + 12, GenContext.BEDROCK, cell);
                    }
                    if (w == 7) {
                        ctx.placeBlock(x + 4, y + 9 - h, z + 12, GenContext.BEDROCK, cell);
                        ctx.placeBlock(x + 12, y + 9 - h, z + 12, GenContext.BEDROCK, cell);
                    }
                }
            }
            for (int w = 2; w < 7; w++) {
                ctx.placeBlock(x + 4 + w, y + 3, z + 10, GenContext.STAIR_B, Direction.EAST, cell);
                ctx.placeBlock(x + 4 + w, y + 7, z + 10, GenContext.STAIR_B_INV, Direction.EAST, cell);
            }
        }
        if (cell.east) {
            for (int w = 2 + mod; w < 9 - mod; w++) {
                for (int h = 2 + mod; h < 9 - mod; h++) {
                    ctx.placeBlock(x + 11, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.EAST, cell);
                }
            }
            if (mod == 0) {
                if (cell.north) {
                    ctx.placeBlock(x + 10, y + 3, z + 6, GenContext.STAIR_B, Direction.NORTH, cell);
                    ctx.placeBlock(x + 10, y + 7, z + 6, GenContext.STAIR_B_INV, Direction.NORTH, cell);
                }
                if (cell.south) {
                    ctx.placeBlock(x + 10, y + 3, z + 10, GenContext.STAIR_B, Direction.NORTH, cell);
                    ctx.placeBlock(x + 10, y + 7, z + 10, GenContext.STAIR_B_INV, Direction.NORTH, cell);
                }
            }
        } else {
            for (int w = 1; w < 8; w++) {
                for (int h = 1; h < 8; h++) {
                    ctx.placeBlock(x + 11, y + 9 - h, z + 4 + w, trappedOrStone(ctx, cell), cell);
                    ctx.placeBlock(x + 12, y + 9 - h, z + 4 + w, GenContext.VOID, cell);
                    ctx.placeBlock(x + 13, y + 9 - h, z + 4 + w, GenContext.BEDROCK, cell);
                    if (h == 7) {
                        ctx.placeBlock(x + 12, y + 1, z + 4 + w, GenContext.BEDROCK, cell);
                        ctx.placeBlock(x + 12, y + 9, z + 4 + w, GenContext.BEDROCK, cell);
                    }
                    if (w == 7) {
                        ctx.placeBlock(x + 12, y + 9 - h, z + 4, GenContext.BEDROCK, cell);
                        ctx.placeBlock(x + 12, y + 9 - h, z + 12, GenContext.BEDROCK, cell);
                    }
                }
            }
            for (int w = 2; w < 7; w++) {
                ctx.placeBlock(x + 10, y + 3, z + 4 + w, GenContext.STAIR_B, Direction.NORTH, cell);
                ctx.placeBlock(x + 10, y + 7, z + 4 + w, GenContext.STAIR_B_INV, Direction.NORTH, cell);
            }
        }
        if (cell.west) {
            for (int w = 2 + mod; w < 9 - mod; w++) {
                for (int h = 2 + mod; h < 9 - mod; h++) {
                    ctx.placeBlock(x + 5, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], Direction.WEST, cell);
                }
            }
            if (mod == 0) {
                if (cell.north) {
                    ctx.placeBlock(x + 6, y + 3, z + 6, GenContext.STAIR_A, Direction.NORTH, cell);
                    ctx.placeBlock(x + 6, y + 7, z + 6, GenContext.STAIR_A_INV, Direction.NORTH, cell);
                }
                if (cell.south) {
                    ctx.placeBlock(x + 6, y + 3, z + 10, GenContext.STAIR_A, Direction.NORTH, cell);
                    ctx.placeBlock(x + 6, y + 7, z + 10, GenContext.STAIR_A_INV, Direction.NORTH, cell);
                }
            }
        } else {
            for (int w = 1; w < 8; w++) {
                for (int h = 1; h < 8; h++) {
                    ctx.placeBlock(x + 5, y + 9 - h, z + 4 + w, trappedOrStone(ctx, cell), cell);
                    ctx.placeBlock(x + 4, y + 9 - h, z + 4 + w, GenContext.VOID, cell);
                    ctx.placeBlock(x + 3, y + 9 - h, z + 4 + w, GenContext.BEDROCK, cell);
                    if (h == 7) {
                        ctx.placeBlock(x + 4, y + 1, z + 4 + w, GenContext.BEDROCK, cell);
                        ctx.placeBlock(x + 4, y + 9, z + 4 + w, GenContext.BEDROCK, cell);
                    }
                    if (w == 7) {
                        ctx.placeBlock(x + 4, y + 9 - h, z + 4, GenContext.BEDROCK, cell);
                        ctx.placeBlock(x + 4, y + 9 - h, z + 12, GenContext.BEDROCK, cell);
                    }
                }
            }
            for (int w = 2; w < 7; w++) {
                ctx.placeBlock(x + 6, y + 3, z + 4 + w, GenContext.STAIR_A, Direction.NORTH, cell);
                ctx.placeBlock(x + 6, y + 7, z + 4 + w, GenContext.STAIR_A_INV, Direction.NORTH, cell);
            }
        }
        if (mod == 1) {
            ctx.placeBlock(x + 5, y + 3, z + 5, GenContext.STAIR_A, Direction.EAST, cell);
            ctx.placeBlock(x + 5, y + 7, z + 5, GenContext.STAIR_A_INV, Direction.EAST, cell);
            ctx.placeBlock(x + 5, y + 3, z + 6, GenContext.STAIR_A, Direction.NORTH, cell);
            ctx.placeBlock(x + 5, y + 7, z + 6, GenContext.STAIR_A_INV, Direction.NORTH, cell);
            ctx.placeBlock(x + 11, y + 3, z + 5, GenContext.STAIR_A, Direction.EAST, cell);
            ctx.placeBlock(x + 11, y + 7, z + 5, GenContext.STAIR_A_INV, Direction.EAST, cell);
            ctx.placeBlock(x + 11, y + 3, z + 6, GenContext.STAIR_B, Direction.NORTH, cell);
            ctx.placeBlock(x + 11, y + 7, z + 6, GenContext.STAIR_B_INV, Direction.NORTH, cell);
            ctx.placeBlock(x + 5, y + 3, z + 11, GenContext.STAIR_A, Direction.NORTH, cell);
            ctx.placeBlock(x + 5, y + 7, z + 11, GenContext.STAIR_A_INV, Direction.NORTH, cell);
            ctx.placeBlock(x + 6, y + 3, z + 11, GenContext.STAIR_B, Direction.EAST, cell);
            ctx.placeBlock(x + 6, y + 7, z + 11, GenContext.STAIR_B_INV, Direction.EAST, cell);
            ctx.placeBlock(x + 11, y + 3, z + 11, GenContext.STAIR_B, Direction.NORTH, cell);
            ctx.placeBlock(x + 11, y + 7, z + 11, GenContext.STAIR_B_INV, Direction.NORTH, cell);
            ctx.placeBlock(x + 10, y + 3, z + 11, GenContext.STAIR_B, Direction.EAST, cell);
            ctx.placeBlock(x + 10, y + 7, z + 11, GenContext.STAIR_B_INV, Direction.EAST, cell);
        }
        if (cell.feature == 12) {
            for (int w = -4; w <= 4; w++) {
                for (int h = -4; h < 5; h++) {
                    for (int j = -4; j <= 4; j++) {
                        BlockPos target = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
                        if ((ctx.level.isEmptyBlock(target) || ctx.level.getBlockState(target).is(TCBlocks.ELDRITCH_STONE.get())
                                || ctx.level.getBlockState(target).is(TCBlocks.ELDRITCH_STONE_INERT.get()) || ctx.level.getBlockState(target).is(TCBlocks.STAIRS_ELDRITCH.get()))
                                && ctx.random.nextBoolean()) {
                            ctx.placeBlock(x + 8 + w, y + 4 + h, z + 8 + j, GenContext.CRUST, cell);
                        }
                    }
                }
            }
        }
        if (cell.feature == 13) {
            for (int w = -4; w <= 4; w++) {
                for (int h = -3; h <= 3; h++) {
                    for (int j = -4; j <= 4; j++) {
                        BlockPos target = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
                        if (ctx.level.isEmptyBlock(target) && isAdjacentToSolid(ctx, target) && ctx.random.nextInt(3) != 0) {
                            ctx.level.setBlock(target, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        if (cell.feature == 14) {
            for (int w = -3; w <= 3; w++) {
                for (int h = -3; h <= 3; h++) {
                    for (int j = -3; j <= 3; j++) {
                        BlockPos target = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
                        if (ctx.level.isEmptyBlock(target) && ctx.random.nextFloat() < 0.35F) {
                            ctx.level.setBlock(target, Blocks.COBWEB.defaultBlockState(), 3);
                        }
                    }
                }
            }
            BlockPos spawnerPos = new BlockPos(x + 8, y + 4, z + 8);
            ctx.level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 3);
            if (ctx.level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawnerEntity) {
                spawnerEntity.setEntityId(TCEntities.MIND_SPIDER.get(), ctx.random);
            }
        }
    }

    private static int trappedOrStone(GenContext ctx, MazeCell cell) {
        return cell.feature == 11 && ctx.random.nextInt(3) == 0 ? GenContext.STONE_TRAPPED : GenContext.STONE;
    }

    private static boolean isAdjacentToSolid(GenContext ctx, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (ctx.level.getBlockState(pos.relative(dir)).isSolidRender()) {
                return true;
            }
        }
        return false;
    }
}
