package com.leclowndu93150.thaumaturge.content.eldritch.gen;

import com.leclowndu93150.thaumaturge.content.eldritch.block.EldritchArenaShapes;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeCell;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class GenRooms extends GenCommonPieces {
    private static final int[][] PAT_DOORWAY = {{0, 2, 2, 2, 2, 2, 0}, {2, 2, 9, 9, 9, 2, 2}, {2, 9, 9, 9, 9, 9, 2}, {2, 9, 9, 1, 9, 9, 2}, {2, 9, 9, 9, 9, 9, 2}, {2, 2, 9, 9, 9, 2, 2},
            {0, 2, 2, 2, 2, 2, 0}};

    private GenRooms() {}

    static void generateBossRoom(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        switch (cell.feature) {
            case MazeCell.FEATURE_BOSS_NW -> Gen2x2.generateUpperLeft(ctx, cx, cz, y, cell);
            case MazeCell.FEATURE_BOSS_NE -> Gen2x2.generateUpperRight(ctx, cx, cz, y, cell);
            case MazeCell.FEATURE_BOSS_SW -> Gen2x2.generateLowerLeft(ctx, cx, cz, y, cell);
            case MazeCell.FEATURE_BOSS_SE -> Gen2x2.generateLowerRight(ctx, cx, cz, y, cell);
        }
        for (int a = 0; a < 7; a++) {
            for (int b = 0; b < 7; b++) {
                int xx = 0;
                int zz = 0;
                Direction dir = Direction.NORTH;
                if (cell.north) {
                    xx = x + 5 + a;
                    zz = z + 3;
                    dir = Direction.NORTH;
                }
                if (cell.south) {
                    xx = x + 5 + a;
                    zz = z + 13;
                    dir = Direction.SOUTH;
                }
                if (cell.east) {
                    xx = x + 13;
                    zz = z + 5 + a;
                    dir = Direction.EAST;
                }
                if (cell.west) {
                    xx = x + 3;
                    zz = z + 5 + a;
                    dir = Direction.WEST;
                }
                if (xx == 0 && zz == 0) {
                    continue;
                }
                switch (PAT_DOORWAY[a][b]) {
                    case 1 -> {
                        ctx.placeBlock(xx, y + 2 + b, zz, GenContext.DOOR_LOCK, cell);
                        ctx.setLockFacing(new BlockPos(xx, y + 2 + b, zz), dir);
                    }
                    case 2 -> ctx.placeBlock(xx, y + 2 + b, zz, GenContext.DOOR_BLOCK, cell);
                    case 9 -> ctx.placeBlock(xx, y + 2 + b, zz, GenContext.VOID_DOOR, cell);
                }
            }
        }
    }

    static void generatePortalRoom(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.BEDROCK, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || a == 14 || b == 2 || b == 14) && (a != 2 || b <= 3 || b >= 12 || !cell.west || c >= 10) && (a != 14 || b <= 3 || b >= 12 || !cell.east || c >= 10)
                            && (b != 2 || a <= 3 || a >= 12 || !cell.north || c >= 10) && (b != 14 || a <= 3 || a >= 12 || !cell.south || c >= 10)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.VOID, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if ((a == 3 || a == 13 || b == 3 || b == 13) && (a > 4 || b > 4) && (a > 4 || b < 12) && (a < 12 || b > 4) && (a < 12 || b < 12)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.STONE, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                ctx.placeBlock(x + a, y - 1, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 1, z + b, GenContext.STONE_NOSPAWN, cell);
                ctx.placeBlock(x + a, y + 13, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y + 12, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 11, z + b, GenContext.STONE, cell);
                if (a > 1 && a < 15 && b > 1 && b < 15) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q - 1; g++) {
                        ctx.placeBlock(x + a, y + 1 + g, z + b, GenContext.STONE_NOSPAWN, cell);
                    }
                }
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q; g++) {
                        ctx.placeBlock(x + a, y + 11 - g, z + b, GenContext.STONE_NOSPAWN, cell);
                    }
                }
            }
        }
        for (int g = 0; g < 5; g++) {
            ctx.placeBlock(x + 6 + g, y + 2, z + 4, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
            ctx.placeBlock(x + 6 + g, y + 2, z + 12, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
            ctx.placeBlock(x + 12, y + 2, z + 6 + g, GenContext.STAIR_DIRECTIONAL, Direction.EAST, cell);
            ctx.placeBlock(x + 4, y + 2, z + 6 + g, GenContext.STAIR_DIRECTIONAL, Direction.WEST, cell);
        }
        generateConnections(ctx, cx, cz, y, cell, 3, true);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 1; c < 12; c++) {
                    if (a <= 4 && b <= 4 || a <= 4 && b >= 12 || a >= 12 && b <= 4 || a >= 12 && b >= 12) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.AIR_REPL, cell);
                    }
                }
            }
        }
        stairCorners(ctx, cell, x, z, y);
        ctx.level.setBlock(new BlockPos(x + 8, y + 2, z + 8), TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
        ctx.level.setBlock(new BlockPos(x + 8, y + 3, z + 8), TCBlocks.ELDRITCH_PORTAL.get().defaultBlockState(), 3);
        EldritchArenaShapes.genObelisk(ctx.level, x + 8, y + 4, z + 8);
    }

    private static void stairCorners(GenContext ctx, MazeCell cell, int x, int z, int y) {
        ctx.placeBlock(x + 5, y + 3, z + 5, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
        ctx.placeBlock(x + 4, y + 3, z + 5, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
        ctx.placeBlock(x + 5, y + 3, z + 4, GenContext.STAIR_DIRECTIONAL, Direction.WEST, cell);
        ctx.placeBlock(x + 5, y + 8, z + 5, GenContext.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
        ctx.placeBlock(x + 4, y + 8, z + 5, GenContext.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
        ctx.placeBlock(x + 5, y + 8, z + 4, GenContext.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
        ctx.placeBlock(x + 12, y + 3, z + 5, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
        ctx.placeBlock(x + 11, y + 3, z + 5, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
        ctx.placeBlock(x + 11, y + 3, z + 4, GenContext.STAIR_DIRECTIONAL, Direction.EAST, cell);
        ctx.placeBlock(x + 12, y + 8, z + 5, GenContext.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
        ctx.placeBlock(x + 11, y + 8, z + 5, GenContext.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
        ctx.placeBlock(x + 11, y + 8, z + 4, GenContext.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
        ctx.placeBlock(x + 5, y + 3, z + 11, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
        ctx.placeBlock(x + 4, y + 3, z + 11, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
        ctx.placeBlock(x + 5, y + 3, z + 12, GenContext.STAIR_DIRECTIONAL, Direction.WEST, cell);
        ctx.placeBlock(x + 5, y + 8, z + 11, GenContext.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
        ctx.placeBlock(x + 4, y + 8, z + 11, GenContext.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
        ctx.placeBlock(x + 5, y + 8, z + 12, GenContext.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
        ctx.placeBlock(x + 12, y + 3, z + 11, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
        ctx.placeBlock(x + 11, y + 3, z + 11, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
        ctx.placeBlock(x + 11, y + 3, z + 12, GenContext.STAIR_DIRECTIONAL, Direction.EAST, cell);
        ctx.placeBlock(x + 12, y + 8, z + 11, GenContext.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
        ctx.placeBlock(x + 11, y + 8, z + 11, GenContext.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
        ctx.placeBlock(x + 11, y + 8, z + 12, GenContext.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
    }

    static void generateKeyRoom(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.BEDROCK, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || a == 14 || b == 2 || b == 14) && (a != 2 || b <= 3 || b >= 12 || !cell.west || c >= 10) && (a != 14 || b <= 3 || b >= 12 || !cell.east || c >= 10)
                            && (b != 2 || a <= 3 || a >= 12 || !cell.north || c >= 10) && (b != 14 || a <= 3 || a >= 12 || !cell.south || c >= 10)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.VOID, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        if (c > 3 && c < 9 && (a == 8 || b == 8) || c > 4 && c < 8 && (a == 7 || b == 7 || a == 9 || b == 9)) {
                            if (a != 8 && b != 8 || c != 6) {
                                ctx.placeBlock(x + a, y + c, z + b, GenContext.STONE_NOSPAWN, cell);
                            }
                        } else {
                            ctx.placeBlock(x + a, y + c, z + b, GenContext.ROCK, cell);
                        }
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                ctx.placeBlock(x + a, y - 1, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 1, z + b, GenContext.STONE, cell);
                ctx.placeBlock(x + a, y + 13, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y + 12, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 11, z + b, GenContext.STONE, cell);
                if (a > 1 && a < 15 && b > 1 && b < 15) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q - 1; g++) {
                        ctx.placeBlock(x + a, y + 1 + g, z + b, GenContext.STONE, cell);
                    }
                }
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q; g++) {
                        ctx.placeBlock(x + a, y + 11 - g, z + b, GenContext.STONE, cell);
                    }
                }
            }
        }
        for (int g = 0; g < 5; g++) {
            ctx.placeBlock(x + 6 + g, y + 2, z + 4, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
            ctx.placeBlock(x + 6 + g, y + 2, z + 12, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
            ctx.placeBlock(x + 12, y + 2, z + 6 + g, GenContext.STAIR_DIRECTIONAL, Direction.EAST, cell);
            ctx.placeBlock(x + 4, y + 2, z + 6 + g, GenContext.STAIR_DIRECTIONAL, Direction.WEST, cell);
        }
        generateConnections(ctx, cx, cz, y, cell, 3, true);
        ctx.level.setBlock(new BlockPos(x + 8, y + 2, z + 8), TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
        EntitySpecialItem item = new EntitySpecialItem(ctx.level.getLevel(), x + 8.5, y + 3.5, z + 8.5, new ItemStack(TCItems.RUNED_TABLET.get()));
        item.setDeltaMovement(0.0, 0.0, 0.0);
        item.setUnlimitedLifetime();
        ctx.level.addFreshEntity(item);
        Difficulty difficulty = ctx.level.getDifficulty();
        int guardians = 2 + (difficulty == Difficulty.HARD ? 2 : difficulty == Difficulty.NORMAL ? 1 : 0);
        for (int qq = 0; qq < guardians; qq++) {
            EntityEldritchGuardian guardian = new EntityEldritchGuardian(TCEntities.ELDRITCH_GUARDIAN.get(), ctx.level.getLevel());
            double gx = x + 8.5 + Mth.randomBetweenInclusive(ctx.random, 1, 3) * Mth.randomBetweenInclusive(ctx.random, -1, 1);
            double gz = z + 8.5 + Mth.randomBetweenInclusive(ctx.random, 1, 3) * Mth.randomBetweenInclusive(ctx.random, -1, 1);
            guardian.snapTo(gx, y + 2, gz, 0.0F, 0.0F);
            guardian.setHomeTo(new BlockPos(x + 8, y + 2, z + 8), 16);
            if (qq == 0 && guardians >= 4) {
                ChampionHelper.makeChampion(guardian, true);
            }
            ctx.level.addFreshEntity(guardian);
        }
    }

    static void generateNestRoom(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 11; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.BEDROCK, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 10; c++) {
                    if ((a == 2 || a == 14 || b == 2 || b == 14) && (a != 2 || b <= 3 || b >= 12 || !cell.west || c >= 10) && (a != 14 || b <= 3 || b >= 12 || !cell.east || c >= 10)
                            && (b != 2 || a <= 3 || a >= 12 || !cell.north || c >= 10) && (b != 14 || a <= 3 || a >= 12 || !cell.south || c >= 10)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.VOID, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 9; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.CRUST, cell);
                    }
                    if ((a == 4 && !cell.west || a == 12 && !cell.east || b == 4 && !cell.north || b == 12 && !cell.south) && ctx.random.nextBoolean()) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.CRUST, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                ctx.placeBlock(x + a, y - 1, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 1, z + b, GenContext.CRUST, cell);
                ctx.placeBlock(x + a, y + 11, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y + 10, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 9, z + b, GenContext.CRUST, cell);
                if (ctx.random.nextBoolean()) {
                    ctx.placeBlock(x + a, y + 8, z + b, GenContext.CRUST, cell);
                } else if (ctx.random.nextBoolean() && ctx.level.isEmptyBlock(new BlockPos(x + a, y + 8, z + b))) {
                    ctx.level.setBlock(new BlockPos(x + a, y + 8, z + b), TCBlocks.CRYSTAL_VITIUM.get().defaultBlockState(), 3);
                }
            }
        }
        ctx.placeBlock(x + 8, y + 2, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 8, y + 3, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 8, y + 4, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 7, y + 2, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 8, y + 2, z + 7, GenContext.CRUST, cell);
        ctx.placeBlock(x + 9, y + 2, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 8, y + 2, z + 9, GenContext.CRUST, cell);
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 7, y + 3, z + 8, GenContext.CRUST, cell);
        }
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 8, y + 3, z + 7, GenContext.CRUST, cell);
        }
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 9, y + 3, z + 8, GenContext.CRUST, cell);
        }
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 8, y + 3, z + 9, GenContext.CRUST, cell);
        }
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 8, y + 5, z + 8, GenContext.GLOW_TILE, cell);
        }
        ctx.placeBlock(x + 8, y + 8, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 8, y + 7, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 8, y + 6, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 7, y + 8, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 8, y + 8, z + 7, GenContext.CRUST, cell);
        ctx.placeBlock(x + 9, y + 8, z + 8, GenContext.CRUST, cell);
        ctx.placeBlock(x + 8, y + 8, z + 9, GenContext.CRUST, cell);
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 7, y + 7, z + 8, GenContext.CRUST, cell);
        }
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 8, y + 7, z + 7, GenContext.CRUST, cell);
        }
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 9, y + 7, z + 8, GenContext.CRUST, cell);
        }
        if (ctx.random.nextBoolean()) {
            ctx.placeBlock(x + 8, y + 7, z + 9, GenContext.CRUST, cell);
        }
        generateConnections(ctx, cx, cz, y, cell, 3, true);
        for (int a = -5; a <= 5; a++) {
            for (int b = -5; b <= 5; b++) {
                BlockPos target = new BlockPos(x + 8 + a, y + 2, z + 8 + b);
                if (ctx.random.nextFloat() < 0.15F && ctx.level.isEmptyBlock(target)) {
                    float roll = ctx.random.nextFloat();
                    boolean crate = ctx.random.nextFloat() < 0.2F;
                    BlockState loot;
                    if (roll < 0.15F) {
                        loot = crate ? TCBlocks.LOOT_CRATE_RARE.get().defaultBlockState() : TCBlocks.LOOT_URN_RARE.get().defaultBlockState();
                    } else if (roll < 0.4F) {
                        loot = crate ? TCBlocks.LOOT_CRATE_UNCOMMON.get().defaultBlockState() : TCBlocks.LOOT_URN_UNCOMMON.get().defaultBlockState();
                    } else {
                        loot = crate ? TCBlocks.LOOT_CRATE_COMMON.get().defaultBlockState() : TCBlocks.LOOT_URN_COMMON.get().defaultBlockState();
                    }
                    ctx.level.setBlock(target, loot, 3);
                }
            }
        }
    }

    static void generateLibraryRoom(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.BEDROCK, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || a == 14 || b == 2 || b == 14) && (a != 2 || b <= 3 || b >= 12 || !cell.west || c >= 10) && (a != 14 || b <= 3 || b >= 12 || !cell.east || c >= 10)
                            && (b != 2 || a <= 3 || a >= 12 || !cell.north || c >= 10) && (b != 14 || a <= 3 || a >= 12 || !cell.south || c >= 10)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.VOID, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.STONE, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                ctx.placeBlock(x + a, y - 1, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 1, z + b, GenContext.STONE, cell);
                ctx.placeBlock(x + a, y + 12, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y + 11, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 10, z + b, GenContext.STONE, cell);
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    if (a <= 5 && b <= 5 || a <= 5 && b >= 11 || a >= 11 && b <= 5 || a >= 11 && b >= 11) {
                        ctx.placeBlock(x + a, y + 2, z + b, GenContext.STONE, cell);
                        ctx.placeBlock(x + a, y + 9, z + b, GenContext.STONE, cell);
                    }
                    if (a == 5 && b == 5 || a == 5 && b == 11 || a == 11 && b == 5 || a == 11 && b == 11) {
                        ctx.level.setBlock(new BlockPos(x + a, y + 3, z + b), TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
                        ctx.level.setBlock(new BlockPos(x + a, y + 8, z + b), TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
                    }
                }
            }
        }
        for (int g = 0; g < 5; g++) {
            ctx.placeBlock(x + 6 + g, y + 2, z + 4, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
            ctx.placeBlock(x + 6 + g, y + 2, z + 12, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
            ctx.placeBlock(x + 12, y + 2, z + 6 + g, GenContext.STAIR_DIRECTIONAL, Direction.EAST, cell);
            ctx.placeBlock(x + 4, y + 2, z + 6 + g, GenContext.STAIR_DIRECTIONAL, Direction.WEST, cell);
            ctx.placeBlock(x + 6 + g, y + 9, z + 4, GenContext.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
            ctx.placeBlock(x + 6 + g, y + 9, z + 12, GenContext.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
            ctx.placeBlock(x + 12, y + 9, z + 6 + g, GenContext.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
            ctx.placeBlock(x + 4, y + 9, z + 6 + g, GenContext.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
        }
        crystalPillar(ctx, x + 5, y, z + 5);
        crystalPillar(ctx, x + 5, y, z + 11);
        crystalPillar(ctx, x + 11, y, z + 5);
        crystalPillar(ctx, x + 11, y, z + 11);
        ctx.level.setBlock(new BlockPos(x + 8, y + 2, z + 8), TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
        ctx.level.setBlock(new BlockPos(x + 8, y + 3, z + 8), TCBlocks.ELDRITCH_STONE_CRYSTAL.get().defaultBlockState(), 3);
        ctx.level.setBlock(new BlockPos(x + 8, y + 4, z + 8), bottomSlab(), 3);
        ctx.level.setBlock(new BlockPos(x + 8, y + 9, z + 8), TCBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), 3);
        ctx.level.setBlock(new BlockPos(x + 8, y + 8, z + 8), TCBlocks.ELDRITCH_STONE_CRYSTAL.get().defaultBlockState(), 3);
        ctx.level.setBlock(new BlockPos(x + 8, y + 7, z + 8), topSlab(), 3);
        generateConnections(ctx, cx, cz, y, cell, 3, true);
    }

    private static void crystalPillar(GenContext ctx, int x, int y, int z) {
        ctx.level.setBlock(new BlockPos(x, y + 4, z), TCBlocks.ELDRITCH_STONE_CRYSTAL.get().defaultBlockState(), 3);
        ctx.level.setBlock(new BlockPos(x, y + 5, z), bottomSlab(), 3);
        ctx.level.setBlock(new BlockPos(x, y + 7, z), TCBlocks.ELDRITCH_STONE_CRYSTAL.get().defaultBlockState(), 3);
        ctx.level.setBlock(new BlockPos(x, y + 6, z), topSlab(), 3);
    }

    private static BlockState bottomSlab() {
        return TCBlocks.SLAB_ARCANE_STONE.get().defaultBlockState();
    }

    private static BlockState topSlab() {
        return TCBlocks.SLAB_ARCANE_STONE.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
    }
}
