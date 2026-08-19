package com.leclowndu93150.thaumaturge.content.eldritch.maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class MazeLayoutGenerator {
    private static final int N = 1;
    private static final int S = 2;
    private static final int E = 4;
    private static final int W = 8;
    private static final int FEATURE_TEMP = 99;
    private static final int RESCUE_MARK = FEATURE_TEMP << 8;
    private static final float PICK_NEWEST_CHANCE = 0.45F;
    private static final float PICK_RANDOM_CHANCE = 0.9F;
    private static final int DECORATION_ROLL = 25;
    private static final int[] DECORATION_TABLE = {8, 10, 11, 11, 12, 12, 13, 14};
    private static final int DEAD_END_FEATURE_BASE = MazeCell.FEATURE_NEST;
    private static final int DEAD_END_FEATURE_SPREAD = 3;

    private final int width;
    private final int height;
    private final Random rand;
    private final List<Integer> deck = new ArrayList<>(Arrays.asList(N, S, E, W));
    public final int[][] grid;

    private record Spot(int col, int row) {
    }

    public MazeLayoutGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        this.grid = new int[height][width];
    }

    private static int opposite(int dir) {
        return switch (dir) {
            case N -> S;
            case S -> N;
            case E -> W;
            case W -> E;
            default -> -99;
        };
    }

    private static int stepX(int dir) {
        return dir == E ? 1 : dir == W ? -1 : 0;
    }

    private static int stepY(int dir) {
        return dir == S ? 1 : dir == N ? -1 : 0;
    }

    private boolean inside(int col, int row) {
        return 0 < col && col < width - 1 && 0 < row && row < height - 1;
    }

    private MazeCell cellAt(int col, int row) {
        return new MazeCell((short) grid[row][col]);
    }

    public boolean generate() {
        Spot boss = stampBossRoom();
        int portalCol = 1 + width / 2;
        int portalRow = 1 + height / 2;
        grid[portalRow][portalCol] = MazeCell.FEATURE_PORTAL << 8;
        scatterBlobs();
        List<Spot> frontier = new ArrayList<>();
        seedFromPortal(portalCol, portalRow, frontier);
        if (!expand(frontier)) {
            return false;
        }
        clearBlobLeftovers();
        openExtraPortalDoors(portalCol, portalRow);
        if (!linkBossRoom(boss) && !carveRescueTunnel(boss, frontier)) {
            return false;
        }
        clearRescueMarks();
        if (!placeDeadEndFeatures()) {
            return false;
        }
        sprinkleDecorations();
        return true;
    }

    private Spot stampBossRoom() {
        int col = 0;
        int row = 0;
        switch (rand.nextInt(4)) {
            case 1 -> {
                col = width - 2;
                row = height - 2;
            }
            case 2 -> col = width - 2;
            case 3 -> row = height - 2;
        }
        grid[row][col] = MazeCell.FEATURE_BOSS_NW << 8;
        grid[row][col + 1] = MazeCell.FEATURE_BOSS_NE << 8;
        grid[row + 1][col] = MazeCell.FEATURE_BOSS_SW << 8;
        grid[row + 1][col + 1] = MazeCell.FEATURE_BOSS_SE << 8;
        return new Spot(col, row);
    }

    private void scatterBlobs() {
        int budget = (width + height) / 4;
        for (int placed = 0; placed < budget; placed++) {
            int size = 1 + rand.nextInt(3);
            if (size > 2) {
                budget--;
            }
            int originCol = rand.nextInt(width - size);
            int originRow = rand.nextInt(height - size);
            for (int col = originCol; col < originCol + size; col++) {
                for (int row = originRow; row < originRow + size; row++) {
                    if (grid[row][col] == 0) {
                        grid[row][col] = -1;
                    }
                }
            }
        }
    }

    private void seedFromPortal(int portalCol, int portalRow, List<Spot> frontier) {
        Collections.shuffle(deck, rand);
        int dir = deck.get(0);
        int col = portalCol + stepX(dir);
        int row = portalRow + stepY(dir);
        grid[portalRow][portalCol] |= dir;
        if (grid[row][col] < 0) {
            grid[row][col] = 0;
        }
        grid[row][col] |= opposite(dir);
        frontier.add(new Spot(col, row));
    }

    private boolean expand(List<Spot> frontier) {
        boolean grew = false;
        while (!frontier.isEmpty()) {
            int index = pickFrontierIndex(frontier.size());
            Spot spot = frontier.get(index);
            Collections.shuffle(deck, rand);
            boolean carved = false;
            for (int dir : deck) {
                int col = spot.col() + stepX(dir);
                int row = spot.row() + stepY(dir);
                if (!inside(col, row)) {
                    continue;
                }
                if (grid[row][col] == 0) {
                    grid[spot.row()][spot.col()] |= dir;
                    grid[row][col] |= opposite(dir);
                    frontier.add(new Spot(col, row));
                    carved = true;
                }
                if (carved) {
                    grew = true;
                    break;
                }
            }
            if (!carved) {
                frontier.remove(index);
            }
        }
        return grew;
    }

    private int pickFrontierIndex(int size) {
        float roll = rand.nextFloat();
        if (roll <= PICK_NEWEST_CHANCE) {
            return size - 1;
        }
        return roll <= PICK_RANDOM_CHANCE ? rand.nextInt(size) : 0;
    }

    private void clearBlobLeftovers() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (grid[row][col] < 0) {
                    grid[row][col] = 0;
                }
            }
        }
    }

    private void openExtraPortalDoors(int portalCol, int portalRow) {
        Collections.shuffle(deck, rand);
        for (int dir : deck) {
            int col = portalCol + stepX(dir);
            int row = portalRow + stepY(dir);
            if (inside(col, row) && grid[row][col] > 0 && rand.nextBoolean()) {
                grid[row][col] |= opposite(dir);
                grid[portalRow][portalCol] |= dir;
            }
        }
    }

    private boolean linkBossRoom(Spot boss) {
        Collections.shuffle(deck, rand);
        for (int dc = 0; dc < 2; dc++) {
            for (int dr = 0; dr < 2; dr++) {
                for (int dir : deck) {
                    int col = boss.col() + dc + stepX(dir);
                    int row = boss.row() + dr + stepY(dir);
                    if (inside(col, row) && grid[row][col] > 0 && cellAt(col, row).feature == 0) {
                        grid[row][col] |= opposite(dir);
                        grid[boss.row() + dr][boss.col() + dc] |= dir;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean carveRescueTunnel(Spot boss, List<Spot> frontier) {
        List<Integer> doorDeck = new ArrayList<>(Arrays.asList(N, S, E, W));
        Collections.shuffle(doorDeck, rand);
        for (int dc = 0; dc < 2; dc++) {
            for (int dr = 0; dr < 2; dr++) {
                for (int doorDir : doorDeck) {
                    int startCol = boss.col() + dc + stepX(doorDir);
                    int startRow = boss.row() + dr + stepY(doorDir);
                    if (!inside(startCol, startRow) || grid[startRow][startCol] != 0) {
                        continue;
                    }
                    frontier.add(new Spot(startCol, startRow));
                    while (!frontier.isEmpty()) {
                        int index = pickFrontierIndex(frontier.size());
                        Spot spot = frontier.get(index);
                        Collections.shuffle(deck, rand);
                        boolean carved = false;
                        for (int dir : deck) {
                            int col = spot.col() + stepX(dir);
                            int row = spot.row() + stepY(dir);
                            if (!inside(col, row)) {
                                continue;
                            }
                            if (grid[row][col] == 0) {
                                grid[spot.row()][spot.col()] |= dir | RESCUE_MARK;
                                grid[row][col] |= opposite(dir) | RESCUE_MARK;
                                frontier.add(new Spot(col, row));
                                carved = true;
                            } else if (cellAt(col, row).feature == 0) {
                                grid[spot.row()][spot.col()] |= dir;
                                grid[row][col] |= opposite(dir);
                                grid[startRow][startCol] |= opposite(doorDir);
                                grid[boss.row() + dr][boss.col() + dc] |= doorDir;
                                return true;
                            }
                            if (carved) {
                                break;
                            }
                        }
                        if (!carved) {
                            frontier.remove(index);
                        }
                    }
                }
            }
        }
        return false;
    }

    private void clearRescueMarks() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                MazeCell cell = cellAt(col, row);
                if (cell.feature == FEATURE_TEMP) {
                    cell.feature = 0;
                    grid[row][col] = cell.pack();
                }
            }
        }
    }

    private boolean placeDeadEndFeatures() {
        List<Spot> deadEnds = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                MazeCell cell = cellAt(col, row);
                if (cell.openingCount() == 1 && cell.feature == 0) {
                    deadEnds.add(new Spot(col, row));
                }
            }
        }
        if (deadEnds.isEmpty()) {
            return false;
        }
        int keyIndex = rand.nextInt(deadEnds.size());
        setFeature(deadEnds.get(keyIndex), MazeCell.FEATURE_KEY);
        deadEnds.remove(keyIndex);
        int filled = 0;
        while (filled < deadEnds.size() / 2) {
            int pick = rand.nextInt(deadEnds.size());
            Spot spot = deadEnds.get(pick);
            if (cellAt(spot.col(), spot.row()).feature == 0) {
                setFeature(spot, DEAD_END_FEATURE_BASE + rand.nextInt(DEAD_END_FEATURE_SPREAD));
                deadEnds.remove(pick);
                filled++;
            }
        }
        return true;
    }

    private void setFeature(Spot spot, int feature) {
        MazeCell cell = cellAt(spot.col(), spot.row());
        cell.feature = (byte) feature;
        grid[spot.row()][spot.col()] = cell.pack();
    }

    private void sprinkleDecorations() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                MazeCell cell = cellAt(col, row);
                if (cell.feature == 0 && cell.hasAnyOpening() && rand.nextInt(DECORATION_ROLL) == 0) {
                    cell.feature = (byte) DECORATION_TABLE[rand.nextInt(DECORATION_TABLE.length)];
                    grid[row][col] = cell.pack();
                }
            }
        }
    }
}
