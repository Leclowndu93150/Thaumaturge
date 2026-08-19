package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public final class EldritchArenaShapes {
    private EldritchArenaShapes() {}

    public static void genObelisk(LevelWriter level, int x, int y, int z) {
        level.setBlock(new BlockPos(x, y, z), TCBlocks.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
        for (int i = 1; i <= 4; i++) {
            level.setBlock(new BlockPos(x, y + i, z), TCBlocks.ELDRITCH_PILLAR.get().defaultBlockState(), 3);
        }
    }

    public static BlockState stairFromLegacyMeta(Block stairs, int legacyMeta) {
        Direction facing = switch (legacyMeta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        Half half = (legacyMeta & 4) != 0 ? Half.TOP : Half.BOTTOM;
        return stairs.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }
}
