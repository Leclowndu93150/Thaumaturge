package com.leclowndu93150.thaumaturge.server.command.admin;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

final class ShowcaseBuilder {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_ALL_SIDEEFFECTS;
    private static final int BLOCK_COLUMNS = 16;
    private static final int BLOCK_SPACING = 3;
    private static final int SIDE_MARGIN = 2;
    private static final int FRONT_MARGIN = 2;
    private static final int WALL_GAP = 3;
    private static final int WALL_BORDER = 1;
    private static final int MIN_CLEAR_HEIGHT = 6;
    private static final int LIGHT_SPACING = 6;
    private static final int LIGHT_HEIGHT = 4;
    private static final int LIGHT_LAYER_STEP = 5;
    private static final Direction[] SUPPORT_SIDES = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};

    private final ServerLevel level;
    private final BlockPos origin;
    private final Direction forward;
    private final Direction right;
    private final List<Block> blocks;
    private final List<Item> items;
    private final List<BlockState> supports;
    private final int width;
    private final int wallRow;
    private final int frameColumns;
    private final int frameRows;
    private final int clearHeight;

    ShowcaseBuilder(ServerLevel level, BlockPos origin, Direction forward, List<Block> blocks, List<Item> items) {
        this.level = level;
        this.origin = origin;
        this.forward = forward;
        this.right = forward.getClockWise();
        this.blocks = blocks;
        this.items = items;
        this.supports = List.of(Blocks.SMOOTH_STONE.defaultBlockState(), Blocks.DIRT.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.SAND.defaultBlockState(),
                TCBlocks.LOG_GREATWOOD.get().defaultBlockState());
        int blockRows = Mth.positiveCeilDiv(blocks.size(), BLOCK_COLUMNS);
        this.width = SIDE_MARGIN * 2 + (BLOCK_COLUMNS - 1) * BLOCK_SPACING + 1;
        this.wallRow = FRONT_MARGIN + Math.max(0, blockRows - 1) * BLOCK_SPACING + WALL_GAP + 1;
        this.frameColumns = width - WALL_BORDER * 2;
        this.frameRows = Mth.positiveCeilDiv(items.size(), frameColumns);
        this.clearHeight = Math.max(MIN_CLEAR_HEIGHT, frameRows + WALL_BORDER * 2);
    }

    boolean fitsHeight() {
        return origin.getY() - 1 >= level.getMinY() && origin.getY() + clearHeight <= level.getMaxY();
    }

    boolean isLoaded() {
        BlockPos near = at(0, 0, 0);
        BlockPos far = at(width - 1, wallRow, 0);
        int minX = SectionPos.blockToSectionCoord(Math.min(near.getX(), far.getX()));
        int maxX = SectionPos.blockToSectionCoord(Math.max(near.getX(), far.getX()));
        int minZ = SectionPos.blockToSectionCoord(Math.min(near.getZ(), far.getZ()));
        int maxZ = SectionPos.blockToSectionCoord(Math.max(near.getZ(), far.getZ()));
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!level.hasChunk(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    void build() {
        removeHangingEntities();
        clearArea();
        placeLights();
        placeBlocks();
        buildWall();
        hangFrames();
    }

    private void removeHangingEntities() {
        AABB area = AABB.encapsulatingFullBlocks(at(0, 0, -1), at(width - 1, wallRow, clearHeight));
        for (HangingEntity entity : level.getEntitiesOfClass(HangingEntity.class, area)) {
            entity.discard();
        }
    }

    private void clearArea() {
        BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int column = 0; column < width; column++) {
            for (int row = 0; row <= wallRow; row++) {
                set(at(column, row, -1), floor);
                for (int up = 0; up < clearHeight; up++) {
                    set(at(column, row, up), air);
                }
            }
        }
    }

    private void placeLights() {
        BlockState light = Blocks.LIGHT.defaultBlockState();
        for (int column = LIGHT_SPACING / 2; column < width; column += LIGHT_SPACING) {
            for (int row = LIGHT_SPACING / 2; row < wallRow - 1; row += LIGHT_SPACING) {
                for (int up = LIGHT_HEIGHT; up < clearHeight; up += LIGHT_LAYER_STEP) {
                    set(at(column, row, up), light);
                }
            }
        }
    }

    private void placeBlocks() {
        for (int i = 0; i < blocks.size(); i++) {
            BlockPos pos = at(SIDE_MARGIN + i % BLOCK_COLUMNS * BLOCK_SPACING, FRONT_MARGIN + i / BLOCK_COLUMNS * BLOCK_SPACING, 0);
            BlockState state = blocks.get(i).defaultBlockState();
            if (!state.getFluidState().isEmpty()) {
                set(pos.below(), state);
                continue;
            }
            set(pos, state);
            if (!state.canSurvive(level, pos)) {
                support(pos, state);
            }
        }
    }

    private void support(BlockPos pos, BlockState state) {
        for (Direction side : SUPPORT_SIDES) {
            BlockPos supportPos = pos.relative(side);
            BlockState previous = level.getBlockState(supportPos);
            for (BlockState candidate : supports) {
                set(supportPos, candidate);
                if (state.canSurvive(level, pos)) {
                    return;
                }
            }
            set(supportPos, previous);
        }
    }

    private void buildWall() {
        BlockState wall = Blocks.POLISHED_ANDESITE.defaultBlockState();
        for (int column = 0; column < width; column++) {
            for (int up = 0; up < frameRows + WALL_BORDER * 2; up++) {
                set(at(column, wallRow, up), wall);
            }
        }
    }

    private void hangFrames() {
        Direction facing = forward.getOpposite();
        for (int i = 0; i < items.size(); i++) {
            ItemFrame frame = new ItemFrame(level, at(WALL_BORDER + i % frameColumns, wallRow - 1, frameRows - i / frameColumns), facing);
            frame.setItem(new ItemStack(items.get(i)), false);
            frame.setInvulnerable(true);
            level.addFreshEntity(frame);
        }
    }

    private BlockPos at(int column, int row, int up) {
        return origin.relative(right, column).relative(forward, row).above(up);
    }

    private void set(BlockPos pos, BlockState state) {
        level.setBlock(pos, state, FLAGS);
    }
}
