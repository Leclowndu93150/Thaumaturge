package com.leclowndu93150.thaumaturge.content.eldritch.gen;

import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeCell;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

public final class MazeChunkStamper {
    private MazeChunkStamper() {}

    public static void stamp(WorldGenLevel level, RandomSource random, int cx, int cz, MazeCell cell) {
        GenContext ctx = new GenContext(level, random);
        int y = OuterLands.MAZE_Y;
        switch (cell.feature) {
            case MazeCell.FEATURE_PORTAL -> GenRooms.generatePortalRoom(ctx, cx, cz, y, cell);
            case MazeCell.FEATURE_BOSS_NW, MazeCell.FEATURE_BOSS_NE, MazeCell.FEATURE_BOSS_SW, MazeCell.FEATURE_BOSS_SE -> GenRooms.generateBossRoom(ctx, cx, cz, y, cell);
            case MazeCell.FEATURE_KEY -> GenRooms.generateKeyRoom(ctx, cx, cz, y, cell);
            case MazeCell.FEATURE_NEST -> GenRooms.generateNestRoom(ctx, cx, cz, y, cell);
            case MazeCell.FEATURE_LIBRARY -> GenRooms.generateLibraryRoom(ctx, cx, cz, y, cell);
            default -> GenPassage.generateDefaultPassage(ctx, cx, cz, y, cell);
        }
        GenCommonPieces.processDecorations(ctx);
    }
}
