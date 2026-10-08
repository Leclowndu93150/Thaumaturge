package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchNothing;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

public final class LabyrinthBlocks {
    private LabyrinthBlocks() {}

    public static List<Block> passableBlocks() {
        return List.of(TTBlocks.ELDRITCH_DOOR.get(), TTBlocks.ELDRITCH_LOCK.get());
    }

    static boolean passable(BlockState state) {
        return passableBlocks().contains(state.getBlock());
    }

    static SkinPalette skin() {
        return new SkinPalette(stone(), stone(), rock());
    }

    static BlockState stone() {
        return TTBlocks.ELDRITCH_STONE.get().defaultBlockState();
    }

    static BlockState inert() {
        return TTBlocks.ELDRITCH_STONE_INERT.get().defaultBlockState();
    }

    static BlockState rock() {
        return TTBlocks.ELDRITCH_ROCK.get().defaultBlockState();
    }

    static BlockState crust() {
        return TTBlocks.ELDRITCH_CRUST.get().defaultBlockState();
    }

    static BlockState glowingCrust() {
        return TTBlocks.ELDRITCH_CRUST_GLOWING.get().defaultBlockState();
    }

    static BlockState tile() {
        return TTBlocks.STONE_ELDRITCH_TILE.get().defaultBlockState();
    }

    static BlockState obsidianTile() {
        return TTBlocks.OBSIDIAN_TILE.get().defaultBlockState();
    }

    static BlockState door() {
        return TTBlocks.ELDRITCH_DOOR.get().defaultBlockState();
    }

    static BlockState glyph() {
        return TTBlocks.ELDRITCH_STONE_CRYSTAL.get().defaultBlockState();
    }

    static BlockState trap() {
        return TTBlocks.ELDRITCH_TRAP.get().defaultBlockState();
    }

    static BlockState crabSpawner() {
        return TTBlocks.ELDRITCH_CRAB_SPAWNER.get().defaultBlockState();
    }

    static BlockState capstone() {
        return TTBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState();
    }

    static BlockState obelisk() {
        return TTBlocks.ELDRITCH_OBELISK.get().defaultBlockState();
    }

    static BlockState pillar() {
        return TTBlocks.ELDRITCH_PILLAR.get().defaultBlockState();
    }

    static BlockState column(Direction.Axis axis) {
        return TTBlocks.STONE_ELDRITCH_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    static BlockState tileStairs(Direction facing, Half half, StairsShape shape) {
        return stairs(TTBlocks.STAIRS_ELDRITCH_TILE.get().defaultBlockState(), facing, half, shape);
    }

    static BlockState stoneStairs(Direction facing, Half half, StairsShape shape) {
        return stairs(TTBlocks.STAIRS_ELDRITCH.get().defaultBlockState(), facing, half, shape);
    }

    static boolean roundingHost(BlockState state) {
        return state.is(TTBlocks.ELDRITCH_STONE.get())
                || state.is(TTBlocks.ELDRITCH_STONE_INERT.get())
                || state.is(TTBlocks.ELDRITCH_ROCK.get());
    }

    static BlockState rockStairs(Direction facing) {
        return stairs(
                TTBlocks.STAIRS_ELDRITCH_ROCK.get().defaultBlockState(), facing, Half.BOTTOM, StairsShape.STRAIGHT);
    }

    private static BlockState stairs(BlockState stairs, Direction facing, Half half, StairsShape shape) {
        return stairs.setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.SHAPE, shape);
    }

    static BlockState starfield() {
        return TTBlocks.ELDRITCH_NOTHING.get().defaultBlockState().setValue(BlockEldritchNothing.EXPOSED, Boolean.TRUE);
    }

    static BlockState bookshelf() {
        return Blocks.BOOKSHELF.defaultBlockState();
    }

    static BlockState web() {
        return Blocks.COBWEB.defaultBlockState();
    }
}
