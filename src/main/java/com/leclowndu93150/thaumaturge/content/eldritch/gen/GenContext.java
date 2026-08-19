package com.leclowndu93150.thaumaturge.content.eldritch.gen;

import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchInset;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.block.EldritchArenaShapes;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeCell;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class GenContext {
    public static final int BEDROCK = 1;
    public static final int STONE = 2;
    public static final int STAIR_A = 3;
    public static final int STAIR_B = 4;
    public static final int STAIR_A_INV = 5;
    public static final int STAIR_B_INV = 6;
    public static final int GLOW_TILE = 7;
    public static final int VOID = 8;
    public static final int AIR_REPL = 9;
    public static final int STAIR_DIRECTIONAL = 10;
    public static final int STAIR_DIRECTIONAL_INV = 11;
    public static final int DOOR_BLOCK = 15;
    public static final int DOOR_LOCK = 16;
    public static final int VOID_DOOR = 17;
    public static final int ROCK = 18;
    public static final int STONE_NOSPAWN = 19;
    public static final int STONE_TRAPPED = 20;
    public static final int CRUST = 21;
    public static final int BEDROCK_REPL = 99;

    public final WorldGenLevel level;
    public final RandomSource random;
    public final List<BlockPos> decoCommon = new ArrayList<>();
    public final List<BlockPos> crabSpawner = new ArrayList<>();
    public final List<BlockPos> decoUrn = new ArrayList<>();

    public GenContext(WorldGenLevel level, RandomSource random) {
        this.level = level;
        this.random = random;
    }

    public void placeBlock(int x, int y, int z, int paletteId, MazeCell cell) {
        placeBlock(x, y, z, paletteId, null, cell);
    }

    public void placeBlock(int x, int y, int z, int paletteId, Direction dir, MazeCell cell) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = null;
        switch (paletteId) {
            case BEDROCK -> {
                if (level.isEmptyBlock(pos)) {
                    state = Blocks.BEDROCK.defaultBlockState();
                }
            }
            case BEDROCK_REPL -> state = Blocks.BEDROCK.defaultBlockState();
            case STONE -> {
                if (cell.feature == MazeCell.FEATURE_NEST && random.nextInt(3) == 0) {
                    state = crustPlacement(pos, cell);
                } else if (!isNothing(pos)) {
                    if (random.nextInt(25) == 0) {
                        boolean crab = cell.feature == MazeCell.FEATURE_NEST || random.nextInt(50) == 0;
                        if (crab && (cell.feature == 0 || cell.feature == MazeCell.FEATURE_NEST)) {
                            crabSpawner.add(pos);
                        } else {
                            decoCommon.add(pos);
                        }
                    }
                    state = TCBlocks.ELDRITCH_STONE.get().defaultBlockState();
                }
            }
            case STONE_NOSPAWN -> {
                if (!isNothing(pos)) {
                    state = TCBlocks.ELDRITCH_STONE_INERT.get().defaultBlockState();
                }
            }
            case ROCK -> {
                if (!isNothing(pos)) {
                    state = TCBlocks.ELDRITCH_ROCK.get().defaultBlockState();
                }
            }
            case STONE_TRAPPED -> {
                if (!isNothing(pos)) {
                    state = TCBlocks.ELDRITCH_TRAP.get().defaultBlockState();
                }
            }
            case CRUST -> state = crustPlacement(pos, cell);
            case STAIR_A -> {
                if (random.nextFloat() < 0.005F) {
                    decoUrn.add(pos);
                }
                state = legacyStair(dir == Direction.NORTH || dir == Direction.SOUTH ? 1 : 3);
            }
            case STAIR_B -> {
                if (random.nextFloat() < 0.005F) {
                    decoUrn.add(pos);
                }
                state = legacyStair(dir == Direction.NORTH || dir == Direction.SOUTH ? 0 : 2);
            }
            case STAIR_A_INV -> state = legacyStair(dir == Direction.NORTH || dir == Direction.SOUTH ? 5 : 7);
            case STAIR_B_INV -> state = legacyStair(dir == Direction.NORTH || dir == Direction.SOUTH ? 4 : 6);
            case STAIR_DIRECTIONAL -> state = legacyStair(switch (dir) {
                case NORTH -> 3;
                case SOUTH -> 2;
                case EAST -> 0;
                default -> 1;
            });
            case STAIR_DIRECTIONAL_INV -> state = legacyStair(switch (dir) {
                case NORTH -> 7;
                case SOUTH -> 6;
                case EAST -> 4;
                default -> 5;
            });
            case GLOW_TILE -> state = TCBlocks.ELDRITCH_CRUST_GLOWING.get().defaultBlockState();
            case VOID -> state = TCBlocks.ELDRITCH_NOTHING.get().defaultBlockState();
            case AIR_REPL -> {
                state = Blocks.AIR.defaultBlockState();
                decoCommon.remove(pos);
                crabSpawner.remove(pos);
                decoUrn.remove(pos);
            }
            case DOOR_BLOCK -> {
                state = TCBlocks.ELDRITCH_DOOR.get().defaultBlockState();
                decoCommon.remove(pos);
                crabSpawner.remove(pos);
                decoUrn.remove(pos);
            }
            case DOOR_LOCK -> {
                state = TCBlocks.ELDRITCH_LOCK.get().defaultBlockState();
                decoCommon.remove(pos);
                crabSpawner.remove(pos);
                decoUrn.remove(pos);
            }
            case VOID_DOOR -> state = Blocks.BARRIER.defaultBlockState();
            default -> {
            }
        }
        if (state != null) {
            Block block = state.getBlock();
            int flags = block != TCBlocks.ELDRITCH_NOTHING.get() && block != Blocks.BEDROCK && block != Blocks.AIR ? 3 : 2;
            level.setBlock(pos, state, flags);
            if (block instanceof StairBlock || block instanceof BlockEldritchInset) {
                level.getChunk(pos).markPosForPostprocessing(pos);
            }
        }
    }

    private BlockState crustPlacement(BlockPos pos, MazeCell cell) {
        if (isNothing(pos)) {
            return null;
        }
        if (random.nextInt(25) == 0) {
            return TCBlocks.ELDRITCH_CRUST_GLOWING.get().defaultBlockState();
        }
        if (random.nextInt(25) == 0) {
            boolean crab = cell.feature == MazeCell.FEATURE_NEST || (cell.feature == 12 && random.nextBoolean()) || random.nextInt(25) == 0;
            if (crab && (cell.feature == 0 || cell.feature == MazeCell.FEATURE_NEST || cell.feature == 12)) {
                crabSpawner.add(pos);
            }
        }
        return TCBlocks.ELDRITCH_CRUST.get().defaultBlockState();
    }

    private boolean isNothing(BlockPos pos) {
        return level.getBlockState(pos).is(TCBlocks.ELDRITCH_NOTHING.get());
    }

    private BlockState legacyStair(int legacyMeta) {
        return EldritchArenaShapes.stairFromLegacyMeta(TCBlocks.STAIRS_ELDRITCH.get(), legacyMeta);
    }

    public void setLockFacing(BlockPos pos, Direction dir) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockEldritchLock) {
            level.setBlock(pos, state.setValue(BlockEldritchLock.FACING, dir), 3);
        }
    }
}
