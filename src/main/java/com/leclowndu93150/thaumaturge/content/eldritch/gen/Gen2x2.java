package com.leclowndu93150.thaumaturge.content.eldritch.gen;

import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeCell;
import net.minecraft.core.Direction;

public final class Gen2x2 extends GenCommonPieces {
    private Gen2x2() {}

    static void generateUpperLeft(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || b == 1) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.BEDROCK, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 15; a++) {
            for (int b = 2; b <= 15; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || b == 2) && (a != 2 || b <= 4 || b >= 12 || !cell.west || c >= 10) && (b != 2 || a <= 4 || a >= 12 || !cell.north || c >= 10)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.VOID, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 15; a++) {
            for (int b = 3; b <= 15; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 3 || b == 3) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.ROCK, cell);
                    }
                }
            }
        }
        floorAndCeiling(ctx, cell, x, z, y, 2, 15, 2, 15);
        for (int g = 4; g <= 15; g++) {
            ctx.placeBlock(x + g, y + 2, z + 4, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
            ctx.placeBlock(x + g, y + 10, z + 4, GenContext.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
        }
        for (int g = 4; g <= 15; g++) {
            ctx.placeBlock(x + 4, y + 2, z + g, GenContext.STAIR_DIRECTIONAL, Direction.WEST, cell);
            ctx.placeBlock(x + 4, y + 10, z + g, GenContext.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
        }
        generateConnections(ctx, cx, cz, y, cell, 3, true);
    }

    static void generateUpperRight(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 0; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 15 || b == 1) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.BEDROCK, cell);
                    }
                }
            }
        }
        for (int a = 0; a <= 14; a++) {
            for (int b = 2; b <= 15; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 14 || b == 2) && (a != 14 || b <= 4 || b >= 12 || !cell.east || c >= 10) && (b != 2 || a <= 4 || a >= 12 || !cell.north || c >= 10)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.VOID, cell);
                    }
                }
            }
        }
        for (int a = 0; a <= 13; a++) {
            for (int b = 3; b <= 15; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 13 || b == 3) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.ROCK, cell);
                    }
                }
            }
        }
        floorAndCeiling(ctx, cell, x, z, y, 0, 14, 2, 15);
        for (int g = 0; g <= 11; g++) {
            ctx.placeBlock(x + g, y + 2, z + 4, GenContext.STAIR_DIRECTIONAL, Direction.NORTH, cell);
            ctx.placeBlock(x + g, y + 10, z + 4, GenContext.STAIR_DIRECTIONAL_INV, Direction.NORTH, cell);
        }
        for (int g = 4; g <= 15; g++) {
            ctx.placeBlock(x + 12, y + 2, z + g, GenContext.STAIR_DIRECTIONAL, Direction.EAST, cell);
            ctx.placeBlock(x + 12, y + 10, z + g, GenContext.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
        }
        generateConnections(ctx, cx, cz, y, cell, 3, true);
    }

    static void generateLowerLeft(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 1; a <= 15; a++) {
            for (int b = 0; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || b == 15) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.BEDROCK, cell);
                    }
                }
            }
        }
        for (int a = 2; a <= 15; a++) {
            for (int b = 0; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || b == 14) && (a != 2 || b <= 4 || b >= 12 || !cell.west || c >= 10) && (b != 14 || a <= 4 || a >= 12 || !cell.south || c >= 10)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.VOID, cell);
                    }
                }
            }
        }
        for (int a = 3; a <= 15; a++) {
            for (int b = 0; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 3 || b == 13) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.ROCK, cell);
                    }
                }
            }
        }
        floorAndCeiling(ctx, cell, x, z, y, 2, 15, 0, 14);
        for (int g = 4; g <= 15; g++) {
            ctx.placeBlock(x + g, y + 2, z + 12, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
            ctx.placeBlock(x + g, y + 10, z + 12, GenContext.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
        }
        for (int g = 0; g <= 11; g++) {
            ctx.placeBlock(x + 4, y + 2, z + g, GenContext.STAIR_DIRECTIONAL, Direction.WEST, cell);
            ctx.placeBlock(x + 4, y + 10, z + g, GenContext.STAIR_DIRECTIONAL_INV, Direction.WEST, cell);
        }
        generateConnections(ctx, cx, cz, y, cell, 3, true);
    }

    static void generateLowerRight(GenContext ctx, int cx, int cz, int y, MazeCell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int a = 0; a <= 15; a++) {
            for (int b = 0; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 15 || b == 15) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.BEDROCK, cell);
                    }
                }
            }
        }
        for (int a = 0; a <= 14; a++) {
            for (int b = 0; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 14 || b == 14) && (a != 14 || b <= 4 || b >= 12 || !cell.east || c >= 10) && (b != 14 || a <= 4 || a >= 12 || !cell.south || c >= 10)) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.VOID, cell);
                    }
                }
            }
        }
        for (int a = 0; a <= 13; a++) {
            for (int b = 0; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a == 13 || b == 13) {
                        ctx.placeBlock(x + a, y + c, z + b, GenContext.ROCK, cell);
                    }
                }
            }
        }
        floorAndCeiling(ctx, cell, x, z, y, 0, 14, 0, 14);
        for (int g = 0; g <= 11; g++) {
            ctx.placeBlock(x + g, y + 2, z + 12, GenContext.STAIR_DIRECTIONAL, Direction.SOUTH, cell);
            ctx.placeBlock(x + g, y + 10, z + 12, GenContext.STAIR_DIRECTIONAL_INV, Direction.SOUTH, cell);
        }
        for (int g = 0; g <= 12; g++) {
            ctx.placeBlock(x + 12, y + 2, z + g, GenContext.STAIR_DIRECTIONAL, Direction.EAST, cell);
            ctx.placeBlock(x + 12, y + 10, z + g, GenContext.STAIR_DIRECTIONAL_INV, Direction.EAST, cell);
        }
        generateConnections(ctx, cx, cz, y, cell, 3, true);
    }

    private static void floorAndCeiling(GenContext ctx, MazeCell cell, int x, int z, int y, int aMin, int aMax, int bMin, int bMax) {
        for (int a = aMin; a <= aMax; a++) {
            for (int b = bMin; b <= bMax; b++) {
                ctx.placeBlock(x + a, y - 1, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 1, z + b, GenContext.STONE_NOSPAWN, cell);
                ctx.placeBlock(x + a, y + 13, z + b, GenContext.BEDROCK, cell);
                ctx.placeBlock(x + a, y + 12, z + b, GenContext.VOID, cell);
                ctx.placeBlock(x + a, y + 11, z + b, GenContext.STONE, cell);
            }
        }
    }
}
